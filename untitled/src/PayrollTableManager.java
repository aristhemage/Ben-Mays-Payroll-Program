import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;

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

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JFrame frame;
    private final JPanel fedRatePanel;
    private final JTextField fedRateField;

    private final PayrollCalculator calculator =
            new PayrollCalculator();

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

        // Recalculate when Enter is pressed
        fedRateField.addActionListener(e -> {
            validateFedRate();
            calculateAllRows();
        });

        // Recalculate when the field loses focus
        fedRateField.addFocusListener(
                new java.awt.event.FocusAdapter() {

                    @Override
                    public void focusLost(
                            java.awt.event.FocusEvent e) {

                        validateFedRate();
                        calculateAllRows();
                    }
                }
        );

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

        tableModel = new DefaultTableModel(
                columnNames,
                0
        ) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {

                return column >= HOURS &&
                        column <= EXTRA;
            }
        };

        table = new JTable(tableModel);

        setupValidation();
    }

    // =========================
    // GET TABLE
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

    // =========================
    // GET TABLE MODEL
    // =========================

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    // =========================
    // VALIDATION SETUP
    // =========================

    private void setupValidation() {

        table.getDefaultEditor(Object.class)
                .addCellEditorListener(
                        new CellEditorListener() {

                            @Override
                            public void editingStopped(
                                    ChangeEvent e) {

                                validateCell();
                            }

                            @Override
                            public void editingCanceled(
                                    ChangeEvent e) {
                            }
                        }
                );
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

        if (column < HOURS ||
                column > EXTRA) {

            return;
        }

        String value = getValue(row, column).trim();

        if (value.isEmpty()) {
            calculateRow(row);
            return;
        }

        try {

            double number =
                    Double.parseDouble(value);

            if (number < 0) {

                JOptionPane.showMessageDialog(
                        frame,
                        "Please enter a number greater than or equal to 0."
                );

                tableModel.setValueAt(
                        "",
                        row,
                        column
                );
            }

        } catch (NumberFormatException error) {

            JOptionPane.showMessageDialog(
                    frame,
                    "Invalid input.\nPlease enter a valid number."
            );

            tableModel.setValueAt(
                    "",
                    row,
                    column
            );
        }

        calculateRow(row);
    }

    // =========================
    // VALIDATE FED RATE
    // =========================

    private void validateFedRate() {

        String value =
                fedRateField.getText().trim();

        if (value.isEmpty()) {

            fedRateField.setText("0");
            return;
        }

        try {

            double rate =
                    Double.parseDouble(value);

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

        stopEditing();

        for (int row = 0;
             row < tableModel.getRowCount();
             row++) {

            calculateRow(row);
        }
    }

    // =========================
    // CALCULATE ROW
    // =========================

    private void calculateRow(int row) {

        double regularPay =
                calculator.calculateRegularPay(
                        getValue(row, HOURS),
                        getValue(row, HOURLY_RATE)
                );

        double otPay =
                calculator.calculateOTPay(
                        getValue(row, OT_HOURS),
                        getValue(row, OT_RATE)
                );

        double extra =
                calculator.calculateExtraPay(
                        getValue(row, EXTRA)
                );

        double totalGross =
                regularPay + otPay + extra;

        double federal =
                calculator.calculateFedRate(
                        totalGross,
                        fedRateField.getText()
                );

        tableModel.setValueAt(
                String.format("%.2f", regularPay),
                row,
                REGULAR_PAY
        );

        tableModel.setValueAt(
                String.format("%.2f", otPay),
                row,
                OT_TOTAL
        );

        tableModel.setValueAt(
                String.format("%.2f", totalGross),
                row,
                TOTAL_GROSS
        );

        tableModel.setValueAt(
                String.format("%.2f", federal),
                row,
                FEDERAL
        );
    }

    // =========================
    // LOAD EMPLOYEE
    // =========================

    public void loadEmployee(Employee employee) {

        tableModel.setRowCount(0);

        fedRateField.setText(employee.fed_rate);

        for (int i = 0;
             i < PayrollData.PAY_PERIODS.length;
             i++) {

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

        employee.fed_rate =
                fedRateField.getText().trim();

        for (int i = 0;
             i < tableModel.getRowCount();
             i++) {

            employee.hours[i] =
                    getValue(i, HOURS);

            employee.ot_hours[i] =
                    getValue(i, OT_HOURS);

            employee.hourly_rates[i] =
                    getValue(i, HOURLY_RATE);

            employee.ot_rates[i] =
                    getValue(i, OT_RATE);

            employee.extra[i] =
                    getValue(i, EXTRA);
        }
    }

    // =========================
    // CHANGE HOURLY RATE
    // =========================

    public void changeHourlyRate(
            int row,
            String newRate) {

        try {

            double rate =
                    Double.parseDouble(
                            newRate.trim()
                    );

            if (rate < 0) {
                return;
            }

            tableModel.setValueAt(
                    newRate.trim(),
                    row,
                    HOURLY_RATE
            );

            for (int i = row + 1;
                 i < tableModel.getRowCount();
                 i++) {

                tableModel.setValueAt(
                        newRate.trim(),
                        i,
                        HOURLY_RATE
                );

                calculateRow(i);
            }

            calculateRow(row);

        } catch (NumberFormatException ignored) {
        }
    }

    // =========================
    // CHANGE OT RATE
    // =========================

    public void changeOTRate(
            int row,
            String newRate) {

        try {

            double rate =
                    Double.parseDouble(
                            newRate.trim()
                    );

            if (rate < 0) {
                return;
            }

            tableModel.setValueAt(
                    newRate.trim(),
                    row,
                    OT_RATE
            );

            for (int i = row + 1;
                 i < tableModel.getRowCount();
                 i++) {

                tableModel.setValueAt(
                        newRate.trim(),
                        i,
                        OT_RATE
                );

                calculateRow(i);
            }

            calculateRow(row);

        } catch (NumberFormatException ignored) {
        }
    }

    // =========================
    // GET VALUE
    // =========================

    public String getValue(
            int row,
            int column) {

        Object value =
                tableModel.getValueAt(
                        row,
                        column
                );

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

            table.getCellEditor()
                    .stopCellEditing();
        }
    }
}