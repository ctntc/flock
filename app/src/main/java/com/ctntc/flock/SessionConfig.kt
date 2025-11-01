package com.ctntc.flock

object SessionConfig {
    @JvmStatic
    var migrationsDir: String? = null
        get() = if (field != null) field else "./migrations"
        set(dir) {
            if (dir != null && !dir.isBlank()) field = dir
        }

    private var databaseUrl: String

    init {
        databaseUrl = ""
    }

    fun getDatabaseUrl(): String {
        return databaseUrl
    }

    fun setDatabaseUrl(url: String) {
        assert(!url.isBlank())
        databaseUrl = url
    }
}
