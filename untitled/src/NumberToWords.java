public class NumberToWords {

    private static final String[] ONES = {
            "", "One", "Two", "Three", "Four",
            "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen",
            "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] TENS = {
            "", "", "Twenty", "Thirty", "Forty",
            "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    public static String convert(double amount) {

        int dollars = (int) amount;

        int cents = (int) Math.round((amount - dollars) * 100);

        // Handle rounding to 100 cents
        if (cents == 100) {
            dollars++;
            cents = 0;
        }

        String words = convertNumber(dollars);

        return words + " and "
                + String.format("%02d", cents)
                + "/100";
    }

    private static String convertNumber(long number) {

        if (number == 0) {
            return "Zero";
        }

        if (number < 20) {
            return ONES[(int) number];
        }

        if (number < 100) {

            return TENS[(int) (number / 10)]
                    + (number % 10 != 0
                    ? " " + ONES[(int) (number % 10)]
                    : "");
        }

        if (number < 1000) {

            return ONES[(int) (number / 100)]
                    + " Hundred"
                    + (number % 100 != 0
                    ? " " + convertNumber(number % 100)
                    : "");
        }

        if (number < 1_000_000) {

            return convertNumber(number / 1000)
                    + " Thousand"
                    + (number % 1000 != 0
                    ? " " + convertNumber(number % 1000)
                    : "");
        }


        return "Error: Number way too big";
    }
}