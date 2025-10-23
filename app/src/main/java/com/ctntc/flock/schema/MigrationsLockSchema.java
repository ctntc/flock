package com.ctntc.flock.schema;

public final class MigrationsLockSchema {
    private static final String SCHEMA = """
            CREATE TABLE IF NOT EXISTS "__flock_migration_lock" (
                id INTEGER NOT NULL CONSTRAINT "PK_migration_lock" PRIMARY KEY,
                locked_at TEXT NOT NULL
            );
                                    """;

    public static void initialize(java.sql.Connection connection) {
        try (var statement = connection.createStatement()) {
            statement.execute(SCHEMA);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}