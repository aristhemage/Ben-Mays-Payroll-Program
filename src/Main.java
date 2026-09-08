import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame loadingWindow = createLoadingWindow();
            loadingWindow.setVisible(true);

            loadPayrollDataInBackground(loadingWindow);

        });

    }

    // Shows a window immediately so the user knows the app is working.
    private static JFrame createLoadingWindow() {

        JFrame window = new JFrame("Payroll Manager");

        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.setSize(390, 150);
        window.setResizable(false);
        window.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 12));
        panel.setBorder(new EmptyBorder(22, 25, 22, 25));

        JLabel message = new JLabel("Loading payroll data from the server...");
        message.setHorizontalAlignment(SwingConstants.CENTER);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        panel.add(message, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.SOUTH);

        window.setContentPane(panel);

        return window;
    }

    // Loads MongoDB data away from the screen thread so the loading window stays responsive.
    private static void loadPayrollDataInBackground(JFrame loadingWindow) {

        Thread loader = new Thread(() -> {

            try {

                EmployeeManager employeeManager = createEmployeeManager();

                SwingUtilities.invokeLater(() -> {

                    loadingWindow.dispose();
                    new PayrollGUI(employeeManager);
                    checkForUpdatesInBackground();
                });

            } catch (Exception error) {

                error.printStackTrace();

                SwingUtilities.invokeLater(() -> {

                    loadingWindow.dispose();

                    JOptionPane.showMessageDialog(
                            null,
                            "Payroll data could not be loaded.\n\n"
                                    + "Please check your internet connection and try again.\n\n"
                                    + "Details: " + error.getMessage(),
                            "Unable to Start Payroll Manager",
                            JOptionPane.ERROR_MESSAGE
                    );
                });
            }
        }, "Payroll Data Loader");

        loader.setDaemon(true);
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
                            "No Employees",
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
        versionChecker.start();
    }

}
