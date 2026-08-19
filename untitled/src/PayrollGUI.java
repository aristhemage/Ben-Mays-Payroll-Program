import javax.swing.*;
import java.awt.*;
import java.util.Map;

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

    public PayrollGUI(
            EmployeeManager employeeManager
    ) {

        this.employeeManager =
                employeeManager;


        // =========================
        // FRAME
        // =========================

        frame =
                new JFrame(
                        "Payroll System " +
                                version
                );

        frame.setSize(
                1200,
                700
        );

        frame.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        frame.setLayout(
                new BorderLayout()
        );


        // =========================
        // MANAGERS
        // =========================

        tableManager =
                new PayrollTableManager(
                        frame
                );


        PayrollTotalsManager totalsManager =
                new PayrollTotalsManager(
                        frame,
                        employeeManager,
                        tableManager
                );


        // =========================
        // TOP PANEL
        // =========================

        JPanel topPanel =
                new JPanel(
                        new BorderLayout()
                );


        JPanel employeeControlsPanel =
                new JPanel();


        employeeNameLabel =
                new JLabel(
                        "Current Employee:"
                );


        employeeSelector =
                new JComboBox<>();


        JButton previousButton =
                new JButton(
                        "< Previous"
                );


        JButton nextButton =
                new JButton(
                        "Next >"
                );


        JButton addEmployeeButton =
                new JButton(
                        "Add Employee"
                );


        JButton removeEmployeeButton =
                new JButton(
                        "Remove Employee"
                );


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


        JButton makeCheckButton =
                new JButton(
                        "Generate Check"
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

        bottomPanel.add(
                makeCheckButton
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


        setupMakeCheckButton(
                makeCheckButton
        );


        // =========================
        // INITIAL SETUP
        // =========================

        updateEmployeeSelector();

        loadCurrentEmployee();


        frame.setLocationRelativeTo(
                null
        );


        frame.setVisible(
                true
        );
    }


    // =========================
    // EMPLOYEE SELECTOR
    // =========================

    private void setupEmployeeSelector() {

        employeeSelector.addActionListener(
                e -> {

                    if (changingEmployee) {
                        return;
                    }


                    int selectedIndex =
                            employeeSelector
                                    .getSelectedIndex();


                    if (
                            selectedIndex >= 0 &&
                                    selectedIndex !=
                                            employeeManager
                                                    .getCurrentEmployeeIndex()
                    ) {

                        switchEmployee(
                                selectedIndex
                        );
                    }
                }
        );
    }


    // =========================
    // PREVIOUS BUTTON
    // =========================

    private void setupPreviousButton(
            JButton button
    ) {

        button.addActionListener(
                e -> {

                    int current =
                            employeeManager
                                    .getCurrentEmployeeIndex();


                    if (current > 0) {

                        employeeSelector
                                .setSelectedIndex(
                                        current - 1
                                );
                    }
                }
        );
    }


    // =========================
    // NEXT BUTTON
    // =========================

    private void setupNextButton(
            JButton button
    ) {

        button.addActionListener(
                e -> {

                    int current =
                            employeeManager
                                    .getCurrentEmployeeIndex();


                    if (
                            current <
                                    employeeManager
                                            .getEmployeeCount() - 1
                    ) {

                        employeeSelector
                                .setSelectedIndex(
                                        current + 1
                                );
                    }
                }
        );
    }


    // =========================
    // ADD EMPLOYEE
    // =========================

    private void setupAddEmployeeButton(
            JButton button
    ) {

        button.addActionListener(
                e -> {

                    String name =
                            JOptionPane.showInputDialog(
                                    frame,
                                    "Enter employee name:"
                            );


                    if (
                            name == null ||
                                    name.trim().isEmpty()
                    ) {

                        return;
                    }


                    saveCurrentEmployee();


                    employeeManager.addEmployee(
                            new Employee(
                                    name.trim()
                            )
                    );


                    employeeManager.setCurrentEmployeeIndex(
                            employeeManager
                                    .getEmployeeCount() - 1
                    );


                    changingEmployee = true;


                    updateEmployeeSelector();


                    employeeSelector.setSelectedIndex(
                            employeeManager
                                    .getCurrentEmployeeIndex()
                    );


                    changingEmployee = false;


                    loadCurrentEmployee();
                }
        );
    }


    // =========================
    // REMOVE EMPLOYEE
    // =========================

    private void setupRemoveEmployeeButton(
            JButton button
    ) {

        button.addActionListener(
                e -> {

                    if (
                            employeeManager
                                    .getEmployeeCount() <= 1
                    ) {

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


                    if (
                            choice !=
                                    JOptionPane.YES_OPTION
                    ) {

                        return;
                    }


                    saveCurrentEmployee();


                    employeeManager
                            .removeCurrentEmployee();


                    changingEmployee = true;


                    updateEmployeeSelector();


                    employeeSelector.setSelectedIndex(
                            employeeManager
                                    .getCurrentEmployeeIndex()
                    );


                    changingEmployee = false;


                    loadCurrentEmployee();
                }
        );
    }


    // =========================
    // HOURLY RATE BUTTON
    // =========================

    private void setupHourlyRateButton(
            JButton button
    ) {

        button.addActionListener(
                e -> {

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


                    if (
                            newRate == null ||
                                    newRate.trim().isEmpty()
                    ) {

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


                    } catch (
                            NumberFormatException error
                    ) {

                        JOptionPane.showMessageDialog(
                                frame,
                                "Please enter a valid number for the hourly rate."
                        );
                    }
                }
        );
    }


    // =========================
    // OT RATE BUTTON
    // =========================

    private void setupOTRateButton(
            JButton button
    ) {

        button.addActionListener(
                e -> {

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


                    if (
                            newRate == null ||
                                    newRate.trim().isEmpty()
                    ) {

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


                    } catch (
                            NumberFormatException error
                    ) {

                        JOptionPane.showMessageDialog(
                                frame,
                                "Please enter a valid number for the OT rate."
                        );
                    }
                }
        );
    }


    // =========================
    // GENERATE CHECK BUTTON
    // =========================

    private void setupMakeCheckButton(
            JButton button
    ) {

        button.addActionListener(
                e -> generateCheck()
        );
    }


    // =========================
    // GENERATE CHECK
    // =========================

    private void generateCheck() {

        // Make sure the latest table edits
        // are saved before generating.
        tableManager.stopEditing();

        saveCurrentEmployee();


        Employee employee =
                employeeManager
                        .getCurrentEmployee();


        if (employee == null) {

            JOptionPane.showMessageDialog(
                    frame,
                    "No employee is currently selected."
            );

            return;
        }


        // ==========================================
        // SELECT PAY PERIOD
        // ==========================================

        String[] options =
                new String[
                        PayrollData.PAY_PERIODS.length
                        ];


        for (
                int i = 0;
                i < PayrollData.PAY_PERIODS.length;
                i++
        ) {

            options[i] =
                    "Pay Period " +
                            PayrollData.PAY_PERIODS[i][0] +
                            " | " +
                            PayrollData.PAY_PERIODS[i][1] +
                            " to " +
                            PayrollData.PAY_PERIODS[i][2];
        }


        String selected =
                (String) JOptionPane.showInputDialog(
                        frame,

                        "Select a pay period:",

                        "Generate Check",

                        JOptionPane.QUESTION_MESSAGE,

                        null,

                        options,

                        options[0]
                );


        if (selected == null) {
            return;
        }


        // ==========================================
        // FIND SELECTED PERIOD
        // ==========================================

        int selectedIndex = 0;


        for (
                int i = 0;
                i < options.length;
                i++
        ) {

            if (
                    options[i].equals(
                            selected
                    )
            ) {

                selectedIndex = i;
                break;
            }
        }


        // ==========================================
        // CURRENT PAY PERIOD
        // ==========================================

        boolean[] currentPeriod =
                new boolean[
                        PayrollData.PAY_PERIODS.length
                        ];


        currentPeriod[selectedIndex] = true;


        Map<String, Double> currentTotals =
                tableManager.calculateEmployeeTotals(
                        employee,
                        currentPeriod
                );


        // ==========================================
        // YTD
        // ==========================================

        boolean[] ytdPeriods =
                new boolean[
                        PayrollData.PAY_PERIODS.length
                        ];


        // Include every pay period from Period 1
        // through the selected pay period.
        for (
                int i = 0;
                i <= selectedIndex;
                i++
        ) {

            ytdPeriods[i] = true;
        }


        Map<String, Double> ytdTotals =
                tableManager.calculateEmployeeTotals(
                        employee,
                        ytdPeriods
                );


        // ==========================================
        // PAY PERIOD INFORMATION
        // ==========================================

        String payDate =
                PayrollData.PAY_PERIODS[
                        selectedIndex
                        ][3];


        int payPeriod =
                Integer.parseInt(
                        PayrollData.PAY_PERIODS[
                                selectedIndex
                                ][0]
                );


        String startDate =
                PayrollData.PAY_PERIODS[
                        selectedIndex
                        ][1];


        String endDate =
                PayrollData.PAY_PERIODS[
                        selectedIndex
                        ][2];


        // ==========================================
        // GENERATE CHECK
        // ==========================================

        PayrollCheckGenerator.generateCheck(

                // Employee information
                employee.name,
                payDate,
                payPeriod,
                startDate,
                endDate,
                employee.address,
                employee.city,
                employee.zip,

                // Current earnings
                currentTotals.get("Hours"),
                currentTotals.get("OT Hours"),
                currentTotals.get("Regular Pay"),
                currentTotals.get("OT Pay"),

                // YTD earnings
                ytdTotals.get("Regular Pay"),
                ytdTotals.get("OT Pay"),

                // Current deductions
                currentTotals.get("Federal"),
                currentTotals.get("Social Security"),
                currentTotals.get("Medicare"),
                currentTotals.get("SLG Tax"),
                currentTotals.get("Total Deductions"),
                currentTotals.get("Net Pay"),

                // YTD deductions
                ytdTotals.get("Federal"),
                ytdTotals.get("Social Security"),
                ytdTotals.get("Medicare"),
                ytdTotals.get("SLG Tax"),
                ytdTotals.get("Total Deductions"),
                ytdTotals.get("Net Pay"),

                // Current Bonus
                currentTotals.get("Bonus"),
                ytdTotals.get("Bonus")
        );


        JOptionPane.showMessageDialog(
                frame,
                "Check generated successfully!\n\n" +
                        "File: GeneratedCheck.png"
        );
    }


    // =========================
    // SWITCH EMPLOYEE
    // =========================

    private void switchEmployee(
            int newEmployeeIndex
    ) {

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
                employeeManager
                        .getCurrentEmployee();


        employeeNameLabel.setText(
                "Current Employee: " +
                        employee.name
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
                employeeManager
                        .getCurrentEmployee()
        );
    }


    // =========================
    // UPDATE SELECTOR
    // =========================

    private void updateEmployeeSelector() {

        employeeSelector.removeAllItems();


        for (
                Employee employee :
                employeeManager.getEmployees()
        ) {

            employeeSelector.addItem(
                    employee.name
            );
        }
    }
}