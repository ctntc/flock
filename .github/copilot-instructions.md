# Flock - Database Migration Tool for Java

## Project Overview
Flock is an Entity Framework-inspired database migration tool for Java applications, currently in early development. It's a CLI tool built with Picocli that generates timestamped SQL migration files.

## Architecture

### Module Structure
- **Multi-module Gradle project** with `app/` as the main module (see `settings.gradle`)
- **Entry point**: `App.java` - Picocli command runner with subcommands
- **Action pattern**: Commands live in `com.ctntc.flock.action.*` (e.g., `FlockNew.java`)
- **SessionConfig**: Global state holder for runtime configuration (database URL, migrations directory)

### Key Design Patterns
- **Picocli subcommands**: Add new commands by creating `@Command` classes in `action/` package and registering in `App.main()`
  - Planned subcommands: `up` (apply migrations), `down` (rollback), `status` (show migration state)
- **Dotenv configuration**: `.env` file at project root (NOT in `app/`) loads `DATABASE_URL` and `FLOCK_MIGRATIONS_DIR`
- **Timestamp-based migrations**: Format `yyyyMMddHHmmss_name.sql` (see `FlockNew.generateTimestamp()`)
- **Database support**: SQLite and PostgreSQL only (use JDBC URLs like `jdbc:sqlite:` or `jdbc:postgresql:`)

## Development Workflows

### Building and Running
```bash
./gradlew build          # Build project
./gradlew run            # Run app (working dir is project root, not app/)
./gradlew test           # Run tests
```

### Critical Gradle Configuration
- **Java 25 toolchain** required (`java.toolchain.languageVersion`)
- **Working directory**: `tasks.named('run') { workingDir = rootProject.projectDir }` ensures `.env` is found
- **Native access**: `--enable-native-access=ALL-UNNAMED` required for SQLite JDBC (set in `applicationDefaultJvmArgs` and test `jvmArgs`)
- **Version catalog**: All dependencies in `gradle/libs.versions.toml` (use `libs.dependency-name` in `build.gradle`)

### Adding New Commands
1. Create class in `com.ctntc.flock.action.*` extending `Runnable` with `@Command` annotation
2. Register in `App.main()`: `.addSubcommand("name", new YourCommand())`
3. Access configuration via `SessionConfig.getDatabaseUrl()` or `SessionConfig.getMigrationsDir()`

### Environment Configuration
- **`.env` location**: Project root (not `app/`), loaded via dotenv-java
- **Required variables**: `DATABASE_URL` (JDBC URL), `FLOCK_MIGRATIONS_DIR` (optional, defaults to `./migrations`)
- **Example**: `DATABASE_URL=jdbc:sqlite::memory:` for in-memory SQLite

## Dependencies & Tools
- **Picocli**: CLI framework with annotation processors (requires `annotationProcessor libs.picocli.codegen`)
- **SQLite JDBC**: Default database driver (Xerial) - primary database for development and testing
- **PostgreSQL JDBC**: To be added for production database support
- **JSpecify**: Nullability annotations (`@Nullable`, `@NonNull`)
- **Dotenv**: Configuration from `.env` files (io.github.cdimascio)
- **Guava**: Utility library (though currently minimal usage)

## Future Plans
- **Gradle/Maven plugins**: Separate modules from CLI tool, far into the future
- **Core library extraction**: CLI will eventually depend on a shared `flock-core` library for plugin reuse

## Conventions

### Code Style
- **Null safety**: Use JSpecify `@Nullable` and `@NonNull` annotations
- **Logging**: `java.util.logging.Logger` with `.getLogger("flock")` or class-specific names
- **Error handling**: Log errors with `LOGGER.severe()` and `System.exit(1)` for fatal errors
- **String templates**: Use text blocks `"""..."""` for multi-line strings (see `FlockNew.NEW_TEMPLATE`)

### File Naming
- **Migration files**: `{timestamp}_{sanitized_name}.sql` where timestamp is `yyyyMMddHHmmss`
- **Package structure**: `com.ctntc.flock.*` with subpackages for `action.*`

## Testing Notes
- **Test strategy**: Use in-memory SQLite (`jdbc:sqlite::memory:`) for all tests
- **Test class**: `AppTest.java` currently empty (tests not yet implemented per roadmap)
- **Test execution**: Uses JUnit 5 Jupiter (`testImplementation libs.junit.jupiter`)
- **Test working dir**: Inherits same configuration as run task
- **Database testing**: No mocking - tests should use real JDBC connections to in-memory SQLite

## Common Pitfalls
- **`.env` not found**: Ensure Gradle `run` task has `workingDir = rootProject.projectDir` (not default `app/`)
- **SQLite warnings**: Must include `--enable-native-access=ALL-UNNAMED` in JVM args for both run and test tasks
- **SessionConfig state**: Static fields mean state persists across command invocations in same JVM
- **Migration directory**: Created automatically by `FlockNew` if it doesn't exist
