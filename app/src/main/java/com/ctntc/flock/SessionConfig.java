package com.ctntc.flock;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class SessionConfig {
    @Nullable
    private static String migrationsDir = null;
    @NonNull
    private static String databaseUrl = null;

    public static void setMigrationsDir(@Nullable String dir) {
        if (dir != null && dir.isBlank() == false)
            migrationsDir = dir;
    }

    public static String getMigrationsDir() {
        return migrationsDir != null ? migrationsDir : "./migrations";
    }

    public static String getDatabaseUrl() {
        return databaseUrl;
    }

    public static void setDatabaseUrl(@NonNull String url) {
        assert url.isBlank() == false;
        databaseUrl = url;
    }
}
