
import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.io.IOException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Desktop;
import java.io.File;
import java.time.LocalDate;

public class PayrollGUI {

    private final EmployeeManager employeeManager;

    private final JFrame frame;
    private final JComboBox<String> employeeSelector;
    private final JComboBox<String> payrollYearSelector;
    private final JLabel employeeNameLabel;

    private final PayrollTableManager tableManager;

    private boolean changingEmployee = false;
    private boolean changingPayrollYear = false;


    // =========================
    // CONSTRUCTOR
    // =========================

    public PayrollGUI(EmployeeManager employeeManager) {

        this.employeeManager = employeeManager;


        // =========================
        // FRAME
        // =========================


        frame = new JFrame("Payroll System Alpha " + AppVersion.VERSION);

        frame.setSize(1200, 700);

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frame.setLayout(new BorderLayout());


        // =========================
        // MANAGERS
        // =========================

        tableManager = new PayrollTableManager(frame);


        // Save upon closing
        frame.addWindowListener(
                new WindowAdapter() {

                    @Override
                    public void windowClosing(WindowEvent event) {

                        // Save any unsaved table edits
                        tableManager.stopEditing();

                        saveCurrentEmployee();

                        frame.dispose();
                        System.exit(0);
                    }
                }
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

        JPanel topPanel = new JPanel(new BorderLayout());

        JPanel employeeControlsPanel = new JPanel();

        employeeNameLabel = new JLabel("Current Employee:");

        employeeSelector = new JComboBox<>();

        payrollYearSelector = new JComboBox<>(getPayrollYearOptions());
        payrollYearSelector.setSelectedItem(String.valueOf(PayrollData.getActiveYear()));

        JButton previousButton = new JButton("< Previous");

        JButton nextButton = new JButton("Next >");

        JButton addEmployeeButton = new JButton("Add Employee");

        JButton removeEmployeeButton = new JButton("Remove Employee");

        JButton employeeActionsButton = new JButton("Employee Actions");

        JButton viewSingleTotalsButton =
                new JButton("Create PDF Report for this Employee");


        employeeControlsPanel.add(employeeNameLabel);
        employeeControlsPanel.add(employeeSelector);
        employeeControlsPanel.add(new JLabel("Payroll Year:"));
        employeeControlsPanel.add(payrollYearSelector);
        employeeControlsPanel.add(previousButton);
        employeeControlsPanel.add(nextButton);
        employeeControlsPanel.add(employeeActionsButton);
        employeeControlsPanel.add(viewSingleTotalsButton);


        topPanel.add(employeeControlsPanel, BorderLayout.WEST);

        topPanel.add(tableManager.getFedRatePanel(), BorderLayout.EAST);

        frame.add(topPanel, BorderLayout.NORTH);


        // =========================
        // TABLE
        // =========================

        JScrollPane scrollPane =
                new JScrollPane(tableManager.getTable());

        // Keep the full-width payroll table readable in narrow windows.
        scrollPane.setHorizontalScrollBarPolicy(
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );

        frame.add(scrollPane, BorderLayout.CENTER);


        // =========================
        // BOTTOM PANEL
        // =========================

        JPanel bottomPanel = new JPanel();

        JButton changeHourlyRateButton =
                new JButton("Mass Change Hourly Rate");

        JButton changeOTRateButton =
                new JButton("Mass Change OT Rate");

        JButton viewTotalsButton =
                new JButton("View All Employee Totals");

        JButton makeCheckButton =
                new JButton("Generate All Checks PDF");

        JButton makeAddressButton =
                new JButton("Add/Change Address");

        JCheckBox simplifiedModeToggle =
                new JCheckBox("Simplified Mode");


        bottomPanel.add(changeHourlyRateButton);
        bottomPanel.add(changeOTRateButton);
        bottomPanel.add(viewTotalsButton);
        bottomPanel.add(makeCheckButton);
        bottomPanel.add(simplifiedModeToggle);


        frame.add(bottomPanel, BorderLayout.SOUTH);


        // =========================
        // BUTTON EVENTS
        // =========================

        setupEmployeeSelector();

        setupPayrollYearSelector();

        setupPreviousButton(previousButton);

        setupNextButton(nextButton);

        setupAddEmployeeButton(addEmployeeButton);

        setupRemoveEmployeeButton(removeEmployeeButton);

        JPopupMenu employeeActionsMenu = new JPopupMenu();

        // Keep employee-management tasks together without using several permanent buttons.

        JMenuItem addEmployeeMenuItem = new JMenuItem("Add Employee");
        addEmployeeMenuItem.addActionListener(e -> addEmployeeButton.doClick());

        JMenuItem removeEmployeeMenuItem = new JMenuItem("Remove Employee");
        removeEmployeeMenuItem.addActionListener(e -> removeEmployeeButton.doClick());

        JMenuItem changeAddressMenuItem = new JMenuItem("Add/Change Address");
        changeAddressMenuItem.addActionListener(e -> makeAddressButton.doClick());

        // A hand cursor makes it clear that these controls can be clicked.
        setHandCursor(
                previousButton,
                nextButton,
                employeeActionsButton,
                viewSingleTotalsButton,
                changeHourlyRateButton,
                changeOTRateButton,
                viewTotalsButton,
                makeCheckButton,
                simplifiedModeToggle,
                addEmployeeMenuItem,
                removeEmployeeMenuItem,
                changeAddressMenuItem
        );

        employeeActionsMenu.add(addEmployeeMenuItem);
        employeeActionsMenu.add(removeEmployeeMenuItem);
        employeeActionsMenu.add(changeAddressMenuItem);

        employeeActionsButton.addActionListener(e -> employeeActionsMenu.show(
                employeeActionsButton,
                0,
                employeeActionsButton.getHeight()
        ));

        setupHourlyRateButton(changeHourlyRateButton);

        setupOTRateButton(changeOTRateButton);

        totalsManager.setupViewTotalsButton(viewTotalsButton);

        viewSingleTotalsButton.addActionListener(e -> {

            // Save any changes currently being edited
            tableManager.stopEditing();
            saveCurrentEmployee();

            // Get the currently selected employee
            Employee employee =
                    employeeManager.getCurrentEmployee();

            // Make sure an employee exists
            if (employee == null) {

                JOptionPane.showMessageDialog(
                        frame,
                        "No employee is currently selected."
                );

                return;
            }

            try {

                // Generate the employee's printable PDF report.
                File report = EmployeeTotalsReportGenerator.generate(employee);

                // Tell the user where it was saved
                JOptionPane.showMessageDialog(
                        frame,
                        "Employee payroll report saved as a PDF:\n\n"
                                + report.getAbsolutePath(),
                        "Payroll Report Created",
                        JOptionPane.INFORMATION_MESSAGE
                );

                // Open the PDF automatically.
                Desktop.getDesktop().open(report);

            } catch (Exception ex) {

                ex.printStackTrace();

                JOptionPane.showMessageDialog(
                        frame,
                        "Could not create the employee payroll report:\n\n"
                                + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        setupMakeCheckButton(makeCheckButton);

        setupMakeAddressButton(makeAddressButton);

        simplifiedModeToggle.addActionListener(
                // Simplified mode changes the table view only; every payroll value remains saved.
                e -> tableManager.setSimplifiedMode(
                        simplifiedModeToggle.isSelected()
                )
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

        employeeSelector.addActionListener(
                e -> {

                    if (changingEmployee) {
                        return;
                    }

                    int selectedIndex =
                            employeeSelector.getSelectedIndex();

                    if (
                            selectedIndex >= 0 &&
                                    selectedIndex !=
                                            employeeManager.getCurrentEmployeeIndex()
                    ) {

                        switchEmployee(selectedIndex);
                    }
                }
        );
    }


    // =========================
    // PAYROLL YEAR SELECTOR
    // =========================

    private void setupPayrollYearSelector() {

        payrollYearSelector.addActionListener(e -> {

            if (changingPayrollYear) {
                return;
            }

            int selectedYear;

            try {
                selectedYear = Integer.parseInt((String) payrollYearSelector.getSelectedItem());
            } catch (NumberFormatException exception) {
                return;
            }

            if (selectedYear == PayrollData.getActiveYear()) {
                return;
            }

            // Store edits under the old year before displaying the new year's blank or saved rows.
            tableManager.stopEditing();
            saveCurrentEmployee();

            PayrollData.setActiveYear(selectedYear);

            for (Employee employee : employeeManager.getEmployees()) {
                employee.activatePayrollYear(selectedYear);
            }

            loadCurrentEmployee();
        });
    }

    /** Offers the original 2026 records plus several future payroll years. */
    private String[] getPayrollYearOptions() {

        int lastYear = Math.max(2027, LocalDate.now().getYear() + 3);
        String[] years = new String[lastYear - 2026 + 1];

        for (int year = 2026; year <= lastYear; year++) {
            years[year - 2026] = String.valueOf(year);
        }

        return years;
    }


    // =========================
    // PREVIOUS BUTTON
    // =========================

    private void setupPreviousButton(JButton button) {

        button.addActionListener(
                e -> {

                    int current =
                            employeeManager.getCurrentEmployeeIndex();

                    if (current > 0) {

                        employeeSelector.setSelectedIndex(
                                current - 1
                        );

                    } else {

                        employeeSelector.setSelectedIndex(
                                employeeManager.getEmployeeCount() - 1
                        );
                    }
                }
        );
    }


    // =========================
    // NEXT BUTTON
    // =========================

    private void setupNextButton(JButton button) {

        button.addActionListener(
                e -> {

                    int current =
                            employeeManager.getCurrentEmployeeIndex();

                    if (
                            current <
                                    employeeManager.getEmployeeCount() - 1
                    ) {

                        employeeSelector.setSelectedIndex(
                                current + 1
                        );

                    } else {

                        employeeSelector.setSelectedIndex(0);
                    }
                }
        );
    }


    // =========================
    // ADD EMPLOYEE
    // =========================

    private void setupAddEmployeeButton(JButton button) {

        button.addActionListener(
                e -> {

                    // Get Name
                    String name =
                            JOptionPane.showInputDialog(
                                    frame,
                                    "Enter employee name:"
                            );

                    if (name == null || name.trim().isEmpty()) {
                        return;
                    }


                    // Get Address
                    String address =
                            JOptionPane.showInputDialog(
                                    frame,
                                    "Enter street address:"
                            );

                    if (address == null || address.trim().isEmpty()) {
                        return;
                    }


                    // Get City
                    String city =
                            JOptionPane.showInputDialog(
                                    frame,
                                    "Enter city and state:"
                            );

                    if (city == null || city.trim().isEmpty()) {
                        return;
                    }


                    // Get Zip Code
                    String zipCode =
                            JOptionPane.showInputDialog(
                                    frame,
                                    "Enter ZIP code:"
                            );

                    if (zipCode == null || zipCode.trim().isEmpty()) {
                        return;
                    }


                    // Confirm Information
                    String message =
                            "Is this information correct?\n\n" +
                                    "Name: " + name.trim() + "\n" +
                                    "Address: " + address.trim() + "\n" +
                                    "City / State: " + city.trim() + "\n" +
                                    "ZIP Code: " + zipCode.trim();


                    int result =
                            JOptionPane.showConfirmDialog(
                                    frame,
                                    message,
                                    "Confirm Employee Information",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE
                            );


                    // User clicked No
                    if (result != JOptionPane.YES_OPTION) {
                        return;
                    }


                    saveCurrentEmployee();


                    Employee newEmployee =
                            new Employee(
                                    name.trim(),
                                    address.trim(),
                                    city.trim(),
                                    zipCode.trim()
                            );


                    employeeManager.addEmployee(newEmployee);


                    setLoadingCursor(true);

                    try {

                        String mongoId =
                                PayrollAPI.addEmployee(newEmployee);

                        newEmployee.mongoId = mongoId;

                        System.out.println(
                                "New employee MongoDB ID: " +
                                        newEmployee.mongoId
                        );

                    } catch (IOException error) {

                        error.printStackTrace();

                        JOptionPane.showMessageDialog(
                                frame,
                                "Employee was added locally, " +
                                "but could not be saved to the server."
                        );
                    } finally {

                        setLoadingCursor(false);
                    }


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
                }
        );
    }


    // =========================
// REMOVE EMPLOYEE
// =========================

    private void setupRemoveEmployeeButton(JButton button) {

        button.addActionListener(
                e -> {

                    if (employeeManager.getEmployeeCount() <= 1) {

                        JOptionPane.showMessageDialog(
                                frame,
                                "You must have at least one employee."
                        );

                        return;
                    }


                    Employee employee =
                            employeeManager.getCurrentEmployee();


                    String employeeName =
                            employee.name;


                    int choice =
                            JOptionPane.showConfirmDialog(
                                    frame,
                                    "Remove " +
                                            employeeName +
                                            "? (THIS ACTION CANNOT BE UNDONE!)",
                                    "Remove Employee",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.WARNING_MESSAGE
                            );


                    if (choice != JOptionPane.YES_OPTION) {
                        return;
                    }


                    // ==========================================
                    // SAVE CURRENT TABLE DATA
                    // ==========================================

                    tableManager.stopEditing();

                    tableManager.saveEmployee(employee);


                    // ==========================================
                    // DELETE FROM MONGODB
                    // ==========================================

                    if (
                            employee.mongoId != null &&
                                    !employee.mongoId.trim().isEmpty()
                    ) {

                        setLoadingCursor(true);

                        try {

                            PayrollAPI.deleteEmployee(
                                    employee.mongoId
                            );

                        } catch (IOException error) {

                            error.printStackTrace();

                            JOptionPane.showMessageDialog(
                                    frame,
                                    "Could not delete " +
                                            employeeName +
                                            " from the server.\n\n" +
                                            "The employee was NOT removed."
                            );

                            return;
                        } finally {

                            setLoadingCursor(false);
                        }
                    }


                    // ==========================================
                    // REMOVE FROM LOCAL EMPLOYEE LIST
                    // ==========================================

                    employeeManager.removeCurrentEmployee();

                    // ==========================================
                    // UPDATE UI
                    // ==========================================

                    changingEmployee = true;

                    updateEmployeeSelector();

                    employeeSelector.setSelectedIndex(
                            employeeManager.getCurrentEmployeeIndex()
                    );

                    changingEmployee = false;

                    loadCurrentEmployee();
                }
        );
    }


    // =========================
    // CHANGE EMPLOYEE ADDRESS BUTTON
    // =========================

    private void setupMakeAddressButton(JButton makeAddressButton) {

        makeAddressButton.addActionListener(
                e -> {

                    Employee employee =
                            employeeManager.getCurrentEmployee();


                    // Get Address
                    String address =
                            JOptionPane.showInputDialog(
                                    frame,
                                    "Enter street address:",
                                    employee.getAddress()
                            );

                    if (address == null || address.trim().isEmpty()) {
                        return;
                    }


                    // Get City
                    String city =
                            JOptionPane.showInputDialog(
                                    frame,
                                    "Enter city and state:",
                                    employee.getCity()
                            );

                    if (city == null || city.trim().isEmpty()) {
                        return;
                    }


                    // Get ZIP Code
                    String zipCode =
                            JOptionPane.showInputDialog(
                                    frame,
                                    "Enter ZIP code:",
                                    employee.getZipCode()
                            );

                    if (zipCode == null || zipCode.trim().isEmpty()) {
                        return;
                    }


                    // Save Information
                    employee.setAddress(address.trim());
                    employee.setCity(city.trim());
                    employee.setZipCode(zipCode.trim());

                    // Address changes now use the same cloud save as every other employee update.
                    saveCurrentEmployee();
                }
        );
    }


    // =========================
    // HOURLY RATE BUTTON
    // =========================

    private void setupHourlyRateButton(JButton button) {

        button.addActionListener(
                e -> {

                    tableManager.stopEditing();

                    int row =
                            tableManager.getTable().getSelectedRow();


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


                    if (newRate == null || newRate.trim().isEmpty()) {
                        return;
                    }


                    try {

                        double rate =
                                Double.parseDouble(newRate.trim());


                        if (rate < 0) {

                            JOptionPane.showMessageDialog(
                                    frame,
                                    "Hourly rate cannot be negative."
                            );

                            return;
                        }


                        tableManager.changeHourlyRate(row, newRate);

                        saveCurrentEmployee();

                    } catch (NumberFormatException error) {

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

    private void setupOTRateButton(JButton button) {

        button.addActionListener(
                e -> {

                    tableManager.stopEditing();

                    int row =
                            tableManager.getTable().getSelectedRow();


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


                    if (newRate == null || newRate.trim().isEmpty()) {
                        return;
                    }


                    try {

                        double rate =
                                Double.parseDouble(newRate.trim());


                        if (rate < 0) {

                            JOptionPane.showMessageDialog(
                                    frame,
                                    "OT rate cannot be negative."
                            );

                            return;
                        }


                        tableManager.changeOTRate(row, newRate);

                        saveCurrentEmployee();

                    } catch (NumberFormatException error) {

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

    private void setupMakeCheckButton(JButton button) {

        button.addActionListener(e -> generateCheck());
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
                employeeManager.getCurrentEmployee();


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
                new String[PayrollData.PAY_PERIODS.length];


        for (int i = 0; i < PayrollData.PAY_PERIODS.length; i++) {

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
                        "Generate All Checks PDF",
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


        for (int i = 0; i < options.length; i++) {

            if (options[i].equals(selected)) {

                selectedIndex = i;
                break;
            }
        }


        // ==========================================
        // CURRENT PAY PERIOD
        // ==========================================

        boolean[] currentPeriod =
                new boolean[PayrollData.PAY_PERIODS.length];

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
                new boolean[PayrollData.PAY_PERIODS.length];


        // Include every pay period from Period 1
        // through the selected pay period.
        for (int i = 0; i <= selectedIndex; i++) {
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
                PayrollData.PAY_PERIODS[selectedIndex][3];


        int payPeriod =
                Integer.parseInt(
                        PayrollData.PAY_PERIODS[selectedIndex][0]
                );


        String startDate =
                PayrollData.PAY_PERIODS[selectedIndex][1];


        String endDate =
                PayrollData.PAY_PERIODS[selectedIndex][2];


        // ==========================================
        // GENERATE CHECK
        // ==========================================

        setLoadingCursor(true);

        try {

            File checkPdf = PayrollCheckGenerator.generateChecks(
                    employeeManager,
                    tableManager,
                    selectedIndex
            );

            JOptionPane.showMessageDialog(
                    frame,
                    "Checks generated successfully for all employees!\n\n"
                            + "PDF: " + checkPdf.getAbsolutePath()
            );

            Desktop.getDesktop().open(checkPdf);

        } catch (IOException error) {

            error.printStackTrace();

            JOptionPane.showMessageDialog(
                    frame,
                    "Could not create the payroll-check PDF:\n\n"
                            + error.getMessage(),
                    "Check Generation Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    // =========================
    // SWITCH EMPLOYEE
    // =========================

    private void switchEmployee(int newEmployeeIndex) {

        // Save the current table before replacing it with another employee's data.
        saveCurrentEmployee();

        employeeManager.setCurrentEmployeeIndex(newEmployeeIndex);

        loadCurrentEmployee();
    }


    // =========================
    // LOAD CURRENT EMPLOYEE
    // =========================

    private void loadCurrentEmployee() {

        Employee employee =
                employeeManager.getCurrentEmployee();


        employeeNameLabel.setText("Current Employee: ");


        tableManager.loadEmployee(employee);
    }

// =========================
// SAVE CURRENT EMPLOYEE
// =========================

    private void saveCurrentEmployee() {

        Employee employee =
                employeeManager.getCurrentEmployee();

        tableManager.saveEmployee(employee);


        // ==========================================
        // SAVE TO MONGODB
        // ==========================================

        if (
                employee.mongoId == null ||
                        employee.mongoId.trim().isEmpty()
        ) {

            System.out.println(
                    "Employee has no MongoDB ID: " +
                            employee.name
            );

            return;
        }


        try {

            PayrollAPI.updateEmployee(
                    employee.mongoId,
                    employee
            );

            System.out.println(
                    "Employee saved to MongoDB: " +
                            employee.name
            );

        } catch (IOException error) {

            error.printStackTrace();

            JOptionPane.showMessageDialog(
                    frame,
                    "Could not save " +
                            employee.name +
                            " to the server.\n\n" +
                    "Your local data was not affected."
            );
        } finally {

            setLoadingCursor(false);
        }
    }

    // Shows the spinning mouse cursor while the program is waiting for the cloud server.
    private void setLoadingCursor(boolean loading) {

        int cursorType = loading
                ? Cursor.WAIT_CURSOR
                : Cursor.DEFAULT_CURSOR;

        // The glass pane sits over every button, so its waiting cursor is always visible.
        JComponent glassPane = (JComponent) frame.getGlassPane();
        glassPane.setCursor(Cursor.getPredefinedCursor(cursorType));
        glassPane.setVisible(loading);

        // The mouse may already be resting on the button that was clicked.
        // Change every button directly so that cursor immediately becomes the spinning one.
        setButtonCursors(
                frame.getContentPane(),
                loading ? Cursor.WAIT_CURSOR : Cursor.HAND_CURSOR
        );
    }

    // Gives every clickable button the familiar hand cursor.
    private void setHandCursor(AbstractButton... buttons) {

        Cursor handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

        for (AbstractButton button : buttons) {
            button.setCursor(handCursor);

            // Start the spinning cursor before the click action begins loading data.
            button.addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent event) {
                    button.setCursor(
                            Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                    );
                }

                @Override
                public void mouseReleased(MouseEvent event) {
                    Timer restoreHandCursor = new Timer(500, e ->
                            button.setCursor(
                                    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                            )
                    );

                    restoreHandCursor.setRepeats(false);
                    restoreHandCursor.start();
                }
            });
        }
    }

    // Changes every visible button cursor without changing the text cursor inside table cells.
    private void setButtonCursors(Container container, int cursorType) {

        Cursor cursor = Cursor.getPredefinedCursor(cursorType);

        for (Component component : container.getComponents()) {

            if (component instanceof AbstractButton) {
                component.setCursor(cursor);
            }

            if (component instanceof Container) {
                setButtonCursors((Container) component, cursorType);
            }
        }
    }
    // =========================
    // UPDATE SELECTOR
    // =========================

    private void updateEmployeeSelector() {

        // Rebuild the list after an employee has been added or removed.
        employeeSelector.removeAllItems();


        for (Employee employee : employeeManager.getEmployees()) {
            employeeSelector.addItem(employee.name);
        }
    }
}
