package com.ctntc.flock.action;

import com.ctntc.flock.SessionConfig;
import com.google.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Command(name = "new", description = "Create a new migration.")
public class FlockNew implements Runnable {

    private static final String NEW_TEMPLATE_UP = """
        --- YOUR UP MIGRATION HERE ---
        """;
    private static final String NEW_TEMPLATE_DOWN = """
        --- YOUR DOWN MIGRATION HERE ---
        """;

    @Inject
    private static SessionConfig sessionConfig;

    @Parameters(index = "0", description = "The name of the new migration.")
    private String migrationName;

    @Option(
            names = {"-d", "--down"},
            description = "Create a matching down migration script.")
    private boolean down;

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
        var sanitizedName = migrationName.replaceAll("\\s+", "_");
        var migrationsDir = SessionConfig.getMigrationsDir();

        // Create the migration directory if it doesn't exist
        var migrationsDirFile = new File(migrationsDir);
        if (!migrationsDirFile.mkdirs()) {
            System.err.println("Failed to create migrations directory: " + migrationsDir);
            return;
        }

        try {
            createMigrationFile(timestamp, sanitizedName, migrationsDir, "up", NEW_TEMPLATE_UP);

            if (down) {
                createMigrationFile(timestamp, sanitizedName, migrationsDir, "down", NEW_TEMPLATE_DOWN);
            }

            System.out.println("Migration file(s) created in: " + migrationsDir);
        } catch (Exception e) {
            System.err.println("Failed to create migration file: " + e.getMessage());
        }
    }

    private void createMigrationFile(
            String timestamp, String sanitizedName, String migrationsDir, String type, String template)
            throws IOException {
        var fileName = String.format("%s__%s.%s.sql", timestamp, sanitizedName, type);
        var filePath = migrationsDir + "/" + fileName;

        try (var writer = new FileWriter(filePath)) {
            writer.write(template);
        }
    }
}
