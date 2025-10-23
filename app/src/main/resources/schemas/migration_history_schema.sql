--- This file is not used in the code, but it is kept for reference. ---
CREATE TABLE IF NOT EXISTS "__flock_migrations_history" (
    "migration_id" TEXT NOT NULL CONSTRAINT "PK_migrations_history" PRIMARY KEY,
    "flock_version" TEXT NOT NULL
);