
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

    private static final String API_URL = "http://localhost:3000/api";

    private static final Gson gson = new Gson();


    // =========================
    // GET EMPLOYEES
    // =========================

    public static ArrayList<Employee> getEmployees() throws IOException {

        URL url =
                URI.create(API_URL + "/employees").toURL();

        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        int responseCode =
                connection.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_OK) {

            throw new IOException(
                    "Server returned HTTP " +
                            responseCode
            );
        }

        StringBuilder response =
                new StringBuilder();

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

        ArrayList<Employee> employees =
                new ArrayList<>();

        JsonArray employeeArray =
                JsonParser
                        .parseString(json)
                        .getAsJsonArray();

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
            employee.fed_rate = "0";
        }
    }


    // =========================
    // FILL ARRAY
    // =========================

    private static void fillArray(
            String[] array
    ) {

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

        URL url =
                URI.create(
                        API_URL + "/employees"
                ).toURL();

        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();

        connection.setRequestMethod("POST");

        connection.setRequestProperty(
                "Content-Type",
                "application/json"
        );

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        connection.setDoOutput(true);

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

        connection.setRequestProperty(
                "Content-Type",
                "application/json"
        );

        connection.setRequestProperty(
                "Accept",
                "application/json"
        );

        connection.setDoOutput(true);

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
    }
}


