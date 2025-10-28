package com.ctntc.flock;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Logger;

import org.jspecify.annotations.Nullable;

import com.ctntc.flock.action.FlockNew;
import com.ctntc.flock.action.FlockRollback;
import com.ctntc.flock.action.FlockUp;

import io.github.cdimascio.dotenv.Dotenv;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "flock", mixinStandardHelpOptions = true, version = "flock 0.1.0", description = "A database migration tool for Java applications.")
public class App implements Runnable {
    private static final Logger LOGGER = Logger.getLogger("flock");

    /// `connection` is `public` and `static` because flock is single threaded, and
    /// only one connection is needed at any time for any action.
    @Nullable
    public static Connection connection = null;

    @Option(names = {"-d", "--database-url"}, description = "The database connection URL.")
    @Nullable
    private static final URL databaseUrl = null;

    public static void main(String[] args) {
        var commandLine = new CommandLine(new App())
                .addSubcommand("new", new FlockNew())
                .addSubcommand("up", new FlockUp())
                .addSubcommand("rollback", new FlockRollback());

        connect(databaseUrl);

        System.exit(commandLine.execute(args));
    }

    private static void connect(@Nullable URL databaseUrl) {
        if (databaseUrl == null || databaseUrl.toString().isBlank()) {
            var dotenv = Dotenv.configure()
                    .directory("./")
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();

            SessionConfig.setDatabaseUrl(dotenv.get("DATABASE_URL"));
            SessionConfig.setMigrationsDir(dotenv.get("FLOCK_MIGRATIONS_DIR"));
        }

        if (SessionConfig.getDatabaseUrl().isBlank()) {
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
