package com.ctntc.flock.schema;

import java.sql.Connection;

import org.jspecify.annotations.NonNull;

public final class MigrationsHistorySchema {
    private static final String SCHEMA = """
            CREATE TABLE IF NOT EXISTS "__flock_migrations_history" (
                "migration_id" TEXT NOT NULL CONSTRAINT "PK_migrations_history" PRIMARY KEY,
                "flock_version" TEXT NOT NULL
            );
                        """;

    public static void initialize(@NonNull Connection connection) {
        try (var statement = connection.createStatement()) {
            statement.execute(SCHEMA);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
