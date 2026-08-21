
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PayrollData {

    public static final String[][] PAY_PERIODS = createPayPeriods();

    // Calculate the payroll dates and pay dates
    public static String[][] createPayPeriods() {
        String[][] payPeriods = new String[26][5];

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yy");
        int lastYear = LocalDate.now().getYear() - 1;
        LocalDate startDate = LocalDate.of(lastYear, 12, 21);

        for (int i = 0; i < 26; i++) {
            LocalDate periodStart = startDate.plusWeeks(i * 2);
            LocalDate periodEnd = periodStart.plusDays(13);
            LocalDate payDate = periodEnd.plusDays(5);

            int quarter = (i / 6) + 1;

            payPeriods[i][0] = String.valueOf(i + 1);
            payPeriods[i][1] = periodStart.format(formatter);
            payPeriods[i][2] = periodEnd.format(formatter);
            payPeriods[i][3] = payDate.format(formatter);
            payPeriods[i][4] = String.valueOf(quarter);
        }

        return payPeriods;
    }
}