import java.io.*;

public class FileData {

    // =========================
    // SAVE FILE LOCATION
    // =========================

    private static final String APP_NAME = "Payroll Manager";

    /**
     * Gets the location where the program should store its data.
     *
     * Windows:
     * %APPDATA%/Payroll Manager/
     *
     * macOS:
     * ~/Library/Application Support/Payroll Manager/
     *
     * Linux:
     * ~/.local/share/Payroll Manager/
     */
    private static File getDataDirectory() {

        String os = System.getProperty("os.name").toLowerCase();

        File dataDirectory;

        if (os.contains("win")) {

            // Windows
            dataDirectory = new File(
                    System.getenv("APPDATA"),
                    APP_NAME
            );

        } else if (os.contains("mac")) {

            // macOS
            dataDirectory = new File(
                    System.getProperty("user.home"),
                    "Library/Application Support/" + APP_NAME
            );

        } else {

            // Linux / other Unix systems
            dataDirectory = new File(
                    System.getProperty("user.home"),
                    ".local/share/" + APP_NAME
            );
        }

        // Create the directory if it doesn't exist
        if (!dataDirectory.exists()) {
            dataDirectory.mkdirs();
        }

        return dataDirectory;
    }

    /**
     * Gets the actual save file.
     */
    private static File getSaveFile() {
        return new File(
                getDataDirectory(),
                "payroll_sav.dat"
        );
    }


    // =========================
    // SAVE
    // =========================

    public static void save(EmployeeManager employeeManager) {

        File saveFile = getSaveFile();

        try {

            ObjectOutputStream output =
                    new ObjectOutputStream(
                            new FileOutputStream(saveFile)
                    );

            output.writeObject(employeeManager);

            output.close();

            System.out.println(
                    "Payroll Data Saved To: "
                            + saveFile.getAbsolutePath()
            );

        } catch (IOException e) {

            e.printStackTrace();
        }
    }


    // =========================
    // LOAD
    // =========================

    public static EmployeeManager load() {

        File saveFile = getSaveFile();

        // No save file exists yet
        if (!saveFile.exists()) {
            return null;
        }

        try {

            ObjectInputStream input =
                    new ObjectInputStream(
                            new FileInputStream(saveFile)
                    );

            EmployeeManager employeeManager =
                    (EmployeeManager) input.readObject();

            input.close();

            System.out.println(
                    "Payroll Data Loaded From: "
                            + saveFile.getAbsolutePath()
            );

            return employeeManager;

        } catch (IOException | ClassNotFoundException e) {

            e.printStackTrace();

            return null;
        }
    }
}