import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            try {

                ArrayList<Employee> employees =
                        PayrollAPI.getEmployees();

                EmployeeManager employee_manager =
                        new EmployeeManager();

                for (Employee employee : employees) {
                    employee_manager.addEmployee(employee);
                }

                // Make sure the application has an employee
                if (employee_manager.getEmployeeCount() == 0) {

                    employee_manager.addEmployee(
                            new Employee(
                                    "No Employees",
                                    "",
                                    "",
                                    ""
                            )
                    );
                }
                checkForUpdates();
                new PayrollGUI(employee_manager);

            } catch (Exception e) {

                e.printStackTrace();

            }

        });

    }

    private static void checkForUpdates() {

        try {

            String latestVersion =
                    PayrollAPI.getLatestVersion();

            if (!AppVersion.VERSION.equals(latestVersion)) {

                JOptionPane.showMessageDialog(
                        null,
                        "A newer version of Payroll Manager is available.\n\n" +
                                "Your version: " +
                                AppVersion.VERSION +
                                "\n" +
                                "Latest version: " +
                                latestVersion +
                                "\n\n" +
                                "Please contact Jacob for the latest version.",
                        "Update Available",
                        JOptionPane.WARNING_MESSAGE
                );
            }

        } catch (IOException error) {

            // Don't prevent the payroll program from opening
            // just because the version server is unavailable.

            System.out.println(
                    "Could not check for program updates."
            );

            error.printStackTrace();
        }
    }


}
