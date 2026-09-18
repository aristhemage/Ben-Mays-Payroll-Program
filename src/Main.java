import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        // Start the program here so the loading window opens and works properly.
        SwingUtilities.invokeLater(() -> {

            if (!showLoginWindow()) {
                System.exit(0);
                return;
            }

            JFrame loadingWindow = createLoadingWindow();
            loadingWindow.setVisible(true);

            loadPayrollDataInBackground(loadingWindow);

        });

    }

    /** Asks for the payroll login before the program downloads any cloud data. */
    private static boolean showLoginWindow() {

        JDialog loginWindow = new JDialog(
                (JFrame) null,
                "Payroll Manager Login",
                true
        );

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.setBorder(new EmptyBorder(18, 20, 18, 20));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JLabel statusLabel = new JLabel(" ");
        JButton loginButton = new JButton("Log In");
        JButton cancelButton = new JButton("Cancel");
        boolean[] loginConfirmed = {false};

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.add(cancelButton);
        buttonPanel.add(loginButton);

        JPanel windowContents = new JPanel(new BorderLayout(8, 12));
        windowContents.add(panel, BorderLayout.CENTER);
        windowContents.add(statusLabel, BorderLayout.NORTH);
        windowContents.add(buttonPanel, BorderLayout.SOUTH);

        loginWindow.setContentPane(windowContents);
        loginWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        loginWindow.setResizable(false);
        loginWindow.pack();
        loginWindow.setSize(360, loginWindow.getHeight());
        loginWindow.setLocationRelativeTo(null);

        cancelButton.addActionListener(e -> loginWindow.dispose());

        loginButton.addActionListener(e -> {

            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setForeground(new Color(170, 30, 30));
                statusLabel.setText("Enter both a username and password.");
                return;
            }

            // Keep the login window responsive while the server confirms the credentials.
            loginButton.setEnabled(false);
            cancelButton.setEnabled(false);
            statusLabel.setForeground(new Color(50, 80, 130));
            statusLabel.setText("Checking login...");

            PayrollAPI.setCredentials(username, password);

            Thread loginChecker = new Thread(() -> {
                try {

                    // The protected version request confirms the login without downloading payroll records.
                    PayrollAPI.getLatestVersion();

                    SwingUtilities.invokeLater(() -> {
                        loginConfirmed[0] = true;
                        loginWindow.dispose();
                    });

                } catch (Exception error) {

                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setForeground(new Color(170, 30, 30));

                        if ("Incorrect username or password.".equals(error.getMessage())) {
                            statusLabel.setText("Username or password is incorrect.");
                        } else {
                            statusLabel.setText("Could not reach the server. Please try again.");
                        }

                        passwordField.setText("");
                        loginButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                    });
                }
            }, "Payroll Login Checker");

            loginChecker.setDaemon(true);
            loginChecker.start();
        });

        passwordField.addActionListener(e -> loginButton.doClick());

        loginWindow.setVisible(true);

        return loginConfirmed[0];
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

                // Do not load payroll data until this installed copy matches the version approved on the server.
                String latestVersion = PayrollAPI.getLatestVersion();

                if (!AppVersion.VERSION.equals(latestVersion)) {
                    SwingUtilities.invokeLater(() -> showRequiredUpdateAndExit(
                            loadingWindow,
                            latestVersion
                    ));

                    return;
                }

                // Download the employees from the database and put them into the program.
                EmployeeManager employeeManager = createEmployeeManager();

                SwingUtilities.invokeLater(() -> {

                    // The data is ready, so close the loading screen and open the main program.
                    loadingWindow.dispose();
                    new PayrollGUI(employeeManager);
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

    /** Stops an outdated copy before it can view or change shared payroll data. */
    private static void showRequiredUpdateAndExit(JFrame loadingWindow, String latestVersion) {

        loadingWindow.dispose();

        JOptionPane.showMessageDialog(
                null,
                "An update is required before Payroll Manager can be used.\n\n"
                        + "Your version: " + AppVersion.VERSION + "\n"
                        + "Required version: " + latestVersion + "\n\n"
                        + "Please contact Jacob for the latest version.",
                "Update Required",
                JOptionPane.ERROR_MESSAGE
        );

        System.exit(0);
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

}
