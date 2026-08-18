public class PayrollCalculator {

    // =========================
    // PARSE VALUE
    // =========================

    private double parseDouble(String value) {

        if (value == null || value.trim().isEmpty()) {
            return 0;
        }

        return Double.parseDouble(value);
    }

    // =========================
    // REGULAR PAY
    // =========================

    public double calculateRegularPay(
            String hours,
            String hourly_rate) {

        return parseDouble(hours)
                * parseDouble(hourly_rate);
    }

    // =========================
    // OT PAY
    // =========================

    public double calculateOTPay(
            String ot_hours,
            String ot_rate) {

        return parseDouble(ot_hours)
                * parseDouble(ot_rate);
    }

    // =========================
    // EXTRA PAY
    // =========================

    public double calculateExtraPay(
            String extra) {

        return parseDouble(extra);
    }

    // =========================
    // TOTAL GROSS
    // =========================

    public double calculateGrossPay(
            String hours,
            String hourly_rate,
            String ot_hours,
            String ot_rate,
            String extra) {

        return calculateRegularPay(
                hours,
                hourly_rate
        )
                + calculateOTPay(
                ot_hours,
                ot_rate
        )
                + calculateExtraPay(
                extra
        );
    }

    // =========================
    // TOTAL FEDERAL
    // =========================
    public double calculateFedRate(
            double gross,
            String fed_rate
    ){
        return gross*parseDouble(fed_rate);
    }
}