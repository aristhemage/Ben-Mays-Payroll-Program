/** Holds one employee's 26 pay-period entries for one payroll year. */
public class PayrollYearData {
    private static final int PAY_PERIOD_COUNT = 26;

    // These seven arrays use the same index, so index 0 always means pay period 1.
    public String[] hours;
    public String[] ot_hours;
    public String[] hourly_rates;
    public String[] hourly_rate_changed_on;
    public String[] ot_rates;
    public String[] ot_rate_changed_on;
    public String[] extra;

    public PayrollYearData() {

        hours = blankArray();
        ot_hours = blankArray();
        hourly_rates = blankArray();
        hourly_rate_changed_on = blankArray();
        ot_rates = blankArray();
        ot_rate_changed_on = blankArray();
        extra = blankArray();
    }

    /** Makes every payroll field safe to use after it is read from the cloud. */
    public void normalize() {

        hours = normalize(hours);
        ot_hours = normalize(ot_hours);
        hourly_rates = normalize(hourly_rates);
        hourly_rate_changed_on = normalize(hourly_rate_changed_on);
        ot_rates = normalize(ot_rates);
        ot_rate_changed_on = normalize(ot_rate_changed_on);
        extra = normalize(extra);
    }

    private static String[] blankArray() {

        String[] values = new String[PAY_PERIOD_COUNT];

        for (int index = 0; index < values.length; index++) {
            values[index] = "";
        }

        return values;
    }

    private static String[] normalize(String[] savedValues) {

        String[] values = blankArray();

        if (savedValues != null) {
            // Keep the saved values that fit and leave new/missing slots blank.
            System.arraycopy(savedValues, 0, values, 0, Math.min(savedValues.length, values.length));
        }

        for (int index = 0; index < values.length; index++) {
            if (values[index] == null) {
                values[index] = "";
            }
        }

        return values;
    }
}
