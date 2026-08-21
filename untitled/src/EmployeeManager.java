import java.io.Serializable;
import java.util.ArrayList;

public class EmployeeManager implements Serializable {

    private static final long serialVersionUID = 1L;
    private final ArrayList<Employee> employees = new ArrayList<>();

    private int current_employee_index = 0;

    // =========================
    // ADD EMPLOYEE
    // =========================

    public void addEmployee(Employee employee) {
        employees.add(employee);
    }

    // =========================
    // REMOVE EMPLOYEE
    // =========================

    public void removeCurrentEmployee() {
        // Don't go below 1
        if (employees.size() <= 1) {
            return;
        }

        employees.remove(current_employee_index);
        // Move to the employee before if overflow because of removal.
        if (current_employee_index >= employees.size()) {
            current_employee_index = employees.size() - 1;
        }
    }

    // =========================
    // GET EMPLOYEES
    // =========================

    public ArrayList<Employee> getEmployees() {
        return employees;
    }

    // =========================
    // GET CURRENT EMPLOYEE
    // =========================

    public Employee getCurrentEmployee() {
        return employees.get(current_employee_index);
    }

    // =========================
    // GET CURRENT INDEX
    // =========================

    public int getCurrentEmployeeIndex() {
        return current_employee_index;
    }

    // =========================
    // SET CURRENT EMPLOYEE
    // =========================

    public void setCurrentEmployeeIndex(int index) {
        if (index >= 0 && index < employees.size()) {
            current_employee_index = index;
        }
    }

    // =========================
    // GET EMPLOYEE COUNT
    // =========================

    public int getEmployeeCount() {
        return employees.size();
    }



}