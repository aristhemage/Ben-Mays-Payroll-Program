import javax.swing.*;
import java.awt.*;

public class PayrollGUI {

    private final String version = "Alpha 1";

    private final EmployeeManager employeeManager;

    private final JFrame frame;
    private final JComboBox<String> employeeSelector;
    private final JLabel employeeNameLabel;

    private final PayrollTableManager tableManager;

    private boolean changingEmployee = false;

    // =========================
    // CONSTRUCTOR
    // =========================

    public PayrollGUI(EmployeeManager employeeManager) {

        this.employeeManager = employeeManager;

        // =========================
        // FRAME
        // =========================

        frame = new JFrame("Payroll System " + version);

        frame.setSize(1200, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // =========================
        // MANAGERS
        // =========================

        tableManager = new PayrollTableManager(frame);

        PayrollTotalsManager totalsManager = new PayrollTotalsManager(
                frame,
                employeeManager,
                tableManager
        );

        // =========================
        // TOP PANEL
        // =========================

        JPanel topPanel =
                new JPanel(new BorderLayout());

        JPanel employeeControlsPanel =
                new JPanel();

        employeeNameLabel =
                new JLabel("Current Employee:");

        employeeSelector =
                new JComboBox<>();

        JButton previousButton =
                new JButton("< Previous");

        JButton nextButton =
                new JButton("Next >");

        JButton addEmployeeButton =
                new JButton("Add Employee");

        JButton removeEmployeeButton =
                new JButton("Remove Employee");

        employeeControlsPanel.add(
                employeeNameLabel
        );

        employeeControlsPanel.add(
                employeeSelector
        );

        employeeControlsPanel.add(
                previousButton
        );

        employeeControlsPanel.add(
                nextButton
        );

        employeeControlsPanel.add(
                addEmployeeButton
        );

        employeeControlsPanel.add(
                removeEmployeeButton
        );

        topPanel.add(
                employeeControlsPanel,
                BorderLayout.WEST
        );

        topPanel.add(
                tableManager.getFedRatePanel(),
                BorderLayout.EAST
        );

        frame.add(
                topPanel,
                BorderLayout.NORTH
        );

        // =========================
        // TABLE
        // =========================

        JScrollPane scrollPane =
                new JScrollPane(
                        tableManager.getTable()
                );

        frame.add(
                scrollPane,
                BorderLayout.CENTER
        );

        // =========================
        // BOTTOM PANEL
        // =========================

        JPanel bottomPanel =
                new JPanel();

        JButton changeHourlyRateButton =
                new JButton(
                        "Mass Change Hourly Rate"
                );

        JButton changeOTRateButton =
                new JButton(
                        "Mass Change OT Rate"
                );

        JButton viewTotalsButton =
                new JButton(
                        "View Totals"
                );

        bottomPanel.add(
                changeHourlyRateButton
        );

        bottomPanel.add(
                changeOTRateButton
        );

        bottomPanel.add(
                viewTotalsButton
        );

        frame.add(
                bottomPanel,
                BorderLayout.SOUTH
        );

        // =========================
        // BUTTON EVENTS
        // =========================

        setupEmployeeSelector();

        setupPreviousButton(
                previousButton
        );

        setupNextButton(
                nextButton
        );

        setupAddEmployeeButton(
                addEmployeeButton
        );

        setupRemoveEmployeeButton(
                removeEmployeeButton
        );

        setupHourlyRateButton(
                changeHourlyRateButton
        );

        setupOTRateButton(
                changeOTRateButton
        );

        totalsManager.setupViewTotalsButton(
                viewTotalsButton
        );

        // =========================
        // INITIAL SETUP
        // =========================

        updateEmployeeSelector();

        loadCurrentEmployee();

        frame.setLocationRelativeTo(null);

        frame.setVisible(true);
    }

    // =========================
    // EMPLOYEE SELECTOR
    // =========================

    private void setupEmployeeSelector() {

        employeeSelector.addActionListener(e -> {

            if (changingEmployee) {
                return;
            }

            int selectedIndex =
                    employeeSelector.getSelectedIndex();

            if (selectedIndex >= 0 &&
                    selectedIndex !=
                            employeeManager.getCurrentEmployeeIndex()) {

                switchEmployee(
                        selectedIndex
                );
            }
        });
    }

    // =========================
    // PREVIOUS BUTTON
    // =========================

    private void setupPreviousButton(
            JButton button) {

        button.addActionListener(e -> {

            int current =
                    employeeManager
                            .getCurrentEmployeeIndex();

            if (current > 0) {

                employeeSelector.setSelectedIndex(
                        current - 1
                );
            }
        });
    }

    // =========================
    // NEXT BUTTON
    // =========================

    private void setupNextButton(
            JButton button) {

        button.addActionListener(e -> {

            int current =
                    employeeManager
                            .getCurrentEmployeeIndex();

            if (current <
                    employeeManager.getEmployeeCount() - 1) {

                employeeSelector.setSelectedIndex(
                        current + 1
                );
            }
        });
    }

    // =========================
    // ADD EMPLOYEE
    // =========================

    private void setupAddEmployeeButton(
            JButton button) {

        button.addActionListener(e -> {

            String name =
                    JOptionPane.showInputDialog(
                            frame,
                            "Enter employee name:"
                    );

            if (name == null ||
                    name.trim().isEmpty()) {

                return;
            }

            saveCurrentEmployee();

            employeeManager.addEmployee(
                    new Employee(
                            name.trim()
                    )
            );

            employeeManager.setCurrentEmployeeIndex(
                    employeeManager.getEmployeeCount() - 1
            );

            changingEmployee = true;

            updateEmployeeSelector();

            employeeSelector.setSelectedIndex(
                    employeeManager.getCurrentEmployeeIndex()
            );

            changingEmployee = false;

            loadCurrentEmployee();
        });
    }

    // =========================
    // REMOVE EMPLOYEE
    // =========================

    private void setupRemoveEmployeeButton(
            JButton button) {

        button.addActionListener(e -> {

            if (employeeManager.getEmployeeCount() <= 1) {

                JOptionPane.showMessageDialog(
                        frame,
                        "You must have at least one employee."
                );

                return;
            }

            String employeeName =
                    employeeManager
                            .getCurrentEmployee()
                            .name;

            int choice =
                    JOptionPane.showConfirmDialog(
                            frame,
                            "Remove " +
                                    employeeName +
                                    "? (THIS ACTION CAN NOT BE UNDONE!)",
                            "Remove Employee",
                            JOptionPane.YES_NO_OPTION
                    );

            if (choice !=
                    JOptionPane.YES_OPTION) {

                return;
            }

            saveCurrentEmployee();

            employeeManager.removeCurrentEmployee();

            changingEmployee = true;

            updateEmployeeSelector();

            employeeSelector.setSelectedIndex(
                    employeeManager
                            .getCurrentEmployeeIndex()
            );

            changingEmployee = false;

            loadCurrentEmployee();
        });
    }

    // =========================
    // HOURLY RATE BUTTON
    // =========================

    private void setupHourlyRateButton(
            JButton button) {

        button.addActionListener(e -> {

            tableManager.stopEditing();

            int row =
                    tableManager
                            .getTable()
                            .getSelectedRow();

            if (row == -1) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Please select a pay period first."
                );

                return;
            }

            String newRate =
                    JOptionPane.showInputDialog(
                            frame,
                            "Enter the new hourly rate for the rest of the rows:"
                    );

            if (newRate == null ||
                    newRate.trim().isEmpty()) {

                return;
            }

            try {

                double rate =
                        Double.parseDouble(
                                newRate.trim()
                        );

                if (rate < 0) {

                    JOptionPane.showMessageDialog(
                            frame,
                            "Hourly rate cannot be negative."
                    );

                    return;
                }

                tableManager.changeHourlyRate(
                        row,
                        newRate
                );

                saveCurrentEmployee();

            } catch (NumberFormatException error) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Please enter a valid number for the hourly rate."
                );
            }
        });
    }

    // =========================
    // OT RATE BUTTON
    // =========================

    private void setupOTRateButton(
            JButton button) {

        button.addActionListener(e -> {

            tableManager.stopEditing();

            int row =
                    tableManager
                            .getTable()
                            .getSelectedRow();

            if (row == -1) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Please select a pay period first."
                );

                return;
            }

            String newRate =
                    JOptionPane.showInputDialog(
                            frame,
                            "Enter the new OT rate for the rest of the rows:"
                    );

            if (newRate == null ||
                    newRate.trim().isEmpty()) {

                return;
            }

            try {

                double rate =
                        Double.parseDouble(
                                newRate.trim()
                        );

                if (rate < 0) {

                    JOptionPane.showMessageDialog(
                            frame,
                            "OT rate cannot be negative."
                    );

                    return;
                }

                tableManager.changeOTRate(
                        row,
                        newRate
                );

                saveCurrentEmployee();

            } catch (NumberFormatException error) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Please enter a valid number for the OT rate."
                );
            }
        });
    }

    // =========================
    // SWITCH EMPLOYEE
    // =========================

    private void switchEmployee(
            int newEmployeeIndex) {

        saveCurrentEmployee();

        employeeManager.setCurrentEmployeeIndex(
                newEmployeeIndex
        );

        loadCurrentEmployee();
    }

    // =========================
    // LOAD CURRENT EMPLOYEE
    // =========================

    private void loadCurrentEmployee() {

        Employee employee =
                employeeManager.getCurrentEmployee();

        employeeNameLabel.setText(
                "Current Employee: "
        );

        tableManager.loadEmployee(
                employee
        );
    }

    // =========================
    // SAVE CURRENT EMPLOYEE
    // =========================

    private void saveCurrentEmployee() {

        tableManager.saveEmployee(
                employeeManager.getCurrentEmployee()
        );
    }

    // =========================
    // UPDATE SELECTOR
    // =========================

    private void updateEmployeeSelector() {

        employeeSelector.removeAllItems();

        for (Employee employee :
                employeeManager.getEmployees()) {

            employeeSelector.addItem(
                    employee.name
            );
        }
    }
}