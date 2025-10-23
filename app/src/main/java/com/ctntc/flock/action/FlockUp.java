package com.ctntc.flock.action;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;

import com.ctntc.flock.App;
import com.ctntc.flock.SessionConfig;
import com.ctntc.flock.schema.MigrationsHistorySchema;
import com.ctntc.flock.schema.MigrationsLockSchema;

import picocli.CommandLine.Command;

@Command(name = "up", description = "Run up migrations")
public class FlockUp implements Runnable {
    @Override
    public void run() {
        System.out.println("Running up migrations...");

        MigrationsHistorySchema.initialize(App.connection);
        MigrationsLockSchema.initialize(App.connection);

        var latestUnappliedMigration = getLatestUnappliedMigration();
        if (latestUnappliedMigration.isPresent()) {

            System.out.println("Applying migration: " + latestUnappliedMigration.get());

            try (var statement = App.connection.createStatement()) {
                var migrationFilePath = Path.of(SessionConfig.getMigrationsDir(),
                        latestUnappliedMigration.get() + ".sql");
                var migrationSql = Files.readString(migrationFilePath);

                // Assuming the migration SQL file contains both up and down migrations,
                // separated by a special comment line "--- YOUR DOWN MIGRATION HERE ---"
                var upMigrationSql = migrationSql.split("--- YOUR DOWN MIGRATION HERE ---")[0];

                statement.execute(upMigrationSql);

                // Record the applied migration in the migrations history table
                var insertHistorySql = String.format(
                        "INSERT INTO \"__flock_migrations_history\" (migration_id, flock_version) VALUES ('%s', '0.1.0');",
                        latestUnappliedMigration.get());
                statement.execute(insertHistorySql);

            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("Migration " + latestUnappliedMigration.get() + " applied successfully.");

        } else {
            System.out.println("No unapplied migrations found.");
        }
    }

    private Optional<String> getLatestUnappliedMigration() {
        try {
            var migrationsDir = Path.of(SessionConfig.getMigrationsDir());
            var appliedMigrations = new HashSet<String>();

            try (var statement = App.connection.createStatement()) {
                var resultSet = statement.executeQuery("SELECT migration_id FROM \"__flock_migrations_history\";");
                while (resultSet.next()) {
                    appliedMigrations.add(resultSet.getString("migration_id"));
                }
            }

            var migrationFiles = Files.list(migrationsDir)
                    .filter(path -> path.getFileName().toString().endsWith(".sql"))
                    .sorted()
                    .toList();

            for (var migrationFile : migrationFiles) {
                var migrationId = migrationFile.getFileName().toString().replace(".sql", "");
                if (!appliedMigrations.contains(migrationId)) {
                    return Optional.of(migrationId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();

    }
}
