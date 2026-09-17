
// Load environment variables from .env
require("dotenv").config();

const express = require("express");
const cors = require("cors");
const { MongoClient, ObjectId } = require("mongodb");

// Create Express application
const app = express();

// Enable CORS
app.use(cors());

// Allow JSON request bodies, including a complete payroll backup file.
app.use(express.json({ limit: "5mb" }));

// These default login details work until you set PAYROLL_USERNAME and PAYROLL_PASSWORD in Render.
const payrollUsername = process.env.PAYROLL_USERNAME || "worker";
const payrollPassword = process.env.PAYROLL_PASSWORD || "payroll";

// Require the payroll login before allowing any person or program to use the API.
app.use("/api", (req, res, next) => {

    const authorization = req.headers.authorization || "";

    if (!authorization.startsWith("Basic ")) {
        return res.status(401).json({
            success: false,
            message: "Payroll login is required."
        });
    }

    const [username, password] = Buffer
            .from(authorization.substring(6), "base64")
            .toString("utf8")
            .split(":");

    if (username !== payrollUsername || password !== payrollPassword) {
        return res.status(401).json({
            success: false,
            message: "Incorrect payroll username or password."
        });
    }

    next();
});

// Log incoming requests
app.use((req, res, next) => {
    console.log(req.method, req.url);
    next();
});

// Get MongoDB connection string
const mongoURI = process.env.MONGODB_URI;

if (!mongoURI) {
    console.error("ERROR: MONGODB_URI is missing from .env");
    process.exit(1);
}

// Create MongoDB client
const client = new MongoClient(mongoURI);

// Database reference
let database;

// Connect to MongoDB
async function connectToDatabase() {
    try {

        await client.connect();

        database = client.db("PayrollDatabase");

        console.log("Successfully connected to MongoDB!");

    } catch (error) {

        console.error("Could not connect to MongoDB:");
        console.error(error);

        process.exit(1);
    }
}

// Test route
app.get("/api/test", (req, res) => {

    res.json({
        success: true,
        message: "Payroll Server is running!"
    });
});

// Get all employees
app.get("/api/employees", async (req, res) => {

    try {

        const employees =
            await database
                .collection("employees")
                .find({})
                .toArray();

        res.json(employees);

    } catch (error) {

        console.error("Error getting employees:");
        console.error(error);

        res.status(500).json({
            success: false,
            message: "Could not retrieve employees."
        });
    }
});

// Replace every cloud employee record with the employees stored in one backup file.
app.post("/api/backups/restore", async (req, res) => {

    const restoredEmployees = req.body;

    if (!Array.isArray(restoredEmployees) || restoredEmployees.length === 0) {
        return res.status(400).json({
            success: false,
            message: "A backup must contain at least one employee."
        });
    }

    if (restoredEmployees.some(employee =>
            !employee || typeof employee !== "object" || Array.isArray(employee))) {

        return res.status(400).json({
            success: false,
            message: "The backup file has an invalid employee record."
        });
    }

    // Restored employees receive new MongoDB IDs. Old IDs must never be inserted again.
    const employeesToRestore = restoredEmployees.map(({ _id, mongoId, ...employee }) => employee);
    const session = client.startSession();

    try {

        // A transaction means MongoDB restores every employee or leaves the old data untouched.
        await session.withTransaction(async () => {
            const employees = database.collection("employees");

            await employees.deleteMany({}, { session });
            await employees.insertMany(employeesToRestore, { session });
        });

        console.log("Restored payroll backup with", employeesToRestore.length, "employees.");

        res.json({
            success: true,
            restoredEmployees: employeesToRestore.length
        });

    } catch (error) {

        console.error("Error restoring payroll backup:");
        console.error(error);

        res.status(500).json({
            success: false,
            message: "Could not restore the backup."
        });

    } finally {
        await session.endSession();
    }
});

// Add an employee
app.post("/api/employees", async (req, res) => {

    try {

        const employee = req.body;

        if (
            !employee ||
            Object.keys(employee).length === 0
        ) {

            return res.status(400).json({
                success: false,
                message: "Employee data is required."
            });
        }

        const result =
            await database
                .collection("employees")
                .insertOne(employee);

        console.log(
            "Added employee:",
            result.insertedId.toString(),
            employee.name
        );

        res.status(201).json({
            success: true,
            message: "Employee added successfully.",
            employeeId: result.insertedId
        });

    } catch (error) {

        console.error("Error adding employee:");
        console.error(error);

        res.status(500).json({
            success: false,
            message: "Could not add employee."
        });
    }
});

// Update an employee
app.put("/api/employees/:id", async (req, res) => {

    try {

        const employeeId = req.params.id;

        // Make sure the ID is a valid MongoDB ObjectId
        if (!ObjectId.isValid(employeeId)) {

            return res.status(400).json({
                success: false,
                message: "Invalid employee ID."
            });
        }

        const employee = req.body;

        if (
            !employee ||
            Object.keys(employee).length === 0
        ) {

            return res.status(400).json({
                success: false,
                message: "Employee data is required."
            });
        }

        // Don't replace MongoDB's _id field
        delete employee._id;

        const result =
            await database
                .collection("employees")
                .replaceOne(
                    {
                        _id: new ObjectId(employeeId)
                    },
                    employee
                );

        console.log(
            "Update result:",
            result
        );

        console.log(
            "Updated employee:",
            employeeId,
            employee.name
        );

        if (result.matchedCount === 0) {

            return res.status(404).json({
                success: false,
                message: "Employee not found."
            });
        }

        res.json({
            success: true,
            message: "Employee updated successfully."
        });

	} catch (error) {

		console.error("Error updating employee:");
		console.error(error);

		res.status(500).json({
			success: false,
			message: error.message
		});
	}
});

// Delete an employee
app.delete("/api/employees/:id", async (req, res) => {

    try {

        const employeeId = req.params.id;

        // Make sure the ID is a valid MongoDB ObjectId
        if (!ObjectId.isValid(employeeId)) {

            return res.status(400).json({
                success: false,
                message: "Invalid employee ID."
            });
        }

        const result =
            await database
                .collection("employees")
                .deleteOne({
                    _id: new ObjectId(employeeId)
                });

        console.log(
            "Delete result:",
            result
        );

        if (result.deletedCount === 0) {

            return res.status(404).json({
                success: false,
                message: "Employee not found."
            });
        }

        console.log(
            "Deleted employee:",
            employeeId
        );

        res.json({
            success: true,
            message: "Employee deleted successfully."
        });

    } catch (error) {

        console.error("Error deleting employee:");
        console.error(error);

        res.status(500).json({
            success: false,
            message: "Could not delete employee."
        });
    }
});

// Set server port
const PORT = process.env.PORT || 3000;

// Start server
async function startServer() {

    await connectToDatabase();

    app.listen(PORT, "0.0.0.0", () => {

        console.log(
            `Payroll Server running on port ${PORT}`
        );

        console.log(
            `http://localhost:${PORT}`
        );
    });
}

app.get("/api/version", (req, res) => {

    res.json({
        version: "1.3"
    });

});

// Start application
startServer();
