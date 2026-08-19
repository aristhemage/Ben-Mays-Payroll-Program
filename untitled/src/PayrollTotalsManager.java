import javax.swing.*;
import java.util.LinkedHashSet;
import java.util.Map;

public class PayrollTotalsManager {

    private final JFrame frame;
    private final EmployeeManager employeeManager;
    private final PayrollTableManager tableManager;

    // =========================
    // CONSTRUCTOR
    // =========================

    public PayrollTotalsManager(
            JFrame frame,
            EmployeeManager employeeManager,
            PayrollTableManager tableManager) {

        this.frame = frame;
        this.employeeManager = employeeManager;
        this.tableManager = tableManager;
    }

    // =========================
    // SETUP VIEW TOTALS BUTTON
    // =========================

    public void setupViewTotalsButton(
            JButton button) {

        button.addActionListener(e -> {

            tableManager.saveEmployee(
                    employeeManager.getCurrentEmployee()
            );

            String[] options = {
                    "Per Pay Period",
                    "Per Month",
                    "Per Quarter",
                    "YTD"
            };

            String choice =
                    (String) JOptionPane.showInputDialog(
                            frame,
                            "What would you like to see?",
                            "View Totals",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]
                    );

            if (choice == null) {
                return;
            }

            switch (choice) {

                case "Per Pay Period":
                    showPayPeriodTotals();
                    break;

                case "Per Month":
                    showMonthTotals();
                    break;

                case "Per Quarter":
                    showQuarterTotals();
                    break;

                case "YTD":
                    showYTDTotals();
                    break;
            }
        });
    }

    // =========================
    // PAY PERIOD TOTALS
    // =========================

    private void showPayPeriodTotals() {

        String[] options =
                new String[
                        PayrollData.PAY_PERIODS.length
                        ];

        for (int i = 0;
             i < PayrollData.PAY_PERIODS.length;
             i++) {

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
                        "Per Pay Period Totals",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]
                );

        if (selected == null) {
            return;
        }

        int selectedIndex = 0;

        for (int i = 0;
             i < options.length;
             i++) {

            if (options[i].equals(selected)) {

                selectedIndex = i;
                break;
            }
        }

        boolean[] included =
                new boolean[
                        PayrollData.PAY_PERIODS.length
                        ];

        included[selectedIndex] = true;

        Map<String, Double> totals =
                tableManager.calculateTotals(
                        employeeManager,
                        included
                );

        showTotals(
                "Totals for Pay Period " +
                        PayrollData.PAY_PERIODS[selectedIndex][0],
                totals
        );
    }

    // =========================
    // MONTH TOTALS
    // =========================

    private void showMonthTotals() {

        String[] monthOptions =
                getAvailableMonths();

        if (monthOptions.length == 0) {

            JOptionPane.showMessageDialog(
                    frame,
                    "No pay periods are available."
            );

            return;
        }

        String selected =
                (String) JOptionPane.showInputDialog(
                        frame,
                        "Select a month:",
                        "Per Month Totals",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        monthOptions,
                        monthOptions[0]
                );

        if (selected == null) {
            return;
        }

        boolean[] included =
                new boolean[
                        PayrollData.PAY_PERIODS.length
                        ];

        for (int i = 0;
             i < PayrollData.PAY_PERIODS.length;
             i++) {

            String paidOn =
                    PayrollData.PAY_PERIODS[i][3];

            if (getMonthYear(paidOn)
                    .equals(selected)) {

                included[i] = true;
            }
        }

        Map<String, Double> totals =
                tableManager.calculateTotals(
                        employeeManager,
                        included
                );

        showTotals(
                "Totals for " + selected,
                totals
        );
    }

    // =========================
    // QUARTER TOTALS
    // =========================

    private void showQuarterTotals() {

        String[] quarterOptions =
                getAvailableQuarters();

        if (quarterOptions.length == 0) {

            JOptionPane.showMessageDialog(
                    frame,
                    "No pay periods are available."
            );

            return;
        }

        String selected =
                (String) JOptionPane.showInputDialog(
                        frame,
                        "Select a quarter:",
                        "Per Quarter Totals",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        quarterOptions,
                        quarterOptions[0]
                );

        if (selected == null) {
            return;
        }

        boolean[] included =
                new boolean[
                        PayrollData.PAY_PERIODS.length
                        ];

        for (int i = 0;
             i < PayrollData.PAY_PERIODS.length;
             i++) {

            String quarterYear =
                    getQuarterYear(
                            PayrollData.PAY_PERIODS[i][3],
                            PayrollData.PAY_PERIODS[i][4]
                    );

            if (quarterYear.equals(selected)) {

                included[i] = true;
            }
        }

        Map<String, Double> totals =
                tableManager.calculateTotals(
                        employeeManager,
                        included
                );

        showTotals(
                "Totals for " + selected,
                totals
        );
    }

    // =========================
    // YTD TOTALS
    // =========================

    private void showYTDTotals() {

        String latestYear =
                getLatestYear();

        boolean[] included =
                new boolean[
                        PayrollData.PAY_PERIODS.length
                        ];

        for (int i = 0;
             i < PayrollData.PAY_PERIODS.length;
             i++) {

            String paidOn =
                    PayrollData.PAY_PERIODS[i][3];

            String year =
                    getFullYear(paidOn);

            if (year.equals(latestYear)) {

                included[i] = true;
            }
        }

        Map<String, Double> totals =
                tableManager.calculateTotals(
                        employeeManager,
                        included
                );

        showTotals(
                latestYear + " Year To Date Totals",
                totals
        );
    }

    // =========================
    // GET AVAILABLE MONTHS
    // =========================

    private String[] getAvailableMonths() {

        LinkedHashSet<String> months =
                new LinkedHashSet<>();

        for (int i = 0;
             i < PayrollData.PAY_PERIODS.length;
             i++) {

            months.add(
                    getMonthYear(
                            PayrollData.PAY_PERIODS[i][3]
                    )
            );
        }

        return months.toArray(
                new String[0]
        );
    }

    // =========================
    // GET MONTH YEAR
    // =========================

    private String getMonthYear(
            String date) {

        String[] parts =
                date.split("/");

        int month =
                Integer.parseInt(parts[0]);

        String year =
                "20" + parts[2];

        String[] monthNames = {
                "January",
                "February",
                "March",
                "April",
                "May",
                "June",
                "July",
                "August",
                "September",
                "October",
                "November",
                "December"
        };

        return monthNames[month - 1] +
                " " + year;
    }

    // =========================
    // GET AVAILABLE QUARTERS
    // =========================

    private String[] getAvailableQuarters() {

        LinkedHashSet<String> quarters =
                new LinkedHashSet<>();

        for (int i = 0;
             i < PayrollData.PAY_PERIODS.length;
             i++) {

            quarters.add(
                    getQuarterYear(
                            PayrollData.PAY_PERIODS[i][3],
                            PayrollData.PAY_PERIODS[i][4]
                    )
            );
        }

        return quarters.toArray(
                new String[0]
        );
    }

    // =========================
    // GET QUARTER YEAR
    // =========================

    private String getQuarterYear(
            String paidOn,
            String quarter) {

        return "Quarter " +
                quarter +
                " " +
                getFullYear(paidOn);
    }

    // =========================
    // GET FULL YEAR
    // =========================

    private String getFullYear(
            String date) {

        String[] parts =
                date.split("/");

        return "20" + parts[2];
    }

    // =========================
    // GET LATEST YEAR
    // =========================

    private String getLatestYear() {

        int latestYear = 0;

        for (int i = 0;
             i < PayrollData.PAY_PERIODS.length;
             i++) {

            String year =
                    getFullYear(
                            PayrollData.PAY_PERIODS[i][3]
                    );

            int yearNumber =
                    Integer.parseInt(year);

            if (yearNumber > latestYear) {

                latestYear = yearNumber;
            }
        }

        return String.valueOf(latestYear);
    }

    // =========================
    // SHOW TOTALS
    // =========================

    private void showTotals(
            String title,
            Map<String, Double> totals) {

        String message =

                "Hours: " +
                        String.format(
                                "%.2f",
                                totals.get("Hours")
                        ) +

                        "\nOT Hours: " +
                        String.format(
                                "%.2f",
                                totals.get("OT Hours")
                        ) +

                        "\n\nRegular Pay: $" +
                        String.format(
                                "%.2f",
                                totals.get("Regular Pay")
                        ) +

                        "\nOT Pay: $" +
                        String.format(
                                "%.2f",
                                totals.get("OT Pay")
                        ) +

                        "\nExtra: $" +
                        String.format(
                                "%.2f",
                                totals.get("Extra")
                        ) +

                        "\nTotal Gross: $" +
                        String.format(
                                "%.2f",
                                totals.get("Total Gross")
                        ) +

                        "\n\nFederal: $" +
                        String.format(
                                "%.2f",
                                totals.get("Federal")
                        ) +

                        "\nSocial Security: $" +
                        String.format(
                                "%.2f",
                                totals.get("Social Security")
                        ) +

                        "\nMedicare: $" +
                        String.format(
                                "%.2f",
                                totals.get("Medicare")
                        ) +

                        "\nMD Tax: $" +
                        String.format(
                                "%.2f",
                                totals.get("MD Tax")
                        ) +

                        "\nBC Tax: $" +
                        String.format(
                                "%.2f",
                                totals.get("BC Tax")
                        ) +

                        "\nSLG Tax: $" +
                        String.format(
                                "%.2f",
                                totals.get("SLG Tax")
                        ) +

                        "\nTotal Deductions: $" +
                        String.format(
                                "%.2f",
                                totals.get("Total Deductions")
                        ) +

                        "\n\nNet Pay: $" +
                        String.format(
                                "%.2f",
                                totals.get("Net Pay")
                        );

        JOptionPane.showMessageDialog(
                frame,
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}