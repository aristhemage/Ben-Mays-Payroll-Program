import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        // Start the program here so the loading window opens and works properly.
        SwingUtilities.invokeLater(() -> {

            JFrame loadingWindow = createLoadingWindow();
            loadingWindow.setVisible(true);

            loadPayrollDataInBackground(loadingWindow);

        });

    }

    // Shows a window immediately so the user knows the app is working. Abby has no patience and spams open file.
    private static JFrame createLoadingWindow() {

        JFrame window = new JFrame("Payroll Manager");

        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.setSize(440, 175);
        window.setResizable(false);
        window.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 12));
        panel.setBorder(new EmptyBorder(22, 25, 22, 25));

        JLabel message = new JLabel(
                "<html><div style='text-align: center;'>"
                        + "Loading payroll data from the server...<br>"
                        + "<strong><em>Warning:</strong> This can take up to one minute.</em>"
                        + "</div></html>"
        );
        message.setHorizontalAlignment(SwingConstants.CENTER);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        panel.add(message, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.SOUTH);

        window.setContentPane(panel);

        return window;
    }

    // This lets the loading screen stay visible instead of freezing when trying to load data.
    private static void loadPayrollDataInBackground(JFrame loadingWindow) {

        Thread loader = new Thread(() -> {
            try {
                // Download the employees from the database and put them into the program.
                EmployeeManager employeeManager = createEmployeeManager();

                SwingUtilities.invokeLater(() -> {

                    // The data is ready, so close the loading screen and open the main program.
                    loadingWindow.dispose();
                    new PayrollGUI(employeeManager);

                    // Check for a newer version without interrupting the user.
                    checkForUpdatesInBackground();
                });

            } catch (Exception error) {

                // Print the technical error for troubleshooting.
                error.printStackTrace();

                SwingUtilities.invokeLater(() -> {

                    // Close the loading screen and explain why the program could not open.
                    loadingWindow.dispose();

                    JOptionPane.showMessageDialog(
                            null,
                            "Payroll data could not be loaded.\n\n"
                                    + "Warning: the cloud server may be waking up after being idle.\n"
                                    + "Please wait one minute, then try again.\n\n"
                                    + "Also check your internet connection.\n\n"
                                    + "Details: " + error.getMessage(),
                            "Unable to Start Payroll Manager :(",
                            JOptionPane.ERROR_MESSAGE
                    );
                });
            }
        }, "Payroll Data Loader");

        // If the user closes the program while data is loading, do not keep it open.
        loader.setDaemon(true);

        // Begin loading the database information.
        loader.start();
    }

    // Creates the app's employee list from the data returned by the server.
    private static EmployeeManager createEmployeeManager() throws IOException {

        ArrayList<Employee> employees = PayrollAPI.getEmployees();
        EmployeeManager employeeManager = new EmployeeManager();

        for (Employee employee : employees) {
            employeeManager.addEmployee(employee);
        }

        // Keep one blank employee available when the database has no employees yet.
        if (employeeManager.getEmployeeCount() == 0) {

            employeeManager.addEmployee(
                    new Employee(
                            "No Employees (This should not happen!)",
                            "",
                            "",
                            ""
                    )
            );
        }

        return employeeManager;
    }

    // Checks for updates after the main payroll window is already open.
    private static void checkForUpdatesInBackground() {

        Thread versionChecker = new Thread(() -> {

            try {

                String latestVersion = PayrollAPI.getLatestVersion();

                if (!AppVersion.VERSION.equals(latestVersion)) {

                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                            null,
                            "A newer version of Payroll Manager is available.\n\n" +
                                    "Your version: " + AppVersion.VERSION + "\n" +
                                    "Latest version: " + latestVersion + "\n\n" +
                                    "Please contact Jacob for the latest version.",
                            "Update Available",
                            JOptionPane.WARNING_MESSAGE
                    ));
                }

            } catch (IOException error) {

                // A version-check failure should never interrupt the payroll app.
                System.out.println("Could not check for program updates.");
                error.printStackTrace();
            }
        }, "Payroll Version Checker");

        versionChecker.setDaemon(true);
        // Run independently so a slow version check never delays payroll work.
        versionChecker.start();
    }

}
