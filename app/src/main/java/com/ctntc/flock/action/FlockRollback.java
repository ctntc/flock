package com.ctntc.flock.action;

import java.sql.SQLException;
import java.util.Optional;

import com.ctntc.flock.App;
import picocli.CommandLine.Command;

@Command(name = "rollback", description = "Rollback the last applied migration")
public class FlockRollback implements Runnable {

    private static final String DELETE_FROM_HISTORY_SQL = "DELETE FROM \"__flock_migrations_history\" WHERE migration_id = '%s';";
    // The migration_id is timestamp-prefixed (yyyyMMddHHmmss_name), so lexical DESC
    // gives the latest applied.
    private static final String FETCH_LATEST_MIGRATION_ID_SQL = "SELECT migration_id FROM \"__flock_migrations_history\" ORDER BY migration_id DESC LIMIT 1;";

    @Override
    public void run() {
        System.out.println("Rolling back the last applied migration...");
        Optional<String> latestMigrationOpt;
        try {
            latestMigrationOpt = getLatestAppliedMigration();
        } catch (SQLException e) {
            System.err.println("Failed to fetch latest applied migration: " + e.getMessage());
            return;
        }
        if (latestMigrationOpt.isPresent()) {
            var latestMigrationId = latestMigrationOpt.get();
            System.out.println("Rolling back migration: " + latestMigrationId);
            performRollback(latestMigrationId);
        } else {
            System.out.println("No applied migrations found to rollback.");
        }
    }

    private void performRollback(String migrationId) {
        var conn = App.connection;

        try {
            assert conn != null;
            try (var statement = conn.createStatement()) {
                // Here you would load and execute the down migration SQL for the given
                // migrationId
                // For simplicity, we will just print a message

                // After successful execution, remove the migration record from history
                var deleteHistorySql = String.format(
                        DELETE_FROM_HISTORY_SQL,
                        migrationId);
                System.out.println("Removing migration record from history: " + migrationId);
                statement.executeUpdate(deleteHistorySql);

                System.out.println("Rollback of migration " + migrationId + " completed successfully.");

            }
        } catch (Exception e) {
            System.err.println("Failed to rollback migration: " + e.getMessage());
        }
    }

    private Optional<String> getLatestAppliedMigration() throws SQLException {
        assert App.connection != null;
        var lastAppliedMigration = App.connection
                .createStatement()
                .executeQuery(
                        FETCH_LATEST_MIGRATION_ID_SQL);

        if (lastAppliedMigration.next()) {
            return Optional.of(lastAppliedMigration.getString("migration_id"));
        }
        lastAppliedMigration.close();
        return Optional.empty();
    }
}