import java.io.*;

public class FileData {
    private static final String SAVE_FILE = "payroll_sav.dat";

    // =========================
    // SAVE
    // =========================
    public static void save(EmployeeManager employeeManager) {
        try {
            ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(SAVE_FILE));

            output.writeObject(employeeManager);

            output.close();

            System.out.println("Payroll Data Saved!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =========================
    // LOAD
    // =========================

    public static EmployeeManager load(){
        File file = new File(SAVE_FILE);

        // No save file exists yet
        if(!file.exists()){
            return null;
        }

        try {
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(SAVE_FILE));

            EmployeeManager employeeManager = (EmployeeManager) input.readObject();

            input.close();
            System.out.println("Payroll Data Loaded!");
            return employeeManager;

        }catch (IOException | ClassNotFoundException e){
            e.printStackTrace();
            return null;
        }

    }

}
