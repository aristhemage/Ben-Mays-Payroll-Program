// Run this file once from your own computer to create or change a payroll login.
require("dotenv").config();

const crypto = require("crypto");
const { MongoClient } = require("mongodb");

const username = process.env.PAYROLL_LOGIN_USERNAME;
const password = process.env.PAYROLL_LOGIN_PASSWORD;
const mongoURI = process.env.MONGODB_URI;

if (!username || !password || !mongoURI) {
    console.error("Set PAYROLL_LOGIN_USERNAME, PAYROLL_LOGIN_PASSWORD, and MONGODB_URI before running this file.");
    process.exit(1);
}

const salt = crypto.randomBytes(16).toString("hex");
const passwordHash = crypto.scryptSync(password, salt, 64).toString("hex");
const client = new MongoClient(mongoURI);

async function createPayrollUser() {
    try {
        await client.connect();

        await client.db("PayrollDatabase").collection("payroll_users").updateOne(
                { username },
                {
                    $set: {
                        username,
                        passwordSalt: salt,
                        passwordHash,
                        updatedAt: new Date()
                    }
                },
                { upsert: true }
        );

        console.log("Payroll login saved for:", username);

    } finally {
        await client.close();
    }
}

createPayrollUser().catch(error => {
    console.error("Could not save the payroll login:", error);
    process.exit(1);
});
