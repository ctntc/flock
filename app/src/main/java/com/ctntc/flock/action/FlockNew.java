package com.ctntc.flock.action;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.ctntc.flock.SessionConfig;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "new", description = "Create a new migration.")
public class FlockNew implements Runnable {
    private static final String NEW_TEMPLATE = """
            --- YOUR UP MIGRATION HERE ---

            --- YOUR DOWN MIGRATION HERE ---
            """;

    @Parameters(index = "0", description = "The name of the new migration.")
    private String migrationName;

    @Override
    public void run() {
        System.out.println("Creating new migration: " + migrationName);
        writeMigrationFile();
    }

    private String generateTimestamp() {
        var now = LocalDateTime.now();
        return now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private void writeMigrationFile() {
        var timestamp = generateTimestamp();
        var fileName = timestamp + "_" + migrationName.replaceAll("\\s+", "_") + ".sql";
        var migrationsDir = SessionConfig.getMigrationsDir();
        var filePath = migrationsDir + "/" + fileName;

        try {
            var file = new File(filePath);
            file.getParentFile().mkdirs();
            try (var writer = new FileWriter(file)) {
                writer.write(NEW_TEMPLATE);
            }
            System.out.println("Migration file created at: " + filePath);
        } catch (Exception e) {
            System.err.println("Failed to create migration file: " + e.getMessage());
        }
    }
}
