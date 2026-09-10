import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.util.LinkedHashMap;
import java.util.Map;

public class PayrollTableManager {

    // =========================
    // COLUMN NUMBERS
    // =========================

    public static final int HOURS = 5;
    public static final int OT_HOURS = 6;
    public static final int HOURLY_RATE = 7;
    public static final int OT_RATE = 8;
    public static final int EXTRA = 9;
    public static final int TOTAL_GROSS = 10;
    public static final int FEDERAL = 11;
    public static final int SOCIAL_SECURITY = 12;
    public static final int MEDICARE = 13;
    public static final int MD_TAX = 14;
    public static final int BC_TAX = 15;
    public static final int NET_PAY = 16;
    public static final int REGULAR_PAY = 17;
    public static final int OT_TOTAL = 18;
    public static final int SLG_TAX = 19;
    public static final int TOTAL_DEDUCTIONS = 20;

    private static final int[] COLUMN_WIDTHS = {
            90, 115, 115, 95, 70,
            75, 75, 90, 90, 75,
            100, 85, 105, 80, 80, 80, 90, 95, 85, 85, 115
    };

    private static final int[] SIMPLIFIED_HIDDEN_COLUMNS = {
            2, 3, 4, FEDERAL, SOCIAL_SECURITY, MEDICARE, MD_TAX, BC_TAX,
            REGULAR_PAY, OT_TOTAL, SLG_TAX
    };

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JFrame frame;
    private final JPanel fedRatePanel;
    private final JTextField fedRateField;

    private final PayrollCalculator calculator = new PayrollCalculator();


    // =========================
    // CONSTRUCTOR
    // =========================

    public PayrollTableManager(JFrame frame) {

        this.frame = frame;


        // =========================
        // FED RATE
        // =========================

        fedRateField = new JTextField(8);
        fedRateField.setText("0");

        fedRatePanel = new JPanel();

        fedRatePanel.setBorder(
                BorderFactory.createTitledBorder("Fed Rate %")
        );

        fedRatePanel.add(fedRateField);

        fedRateField.addActionListener(e -> {
            validateFedRate();
            calculateAllRows();
        });

        fedRateField.addFocusListener(new java.awt.event.FocusAdapter() {

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                validateFedRate();
                calculateAllRows();
            }
        });


        // =========================
        // COLUMN NAMES
        // =========================

        String[] columnNames = {

                "Pay Period #",
                "Pay Period Start",
                "Pay Period End",
                "Paid On",
                "Quarter",

                "Hours",
                "OT Hours",
                "Hourly Rate",
                "OT Rate",
                "Extra $",

                "Total Gross",
                "Federal",
                "Social Security",
                "Medicare",
                "MD Tax",
                "BC Tax",
                "Net Pay",
                "Regular Pay",
                "OT Pay",
                "SLG Tax",
                "Total Deductions"
        };


        // =========================
        // TABLE MODEL
        // =========================

        tableModel = new DefaultTableModel(columnNames, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= HOURS && column <= EXTRA;
            }
        };

        table = new JTable(tableModel);

        // Keep columns at readable widths.  A smaller program window will scroll sideways
        // instead of squeezing the columns and replacing their headings with "...".
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Keep each column in its correct spot so users cannot drag the headings into a different order.
        table.getTableHeader().setReorderingAllowed(false);

        setColumnWidths();

        setupValidation();
    }


    // =========================
    // GETTERS
    // =========================

    public JTable getTable() {
        return table;
    }

    public JPanel getFedRatePanel() {
        return fedRatePanel;
    }

    public JTextField getFedRateField() {
        return fedRateField;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    /** Hides or restores the detailed columns without changing any employee data. */
    public void setSimplifiedMode(boolean simplifiedMode) {

        for (int columnIndex : SIMPLIFIED_HIDDEN_COLUMNS) {

            TableColumn column = table.getColumnModel().getColumn(columnIndex);

            if (simplifiedMode) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
                column.setPreferredWidth(0);
                column.setWidth(0);
                column.setResizable(false);
            } else {
                restoreColumnWidth(columnIndex);
            }
        }
    }


    // =========================
    // TABLE COLUMN WIDTHS
    // =========================

    private void setColumnWidths() {

        for (int column = 0; column < COLUMN_WIDTHS.length; column++) {
            restoreColumnWidth(column);
        }
    }

    /** Restores one full-table column to its normal readable width. */
    private void restoreColumnWidth(int columnIndex) {

        TableColumn column = table.getColumnModel().getColumn(columnIndex);
        int width = COLUMN_WIDTHS[columnIndex];

        column.setMinWidth(width);
        column.setMaxWidth(Integer.MAX_VALUE);
        column.setPreferredWidth(width);
        column.setWidth(width);
        column.setResizable(true);
    }


    // =========================
    // VALIDATION SETUP
    // =========================

    private void setupValidation() {

        table.getDefaultEditor(Object.class)
                .addCellEditorListener(new CellEditorListener() {

                    @Override
                    public void editingStopped(ChangeEvent e) {
                        validateCell();
                    }

                    @Override
                    public void editingCanceled(ChangeEvent e) {
                    }
                });
    }


    // =========================
    // VALIDATE CELL
    // =========================

    private void validateCell() {

        int row = table.getSelectedRow();
        int column = table.getSelectedColumn();

        if (row == -1 || column == -1) {
            return;
        }

        if (column < HOURS || column > EXTRA) {
            return;
        }

        String value = getValue(row, column).trim();

        if (value.isEmpty()) {
            calculateRow(row);
            return;
        }

        try {

            double number = Double.parseDouble(value);

            if (number < 0) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Please enter a number greater than or equal to 0."
                );

                tableModel.setValueAt("", row, column);
            }

        } catch (NumberFormatException error) {

            JOptionPane.showMessageDialog(
                    frame,
                    "Invalid input.\nPlease enter a valid number."
            );

            tableModel.setValueAt("", row, column);
        }

        calculateRow(row);
    }


    // =========================
    // VALIDATE FED RATE
    // =========================

    private void validateFedRate() {

        String value = fedRateField.getText().trim();

        if (value.isEmpty()) {
            fedRateField.setText("0");
            return;
        }

        try {

            double rate = Double.parseDouble(value);

            if (rate < 0) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Fed Rate cannot be negative."
                );

                fedRateField.setText("0");
            }

        } catch (NumberFormatException error) {

            JOptionPane.showMessageDialog(
                    frame,
                    "Please enter a valid number for the Fed Rate."
            );

            fedRateField.setText("0");
        }
    }


    // =========================
    // CALCULATE ALL ROWS
    // =========================

    private void calculateAllRows() {

        // Finish the user's current edit before recalculating values from the table.
        stopEditing();

        for (int row = 0; row < tableModel.getRowCount(); row++) {
            calculateRow(row);
        }
    }


    // =========================
    // CALCULATE ROW
    // =========================

    private void calculateRow(int row) {

        double regularPay = calculator.calculateRegularPay(
                getValue(row, HOURS),
                getValue(row, HOURLY_RATE)
        );

        double otPay = calculator.calculateOTPay(
                getValue(row, OT_HOURS),
                getValue(row, OT_RATE)
        );

        double extra = calculator.calculateExtraPay(
                getValue(row, EXTRA)
        );

        double totalGross = regularPay + otPay + extra;

        double federal = calculator.calculateFedRate(
                totalGross,
                fedRateField.getText()
        );

        double socialSecurity = calculator.calculateSocialSecurity(totalGross);
        double medicare = calculator.calculateMedicare(totalGross);
        double maryland = calculator.calculateMaryland(totalGross);
        double baltimore = calculator.calculateBaltimore(totalGross);

        double net = calculator.calculateNet(
                totalGross,
                federal,
                socialSecurity,
                medicare,
                maryland,
                baltimore
        );

        // SLG is shown as the combined Maryland and Baltimore County tax amount.
        double slg = maryland + baltimore;
        double deductions = totalGross - net;

        tableModel.setValueAt(String.format("%.2f", regularPay), row, REGULAR_PAY);
        tableModel.setValueAt(String.format("%.2f", otPay), row, OT_TOTAL);
        tableModel.setValueAt(String.format("%.2f", totalGross), row, TOTAL_GROSS);
        tableModel.setValueAt(String.format("%.2f", federal), row, FEDERAL);
        tableModel.setValueAt(String.format("%.2f", socialSecurity), row, SOCIAL_SECURITY);
        tableModel.setValueAt(String.format("%.2f", medicare), row, MEDICARE);
        tableModel.setValueAt(String.format("%.2f", maryland), row, MD_TAX);
        tableModel.setValueAt(String.format("%.2f", baltimore), row, BC_TAX);
        tableModel.setValueAt(String.format("%.2f", net), row, NET_PAY);
        tableModel.setValueAt(String.format("%.2f", slg), row, SLG_TAX);
        tableModel.setValueAt(String.format("%.2f", deductions), row, TOTAL_DEDUCTIONS);
    }


    // =========================
    // LOAD EMPLOYEE
    // =========================

    public void loadEmployee(Employee employee) {

        // Remove the previous employee's rows before filling the table for this employee.
        tableModel.setRowCount(0);

        fedRateField.setText(employee.fed_rate);

        for (int i = 0; i < PayrollData.PAY_PERIODS.length; i++) {

            Object[] row = {

                    PayrollData.PAY_PERIODS[i][0],
                    PayrollData.PAY_PERIODS[i][1],
                    PayrollData.PAY_PERIODS[i][2],
                    PayrollData.PAY_PERIODS[i][3],
                    PayrollData.PAY_PERIODS[i][4],

                    employee.hours[i],
                    employee.ot_hours[i],
                    employee.hourly_rates[i],
                    employee.ot_rates[i],
                    employee.extra[i],

                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
            };

            tableModel.addRow(row);
            calculateRow(i);
        }
    }


    // =========================
    // SAVE EMPLOYEE
    // =========================

    public void saveEmployee(Employee employee) {

        stopEditing();
        validateFedRate();

        employee.fed_rate = fedRateField.getText().trim();

        // Calculated columns are rebuilt on load, so only editable inputs are stored here.
        for (int i = 0; i < tableModel.getRowCount(); i++) {

            employee.hours[i] = getValue(i, HOURS);
            employee.ot_hours[i] = getValue(i, OT_HOURS);
            employee.hourly_rates[i] = getValue(i, HOURLY_RATE);
            employee.ot_rates[i] = getValue(i, OT_RATE);
            employee.extra[i] = getValue(i, EXTRA);
        }
    }


    // =========================
    // CHANGE HOURLY RATE
    // =========================

    public void changeHourlyRate(int row, String newRate) {

        try {

            double rate = Double.parseDouble(newRate.trim());

            if (rate < 0) {
                return;
            }

            tableModel.setValueAt(newRate.trim(), row, HOURLY_RATE);

            for (int i = row + 1; i < tableModel.getRowCount(); i++) {
                tableModel.setValueAt(newRate.trim(), i, HOURLY_RATE);
                calculateRow(i);
            }

            calculateRow(row);

        } catch (NumberFormatException ignored) {
        }
    }


    // =========================
    // CHANGE OT RATE
    // =========================

    public void changeOTRate(int row, String newRate) {

        try {

            double rate = Double.parseDouble(newRate.trim());

            if (rate < 0) {
                return;
            }

            tableModel.setValueAt(newRate.trim(), row, OT_RATE);

            for (int i = row + 1; i < tableModel.getRowCount(); i++) {
                tableModel.setValueAt(newRate.trim(), i, OT_RATE);
                calculateRow(i);
            }

            calculateRow(row);

        } catch (NumberFormatException ignored) {
        }
    }


    // =========================
    // CALCULATE TOTALS
    // =========================

    public Map<String, Double> calculateTotals(
            EmployeeManager employeeManager,
            boolean[] includedPayPeriods
    ) {

        Map<String, Double> totals = createEmptyTotals();

        for (Employee employee : employeeManager.getEmployees()) {
            addEmployeeTotals(totals, employee, includedPayPeriods);
        }

        return totals;
    }


    // =========================
    // CALCULATE ONE EMPLOYEE
    // =========================

    public Map<String, Double> calculateEmployeeTotals(
            Employee employee,
            boolean[] includedPayPeriods
    ) {

        Map<String, Double> totals = createEmptyTotals();

        addEmployeeTotals(
                totals,
                employee,
                includedPayPeriods
        );

        return totals;
    }


    // =========================
    // ADD EMPLOYEE TOTALS
    // =========================

    private void addEmployeeTotals(
            Map<String, Double> totals,
            Employee employee,
            boolean[] includedPayPeriods
    ) {

        // Reports and checks always calculate from the year currently selected in the table.
        employee.activatePayrollYear(PayrollData.getActiveYear());

        for (int i = 0; i < PayrollData.PAY_PERIODS.length; i++) {

            if (!includedPayPeriods[i]) {
                continue;
            }

            double hours = getNumber(employee.hours[i]);
            double otHours = getNumber(employee.ot_hours[i]);

            double regularPay = calculator.calculateRegularPay(
                    employee.hours[i],
                    employee.hourly_rates[i]
            );

            double otPay = calculator.calculateOTPay(
                    employee.ot_hours[i],
                    employee.ot_rates[i]
            );

            double extra = calculator.calculateExtraPay(employee.extra[i]);

            double totalGross = regularPay + otPay + extra;

            double federal = calculator.calculateFedRate(
                    totalGross,
                    employee.fed_rate
            );

            double socialSecurity = calculator.calculateSocialSecurity(totalGross);
            double medicare = calculator.calculateMedicare(totalGross);
            double maryland = calculator.calculateMaryland(totalGross);
            double baltimore = calculator.calculateBaltimore(totalGross);

            double slg = maryland + baltimore;

            double net = calculator.calculateNet(
                    totalGross,
                    federal,
                    socialSecurity,
                    medicare,
                    maryland,
                    baltimore
            );

            double deductions = totalGross - net;

            addToTotal(totals, "Hours", hours);
            addToTotal(totals, "OT Hours", otHours);
            addToTotal(totals, "Regular Pay", regularPay);
            addToTotal(totals, "OT Pay", otPay);
            addToTotal(totals, "Bonus", extra);
            addToTotal(totals, "Total Gross", totalGross);
            addToTotal(totals, "Federal", federal);
            addToTotal(totals, "Social Security", socialSecurity);
            addToTotal(totals, "Medicare", medicare);
            addToTotal(totals, "MD Tax", maryland);
            addToTotal(totals, "BC Tax", baltimore);
            addToTotal(totals, "SLG Tax", slg);
            addToTotal(totals, "Total Deductions", deductions);
            addToTotal(totals, "Net Pay", net);
        }
    }


    // =========================
    // CREATE EMPTY TOTALS
    // =========================

    private Map<String, Double> createEmptyTotals() {

        Map<String, Double> totals = new LinkedHashMap<>();

        totals.put("Hours", 0.0);
        totals.put("OT Hours", 0.0);
        totals.put("Regular Pay", 0.0);
        totals.put("OT Pay", 0.0);
        totals.put("Bonus", 0.0);
        totals.put("Total Gross", 0.0);
        totals.put("Federal", 0.0);
        totals.put("Social Security", 0.0);
        totals.put("Medicare", 0.0);
        totals.put("MD Tax", 0.0);
        totals.put("BC Tax", 0.0);
        totals.put("SLG Tax", 0.0);
        totals.put("Total Deductions", 0.0);
        totals.put("Net Pay", 0.0);

        return totals;
    }


    // =========================
    // ADD TO TOTAL
    // =========================

    private void addToTotal(
            Map<String, Double> totals,
            String name,
            double amount
    ) {

        totals.put(name, totals.get(name) + amount);
    }


    // =========================
    // GET NUMBER
    // =========================

    private double getNumber(String value) {

        // Empty cells count as zero so one blank field cannot stop totals from being calculated.
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }

        try {
            return Double.parseDouble(value.trim());

        } catch (NumberFormatException error) {
            return 0;
        }
    }


    // =========================
    // GET VALUE
    // =========================

    public String getValue(int row, int column) {

        Object value = tableModel.getValueAt(row, column);

        if (value == null) {
            return "";
        }

        return value.toString();
    }


    // =========================
    // STOP EDITING
    // =========================

    public void stopEditing() {

        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
    }
}
