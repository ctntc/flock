--- This file is not used in the code, but it is kept for reference. ---
CREATE TABLE IF NOT EXISTS "__flock_migration_lock" (
    id INTEGER NOT NULL CONSTRAINT "PK_migration_lock" PRIMARY KEY,
    locked_at TEXT NOT NULL
);