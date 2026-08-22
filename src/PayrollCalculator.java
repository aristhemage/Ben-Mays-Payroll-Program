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

    public double calculateRegularPay(String hours, String hourly_rate) {
        return parseDouble(hours) * parseDouble(hourly_rate);
    }


    // =========================
    // OT PAY
    // =========================

    public double calculateOTPay(String ot_hours, String ot_rate) {
        return parseDouble(ot_hours) * parseDouble(ot_rate);
    }


    // =========================
    // EXTRA PAY
    // =========================

    public double calculateExtraPay(String extra) {
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

        return calculateRegularPay(hours, hourly_rate)
                + calculateOTPay(ot_hours, ot_rate)
                + calculateExtraPay(extra);
    }


    // =========================
    // TOTAL FEDERAL
    // =========================

    public double calculateFedRate(double gross, String fed_rate) {
        return gross * parseDouble(fed_rate) / 100;
    }


    // =========================
    // TOTAL SOCIAL SECURITY
    // =========================

    public double calculateSocialSecurity(double gross) {
        return gross * 0.062;
    }


    // =========================
    // TOTAL MEDICARE
    // =========================

    public double calculateMedicare(double gross) {
        return gross * 0.0145;
    }


    // =========================
    // TOTAL MARYLAND TAX
    // =========================

    public double calculateMaryland(double gross) {

        double result = 0;

        double[] thresholds = {
                0, 1000, 2000, 3000, 100000,
                125000, 150000, 250000, 500000, 1000000
        };

        double[] rates = {
                0.02, 0.01, 0.01, 0.0075, 0.0025,
                0.0025, 0.0025, 0.0025, 0.005, 0.0025
        };

        for (int i = 0; i < thresholds.length; i++) {

            if (gross > thresholds[i]) {
                result += (gross - thresholds[i]) * rates[i];
            }
        }

        return result;
    }


    // =========================
    // TOTAL BALTIMORE
    // =========================

    public double calculateBaltimore(double gross) {
        return gross * 0.032;
    }


    // =========================
    // TOTAL NET
    // =========================

    public double calculateNet(
            double gross,
            double fed,
            double soc,
            double med,
            double md,
            double bc) {

        return gross
                - fed
                - soc
                - med
                - md
                - bc;
    }
}