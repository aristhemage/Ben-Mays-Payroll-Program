import javax.swing.SwingUtilities;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            try {

                ArrayList<Employee> employees =
                        PayrollAPI.getEmployees();

                EmployeeManager employee_manager =
                        new EmployeeManager();

                for (Employee employee : employees) {
                    employee_manager.addEmployee(employee);
                }

                // Make sure the application has an employee
                if (employee_manager.getEmployeeCount() == 0) {

                    employee_manager.addEmployee(
                            new Employee(
                                    "No Employees",
                                    "",
                                    "",
                                    ""
                            )
                    );
                }

                new PayrollGUI(employee_manager);

            } catch (Exception e) {

                e.printStackTrace();

            }

        });

    }
}