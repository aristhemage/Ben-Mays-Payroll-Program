import java.util.LinkedHashMap;
import java.util.Map;

public class Employee {
    // =========================
    // VARIABLE DEFINITION
    // =========================

    // Declare vars that Employee class uses
    public String zip;
    public String city;
    String name;
    int num_rows = 26;
    public String mongoId;

    // Every year has its own set of 26 payroll rows.
    // Reminder: A map stores a label, such as "2026", together with that year's payroll data. Like an unorganized array.
    // LinkedHashMap remembers the order years were added, such as 2026, then 2027.
    public Map<String, PayrollYearData> payrollYears = new LinkedHashMap<>();
    public String activePayrollYear = "2026";

    // Creates arrays with the size of 26 weeks.
    transient String[] hours = new String[num_rows];
    transient String[] ot_hours = new String[num_rows];

    transient String[] hourly_rates = new String[num_rows];
    transient String[] hourly_rate_changed_on = new String[num_rows];

    transient String[] ot_rates = new String[num_rows];
    transient String[] ot_rate_changed_on = new String[num_rows];

    transient String[] extra = new String[num_rows];

    // More variables made.
    String fed_rate;
    String address;

    // Require when new Employee is created, it has these variables.
    public Employee(String name, String address, String city, String zip) {

        this.name = name;

        fed_rate = "0";
        this.address = address;
        this.city = city;
        this.zip = zip;

        // Put this employee's payroll rows into the year currently selected in the program.
        // For example, if 2027 is selected, use this employee's 2027 payroll data.
        activatePayrollYear(PayrollData.getActiveYear());
    }

    // =========================
    // FUNCTIONS
    // =========================

    public void activatePayrollYear(int year) {

        // Convert the year number into text so it can be used as the map key,
        // then get that year's saved payroll data or create it if it does not exist.
        String yearKey = String.valueOf(year);
        PayrollYearData yearData = getOrCreatePayrollYear(yearKey);

        activePayrollYear = yearKey;

        // Set the array's data to the saved years info.
        hours = yearData.hours;
        ot_hours = yearData.ot_hours;
        hourly_rates = yearData.hourly_rates;
        hourly_rate_changed_on = yearData.hourly_rate_changed_on;
        ot_rates = yearData.ot_rates;
        ot_rate_changed_on = yearData.ot_rate_changed_on;
        extra = yearData.extra;
    }

    // Gets the requested year's data, creating a blank 26-pay-period record when needed.
    private PayrollYearData getOrCreatePayrollYear(String yearKey) {

        // If blank, make a new year (Shouldn't really happen, but just in case)
        if (payrollYears == null) {
            payrollYears = new LinkedHashMap<>();
        }

        PayrollYearData yearData = payrollYears.get(yearKey);
        // If blank make a new year (Again, shouldn't happen, but here we are)
        if (yearData == null) {
            yearData = new PayrollYearData();
            // Determine if its broken data, if so, fix (Had an issue with cloud data, so I'm fixing it here)
            yearData.normalize();
            payrollYears.put(yearKey, yearData);
        } else {
            // Same as above comment
            yearData.normalize();
        }

        return yearData;
    }

    // =========================
    // BASIC GETTERS
    // =========================
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zip;
    }

    public void setZipCode(String zip) {
        this.zip = zip;
    }
}
