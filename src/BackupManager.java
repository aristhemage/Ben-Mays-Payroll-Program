import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BackupManager {
    // Creates a backup file
    public static Path createBackup(List<Employee> employees) throws IOException {
        // Determine the path, timestamp, and file name
        Path backupFolder = Paths.get(
                System.getProperty("user.home"),
                "Documents",
                "Payroll Manager",
                "Backups"
        );

        // Make the Backups folder if it does not exist yet.
        Files.createDirectories(backupFolder);


        String timeStamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
        );

        String fileName = "Payroll_Backup_" + timeStamp + ".json";

        Path backupFile = backupFolder.resolve(fileName);

        // Set it up to be Gson (Better Json)
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        String backupText = gson.toJson(employees);

        Files.writeString(backupFile,backupText,StandardCharsets.UTF_8);
        return backupFile;
    }

}
