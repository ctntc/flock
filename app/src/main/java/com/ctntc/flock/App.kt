package com.ctntc.flock

import com.ctntc.flock.action.FlockNew
import com.ctntc.flock.action.FlockRollback
import com.ctntc.flock.action.FlockUp
import com.google.inject.AbstractModule
import com.google.inject.Guice
import io.github.cdimascio.dotenv.Dotenv
import picocli.CommandLine
import java.net.URL
import java.sql.Connection
import java.sql.DriverManager
import java.util.logging.Logger

@CommandLine.Command(
    name = "flock",
    mixinStandardHelpOptions = true,
    version = ["flock 0.1.0"],
    description = ["A database migration tool for Java applications."],
)
class App : Runnable {
    override fun run() {
        println(
            "Flock: A database migration tool for Java applications inspired by Entity Framework."
        )
        connect(null)
    }

    companion object {
        private val LOGGER: Logger = Logger.getLogger("flock")

        @CommandLine.Option(
            names = ["-d", "--database-url"],
            description = ["The database connection URL."],
        )
        private val databaseUrl: URL? = null
        /**
         * `connection` is `public` and `static` because `flock` is single threaded, and only one
         * connection is needed at any time for any action.
         */
        @JvmField var connection: Connection? = null

        @JvmStatic
        fun main(args: Array<String>) {
            connect(databaseUrl)

            val app: App =
                Guice.createInjector(
                        object : AbstractModule() {
                            override fun configure() {
                                bind<SessionConfig?>(SessionConfig::class.java).asEagerSingleton()
                            }
                        }
                    )
                    .getInstance<App>(App::class.java)

            val commandLine =
                CommandLine(app)
                    .addSubcommand("new", FlockNew())
                    .addSubcommand("up", FlockUp())
                    .addSubcommand("rollback", FlockRollback())

            System.exit(commandLine.execute(*args))
        }

        private fun connect(databaseUrl: URL?) {
            if (databaseUrl == null || databaseUrl.toString().isBlank()) {
                val dotenv =
                    Dotenv.configure().directory("./").ignoreIfMalformed().ignoreIfMissing().load()

                SessionConfig.setDatabaseUrl(dotenv.get("DATABASE_URL"))
                SessionConfig.setMigrationsDir(dotenv.get("FLOCK_MIGRATIONS_DIR"))
            }

            if (SessionConfig.getDatabaseUrl().isBlank()) {
                LOGGER.severe(
                    "Database URL is not provided. Please set the DATABASE_URL environment variable."
                )
                System.exit(1)
            }

            LOGGER.info("Connecting to " + SessionConfig.getDatabaseUrl() + "...")

            try {
                connection = DriverManager.getConnection(SessionConfig.getDatabaseUrl())
                LOGGER.info("Connected to the database successfully.")
            } catch (e: Exception) {
                LOGGER.severe("Database connection failed: " + e.message)
                System.exit(1)
            }
        }
    }
}
