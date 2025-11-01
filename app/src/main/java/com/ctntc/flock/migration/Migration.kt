package com.ctntc.flock.migration

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.zip.CRC32
import java.util.zip.Checksum

class Migration {
    var type: MigrationType? = null
    var applied: Boolean? = null
    var filename: String? = null
    var filepath: String? = null
    private val file: File? = null
    var version: String? = null

    private var checksum: Checksum? = null

    fun calculateChecksum() {
        try {
            val contents = Files.readString(file!!.toPath())
            val crc32 = CRC32()
            crc32.update(contents.toByteArray())
            checksum = crc32
        } catch (e: IOException) {
            System.err.println("Could not calculate checksum for migration: " + e.message)
            System.exit(1)
        }
    }

    fun getChecksum(): Checksum {
        if (checksum == null) {
            calculateChecksum()
            checkNotNull(checksum)
        }

        return checksum!!
    }
}
