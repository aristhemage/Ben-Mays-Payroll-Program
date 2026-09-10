
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PayrollData {

    // The year currently displayed in the payroll table.  It can be changed without deleting another year.
    private static int activeYear = Math.max(2026, LocalDate.now().getYear());

    public static String[][] PAY_PERIODS = createPayPeriods(activeYear);

    /** Returns the year currently selected in the payroll program. */
    public static int getActiveYear() {

        return activeYear;
    }

    /** Changes the table/check/report calendar to a different payroll year. */
    public static void setActiveYear(int year) {

        activeYear = year;
        PAY_PERIODS = createPayPeriods(year);
    }

    // Calculate the 26 payroll dates and pay dates for one selected calendar year.
    public static String[][] createPayPeriods(int year) {
        String[][] payPeriods = new String[26][5];

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yy");
        // The first payroll period starts in December so its payment date falls in this payroll year.
        LocalDate startDate = LocalDate.of(year - 1, 12, 21);

        for (int i = 0; i < 26; i++) {
            LocalDate periodStart = startDate.plusWeeks(i * 2);
            LocalDate periodEnd = periodStart.plusDays(13);
            LocalDate payDate = periodEnd.plusDays(5);

            // A quarter belongs to the calendar month in which the check is paid.
            int quarter = ((payDate.getMonthValue() - 1) / 3) + 1;

            // Each row stores: number, start date, end date, paid-on date, and quarter.
            payPeriods[i][0] = String.valueOf(i + 1);
            payPeriods[i][1] = periodStart.format(formatter);
            payPeriods[i][2] = periodEnd.format(formatter);
            payPeriods[i][3] = payDate.format(formatter);
            payPeriods[i][4] = String.valueOf(quarter);
        }

        return payPeriods;
    }
}
