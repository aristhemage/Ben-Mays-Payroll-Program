public class Employee {

    String name;
    int num_rows = 26;

    String[] hours = new String[num_rows];
    String[] ot_hours = new String[num_rows];

    String[] hourly_rates = new String[num_rows];
    String[] hourly_rate_changed_on = new String[num_rows];

    String[] ot_rates = new String[num_rows];
    String[] ot_rate_changed_on = new String[num_rows];

    String[] extra = new String[num_rows];

    public Employee(String name) {

        this.name = name;

        // Make the array blank by default on all categories
        for (int i = 0; i < num_rows; i++) {

            hours[i] = "";
            ot_hours[i] = "";

            hourly_rates[i] = "";
            hourly_rate_changed_on[i] = "";

            ot_rates[i] = "";
            ot_rate_changed_on[i] = "";

            extra[i] = "";
        }
    }
}