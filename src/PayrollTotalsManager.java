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
    // SETUP ALL EMPLOYEE BUTTON
    // =========================

    public void setupViewTotalsButton(
            JButton button) {

        button.addActionListener(
                e -> showTotalsMenu(false)
        );
    }


    // =========================
    // SETUP INDIVIDUAL BUTTON
    // =========================

    public void setupViewIndividualTotalsButton(
            JButton button) {

        button.addActionListener(
                e -> showTotalsMenu(true)
        );
    }


    // =========================
    // SHOW TOTALS MENU
    // =========================
    //
    // individual = false
    //     Calculate all employees
    //
    // individual = true
    //     Calculate current employee only
    // =========================

    private void showTotalsMenu(
            boolean individual
    ) {

        tableManager.saveEmployee(
                employeeManager.getCurrentEmployee()
        );


        String[] options = {
                "Per Pay Period",
                "Per Month",
                "Per Quarter",
                "YTD"
        };


        String title;

        if (individual) {

            title =
                    "View Individual Employee Totals";

        } else {

            title =
                    "View All Employee Totals";
        }


        String choice =
                (String) JOptionPane.showInputDialog(
                        frame,
                        "What would you like to see?",
                        title,
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

                showPayPeriodTotals(
                        individual
                );

                break;


            case "Per Month":

                showMonthTotals(
                        individual
                );

                break;


            case "Per Quarter":

                showQuarterTotals(
                        individual
                );

                break;


            case "YTD":

                showYTDTotals(
                        individual
                );

                break;
        }
    }


    // =========================
    // PAY PERIOD TOTALS
    // =========================

    private void showPayPeriodTotals(
            boolean individual
    ) {

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


        for (
                int i = 0;
                i < options.length;
                i++
        ) {

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
                calculateTotals(
                        individual,
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

    private void showMonthTotals(
            boolean individual
    ) {

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


        for (
                int i = 0;
                i < PayrollData.PAY_PERIODS.length;
                i++
        ) {

            String paidOn =
                    PayrollData.PAY_PERIODS[i][3];


            if (
                    getMonthYear(paidOn)
                            .equals(selected)
            ) {

                included[i] = true;
            }
        }


        Map<String, Double> totals =
                calculateTotals(
                        individual,
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

    private void showQuarterTotals(
            boolean individual
    ) {

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


        for (
                int i = 0;
                i < PayrollData.PAY_PERIODS.length;
                i++
        ) {

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
                calculateTotals(
                        individual,
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

    private void showYTDTotals(
            boolean individual
    ) {

        String[] yearOptions =
                getAvailableYears();


        if (yearOptions.length == 0) {

            JOptionPane.showMessageDialog(
                    frame,
                    "No pay periods are available."
            );

            return;
        }


        String selectedYear =
                (String) JOptionPane.showInputDialog(
                        frame,
                        "Select a year:",
                        "Year To Date Totals",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        yearOptions,
                        yearOptions[0]
                );


        if (selectedYear == null) {
            return;
        }


        boolean[] included =
                new boolean[
                        PayrollData.PAY_PERIODS.length
                        ];


        for (
                int i = 0;
                i < PayrollData.PAY_PERIODS.length;
                i++
        ) {

            String paidOn =
                    PayrollData.PAY_PERIODS[i][3];


            String year =
                    getFullYear(paidOn);


            if (year.equals(selectedYear)) {

                included[i] = true;
            }
        }


        Map<String, Double> totals =
                calculateTotals(
                        individual,
                        included
                );


        showTotals(
                selectedYear +
                        " Year To Date Totals",
                totals
        );
    }


    // =========================
    // CALCULATE TOTALS
    // =========================
    //
    // This is the shared function.
    //
    // individual = true:
    //     Current employee only
    //
    // individual = false:
    //     All employees
    // =========================

    private Map<String, Double> calculateTotals(
            boolean individual,
            boolean[] includedPayPeriods
    ) {

        if (individual) {

            return tableManager.calculateEmployeeTotals(
                    employeeManager.getCurrentEmployee(),
                    includedPayPeriods
            );
        }


        return tableManager.calculateTotals(
                employeeManager,
                includedPayPeriods
        );
    }


    // =========================
    // GET AVAILABLE MONTHS
    // =========================

    private String[] getAvailableMonths() {

        LinkedHashSet<String> months =
                new LinkedHashSet<>();


        for (
                int i = 0;
                i < PayrollData.PAY_PERIODS.length;
                i++
        ) {

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
            String date
    ) {

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
                " " +
                year;
    }


    // =========================
    // GET AVAILABLE QUARTERS
    // =========================

    private String[] getAvailableQuarters() {

        LinkedHashSet<String> quarters =
                new LinkedHashSet<>();


        for (
                int i = 0;
                i < PayrollData.PAY_PERIODS.length;
                i++
        ) {

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
            String quarter
    ) {

        return "Quarter " +
                quarter +
                " " +
                getFullYear(paidOn);
    }


    // =========================
    // GET AVAILABLE YEARS
    // =========================

    private String[] getAvailableYears() {

        LinkedHashSet<String> years =
                new LinkedHashSet<>();


        for (
                int i = 0;
                i < PayrollData.PAY_PERIODS.length;
                i++
        ) {

            years.add(
                    getFullYear(
                            PayrollData.PAY_PERIODS[i][3]
                    )
            );
        }


        return years.toArray(
                new String[0]
        );
    }


    // =========================
    // GET FULL YEAR
    // =========================

    private String getFullYear(
            String date
    ) {

        String[] parts =
                date.split("/");

        return "20" + parts[2];
    }


    // =========================
    // SHOW TOTALS
    // =========================

    private void showTotals(
            String title,
            Map<String, Double> totals
    ) {

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
                                totals.get("Bonus")
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