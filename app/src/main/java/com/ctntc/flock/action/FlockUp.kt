package com.ctntc.flock.action

import com.ctntc.flock.App
import com.ctntc.flock.SessionConfig.migrationsDir
import com.ctntc.flock.schema.MigrationsHistorySchema
import com.ctntc.flock.schema.MigrationsLockSchema
import picocli.CommandLine
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

@CommandLine.Command(name = "up", description = ["Run up migrations"])
class FlockUp : Runnable {
    override fun run() {
        checkNotNull(App.connection)

        println("Running up migrations...")

        MigrationsHistorySchema.initialize(App.connection!!)
        MigrationsLockSchema.initialize(App.connection)

        val latestUnappliedMigration = this.latestUnappliedMigration
        if (latestUnappliedMigration.isPresent) {
            println("Applying migration: " + latestUnappliedMigration.get())

            try {
                App.connection!!.createStatement().use { statement ->
                    val migrationFilePath =
                        Path.of(migrationsDir, latestUnappliedMigration.get() + ".sql")
                    val migrationSql = Files.readString(migrationFilePath)

                    statement.execute(migrationSql)

                    val insertHistorySql =
                        String.format(
                            "INSERT INTO \"__flock_migrations_history\" (migration_id, flock_version) VALUES ('%s'," +
                                " '0.1.0');",
                            latestUnappliedMigration.get(),
                        )
                    statement.execute(insertHistorySql)
                }
            } catch (e: Exception) {
                System.err.println("Failed to apply migration: " + latestUnappliedMigration.get())
                System.exit(1)
            }

            println("Migration " + latestUnappliedMigration.get() + " applied successfully.")
        } else {
            println("No unapplied migrations found.")
        }
    }

    private val latestUnappliedMigration: Optional<String?>
        get() {
            checkNotNull(App.connection)

            val migrationsDir = Path.of(migrationsDir)
            if (!Files.exists(migrationsDir) || !Files.isDirectory(migrationsDir)) {
                System.err.println("Migrations directory does not exist: " + migrationsDir)
                System.exit(1)
            }

            try {
                val appliedMigrations = HashSet<String?>()

                App.connection!!.createStatement().use { statement ->
                    val resultSet =
                        statement.executeQuery(
                            "SELECT migration_id FROM \"__flock_migrations_history\";"
                        )
                    while (resultSet.next()) {
                        appliedMigrations.add(resultSet.getString("migration_id"))
                    }
                }
                Files.list(migrationsDir).use { stream ->
                    val migrationFiles =
                        stream
                            .filter { path: Path? ->
                                path!!
                                    .fileName
                                    .toString()
                                    .lowercase(Locale.getDefault())
                                    .endsWith(".sql")
                            }
                            .sorted()
                            .toList()
                    for (migrationFile in migrationFiles) {
                        val migrationId =
                            migrationFile!!.fileName.toString().replace(".sql", "")
                        if (!appliedMigrations.contains(migrationId)) {
                            return Optional.of<String?>(migrationId)
                        }
                    }
                }
            } catch (e: Exception) {
                System.err.println("Failed to get latest unapplied migration: " + e.message)
                System.exit(1)
            }
            return Optional.empty<String?>()
        }
}
