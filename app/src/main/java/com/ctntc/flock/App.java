package com.ctntc.flock;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Logger;

import org.jspecify.annotations.Nullable;

import com.ctntc.flock.action.FlockNew;

import io.github.cdimascio.dotenv.Dotenv;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "flock", mixinStandardHelpOptions = true, version = "flock 0.1.0", description = "A database migration tool for Java applications.")
public class App implements Runnable {
    private static final Logger LOGGER = Logger.getLogger("flock");

    @Nullable
    private static Connection connection = null;

    public static void main(String[] args) {
        var commandLine = new CommandLine(new App())
                .addSubcommand("new", new FlockNew());

        System.exit(commandLine.execute(args));
    }

    private static void connect(@Nullable String databaseUrl) {
        if (databaseUrl == null || databaseUrl.isBlank()) {
            var dotenv = Dotenv.configure()
                    .directory("./")
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();

            SessionConfig.setDatabaseUrl(dotenv.get("DATABASE_URL"));
            SessionConfig.setMigrationsDir(dotenv.get("FLOCK_MIGRATIONS_DIR"));
        }

        if (SessionConfig.getDatabaseUrl() == null || SessionConfig.getDatabaseUrl().isBlank()) {
            LOGGER.severe("Database URL is not provided. Please set the DATABASE_URL environment variable.");
            System.exit(1);
        }

        LOGGER.info("Connecting to " + SessionConfig.getDatabaseUrl() + "...");

        try {
            connection = DriverManager.getConnection(SessionConfig.getDatabaseUrl());
            LOGGER.info("Connected to the database successfully.");
        } catch (Exception e) {
            LOGGER.severe("Database connection failed: " + e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void run() {
        System.out.println("Flock: A database migration tool for Java applications inspired by Entity Framework.");
        connect(null);
    }
}
