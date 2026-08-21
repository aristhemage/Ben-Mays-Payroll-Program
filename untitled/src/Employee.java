import java.io.Serializable;
public class Employee implements Serializable{
    private static final long serialVersionUID = 1L;
    public String zip;
    public String city;
    String name;
    int num_rows = 26;

    String[] hours = new String[num_rows];
    String[] ot_hours = new String[num_rows];

    String[] hourly_rates = new String[num_rows];
    String[] hourly_rate_changed_on = new String[num_rows];

    String[] ot_rates = new String[num_rows];
    String[] ot_rate_changed_on = new String[num_rows];

    String[] extra = new String[num_rows];
    String fed_rate;
    String address;

    public Employee(String name, String address, String city, String zip) {

        this.name = name;

        fed_rate = "0";
        this.address = address;
        this.city = city;
        this.zip = zip;

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