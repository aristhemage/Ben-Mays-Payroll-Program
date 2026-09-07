
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class PayrollAPI {

    // The online server that the payroll app talks to.
    private static final String API_URL = "https://ben-mays-payroll-server.onrender.com/api";

    // Converts between Java employee data and JSON text for the server.
    private static final Gson gson = new Gson();


    // =========================
    // GET EMPLOYEES
    // =========================

    public static ArrayList<Employee> getEmployees() throws IOException {

        // Ask the server for every employee saved in MongoDB.
        URL url =
                URI.create(API_URL + "/employees").toURL();

        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");

        // Tell the server that this app expects JSON data back.
        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        int responseCode =
                connection.getResponseCode();

        // Stop and report an error if the server did not answer successfully.
        if (responseCode != HttpURLConnection.HTTP_OK) {

            throw new IOException(
                    "Server returned HTTP " +
                            responseCode
            );
        }

        StringBuilder response =
                new StringBuilder();

        // Read the server's JSON reply into one piece of text.
        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        connection.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        connection.disconnect();

        // Turn the JSON reply into employee objects that the app can use.
        return parseEmployees(
                response.toString()
        );
    }


    // =========================
    // PARSE EMPLOYEES
    // =========================

    private static ArrayList<Employee> parseEmployees(
            String json
    ) {

        // Make a list to hold the employees received from the server.
        ArrayList<Employee> employees =
                new ArrayList<>();

        JsonArray employeeArray =
                JsonParser
                        .parseString(json)
                        .getAsJsonArray();

        // Convert each employee record from JSON into a Java Employee object.
        for (int i = 0;
             i < employeeArray.size();
             i++) {

            JsonObject data =
                    employeeArray
                            .get(i)
                            .getAsJsonObject();

            Employee employee =
                    gson.fromJson(
                            data,
                            Employee.class
                    );

            // Keep MongoDB's unique record ID so this employee can be saved or deleted later.
            if (data.has("_id")) {
                employee.mongoId =
                        data.get("_id").getAsString();
            }

            System.out.println(
                    "Loaded employee: " +
                            employee.name +
                            " | MongoDB ID: " +
                            employee.mongoId
            );

            // Fill in any missing payroll fields so older records do not break the app.
            initializeEmployeeData(employee);

            employees.add(employee);
        }

        return employees;
    }


    // =========================
    // INITIALIZE EMPLOYEE DATA
    // =========================

    private static void initializeEmployeeData(
            Employee employee
    ) {

        // Create blank 26-pay-period fields when a saved employee does not have them.
        if (employee.hours == null) {

            employee.hours =
                    new String[26];

            fillArray(employee.hours);
        }

        if (employee.ot_hours == null) {

            employee.ot_hours =
                    new String[26];

            fillArray(employee.ot_hours);
        }

        if (employee.hourly_rates == null) {

            employee.hourly_rates =
                    new String[26];

            fillArray(employee.hourly_rates);
        }

        if (employee.hourly_rate_changed_on == null) {

            employee.hourly_rate_changed_on =
                    new String[26];

            fillArray(
                    employee.hourly_rate_changed_on
            );
        }

        if (employee.ot_rates == null) {

            employee.ot_rates =
                    new String[26];

            fillArray(employee.ot_rates);
        }

        if (employee.ot_rate_changed_on == null) {

            employee.ot_rate_changed_on =
                    new String[26];

            fillArray(
                    employee.ot_rate_changed_on
            );
        }

        if (employee.extra == null) {

            employee.extra =
                    new String[26];

            fillArray(employee.extra);
        }

        if (employee.fed_rate == null) {
            // Use zero when no federal tax rate was saved.
            employee.fed_rate = "0";
        }
    }


    // =========================
    // FILL ARRAY
    // =========================

    private static void fillArray(
            String[] array
    ) {

        // Start every pay-period slot as blank instead of missing.
        for (int i = 0;
             i < array.length;
             i++) {

            array[i] = "";
        }
    }


    // =========================
    // ADD EMPLOYEE
    // =========================

    public static String addEmployee(
            Employee employee
    ) throws IOException {

        // Ask the server to create a new employee record in MongoDB.
        URL url =
                URI.create(
                        API_URL + "/employees"
                ).toURL();

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        connection.setRequestMethod("POST");

        // Tell the server that the employee data is being sent as JSON.
        connection.setRequestProperty(
                "Content-Type",
                "application/json"
        );

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        connection.setDoOutput(true);

        // Convert the new employee into JSON text before sending it.
        String json =
                gson.toJson(employee);

        try (
                var outputStream =
                        connection.getOutputStream()
        ) {

            outputStream.write(
                    json.getBytes(
                            StandardCharsets.UTF_8
                    )
            );
        }

        int responseCode =
                connection.getResponseCode();

        // A newly created record must return HTTP 201.
        if (
                responseCode !=
                        HttpURLConnection.HTTP_CREATED
        ) {

            throw new IOException(
                    "Server returned HTTP " +
                            responseCode
            );
        }

        StringBuilder response =
                new StringBuilder();

        // Read the server reply containing the new MongoDB record ID.
        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        connection.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String line;

            while (
                    (line = reader.readLine()) != null
            ) {

                response.append(line);
            }
        }

        connection.disconnect();

        JsonObject result =
                JsonParser
                        .parseString(
                                response.toString()
                        )
                        .getAsJsonObject();

        // Return the new ID so the desktop app remembers which record belongs to this employee.
        return result
                .get("employeeId")
                .getAsString();
    }


    // =========================
    // UPDATE EMPLOYEE
    // =========================

    public static void updateEmployee(
            String employeeId,
            Employee employee
    ) throws IOException {

        // Ask the server to replace this employee's existing MongoDB record.
        URL url =
                URI.create(
                        API_URL +
                                "/employees/" +
                                employeeId
                ).toURL();

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        connection.setRequestMethod("PUT");

        // Tell the server that the updated employee is being sent as JSON.
        connection.setRequestProperty(
                "Content-Type",
                "application/json"
        );

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        connection.setDoOutput(true);

        // Convert the changed employee into JSON text before sending it.
        String json =
                gson.toJson(employee);

        try (
                var outputStream =
                        connection.getOutputStream()
        ) {

            outputStream.write(
                    json.getBytes(
                            StandardCharsets.UTF_8
                    )
            );
        }

        int responseCode =
                connection.getResponseCode();

        // Read the server's detailed error message when the save did not work.
        if (
                responseCode !=
                        HttpURLConnection.HTTP_OK
        ) {

            StringBuilder errorResponse =
                    new StringBuilder();

            try (
                    BufferedReader reader =
                            new BufferedReader(
                                    new InputStreamReader(
                                            connection.getErrorStream(),
                                            StandardCharsets.UTF_8
                                    )
                            )
            ) {

                String line;

                while (
                        (line = reader.readLine()) != null
                ) {

                    errorResponse.append(line);
                }
            }

            connection.disconnect();

            throw new IOException(
                    "Server returned HTTP " +
                            responseCode +
                            ": " +
                            errorResponse
            );
        }

        connection.disconnect();
    }

    // =========================
    // DELETE EMPLOYEE
    // =========================

    public static void deleteEmployee(
            String employeeId
    ) throws IOException {

        // Ask the server to permanently remove this employee's MongoDB record.
        URL url =
                URI.create(
                        API_URL +
                                "/employees/" +
                                employeeId
                ).toURL();

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        connection.setRequestMethod("DELETE");

        // This request does not send employee data; the ID in the address identifies the record.
        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        int responseCode =
                connection.getResponseCode();

        // Only continue if the server confirmed the deletion.
        if (
                responseCode !=
                        HttpURLConnection.HTTP_OK
        ) {

            throw new IOException(
                    "Server returned HTTP " +
                            responseCode
            );
        }

        connection.disconnect();

        System.out.println(
                "Employee deleted from MongoDB: " +
                        employeeId
        );
    }
    // =========================
// CHECK PROGRAM VERSION
// =========================

    public static String getLatestVersion() throws IOException {

        // Ask the server which version of the payroll app is current.
        URL url =
                URI.create(
                        API_URL + "/version"
                ).toURL();

        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");

        // Tell the server that this app expects JSON data back.
        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        int responseCode =
                connection.getResponseCode();

        // Stop and report an error if the version check did not succeed.
        if (responseCode != HttpURLConnection.HTTP_OK) {

            throw new IOException(
                    "Server returned HTTP " +
                            responseCode
            );
        }

        StringBuilder response =
                new StringBuilder();

        // Read the JSON version reply from the server.
        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        connection.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        connection.disconnect();

        JsonObject result =
                JsonParser
                        .parseString(
                                response.toString()
                        )
                        .getAsJsonObject();

        // Return only the version number from the server reply.
        return result
                .get("version")
                .getAsString();
    }
}


