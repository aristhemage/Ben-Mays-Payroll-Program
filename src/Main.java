import javax.swing.SwingUtilities;

public class Main {


    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            EmployeeManager employee_manager = FileData.load();

            if(employee_manager == null) {
                employee_manager = new EmployeeManager();

                employee_manager.addEmployee(new Employee("Test Employee (Remove when done)", "Dummy Address", "Dummy City", "Dummy Zip #"));
            }

            new PayrollGUI(employee_manager);
        });

    }
}