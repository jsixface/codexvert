package io.github.jsixface.codexvert.api

import io.github.jsixface.codexvert.logger
import java.io.File

class BackupApi {

    private val logger = logger<BackupApi>()
    private val backupPattern = ".*[0-9]+\\.bkp$".toRegex()

    fun getBackups(): List<String> {
        val settings = SavedData.load().settings
        return settings.libraryLocations.flatMap { location ->
            File(location).walk().filter { it.name.matches(backupPattern) }.map { it.absolutePath }
        }
    }

    fun deleteBackup(path: String) {
        logger.info("Deleting backup: $path")
        File(path).delete()
    }

    fun deleteAllBackups() {
        val settings = SavedData.load().settings
        settings.libraryLocations.forEach { location ->
            logger.info("Deleting all backups in $location")
            File(location).walk().filter { it.name.matches(backupPattern) }.forEach { it.delete() }
        }
    }
}