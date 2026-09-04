
// Load environment variables from .env
require("dotenv").config();

const express = require("express");
const cors = require("cors");
const { MongoClient, ObjectId } = require("mongodb");

// Create Express application
const app = express();

// Enable CORS
app.use(cors());

// Allow JSON request bodies
app.use(express.json());

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
            message: "Could not update employee."
        });
    }
});

// Set server port
const PORT = process.env.PORT || 3000;

// Start server
async function startServer() {

    await connectToDatabase();

    app.listen(PORT, () => {

        console.log(
            `Payroll Server running on port ${PORT}`
        );

        console.log(
            `http://localhost:${PORT}`
        );
    });
}

// Start application
startServer();
