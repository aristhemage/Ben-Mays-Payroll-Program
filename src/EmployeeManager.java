import java.util.ArrayList;

public class EmployeeManager {

    // Make a list of employees of type Employee.
    private final ArrayList<Employee> employees = new ArrayList<>();

    private int current_employee_index = 0;

    // =========================
    // ADD/REMOVE/SET EMPLOYEE
    // =========================

    public void addEmployee(Employee employee) {
        // New employees are appended so they become the last choice in the selector.
        employees.add(employee);
    }


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


    public void setCurrentEmployeeIndex(int index) {
        // Ignore invalid selections rather than letting the table access a missing employee.
        if (index >= 0 && index < employees.size()) {
            current_employee_index = index;
        }
    }

    // =========================
    // BASIC GETTERS
    // =========================

    public ArrayList<Employee> getEmployees() {
        return employees;
    }

    public Employee getCurrentEmployee() {
        return employees.get(current_employee_index);
    }

    public int getCurrentEmployeeIndex() {
        return current_employee_index;
    }

    public int getEmployeeCount() {
        return employees.size();
    }

}
