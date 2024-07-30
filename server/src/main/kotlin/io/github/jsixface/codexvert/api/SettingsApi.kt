package io.github.jsixface.codexvert.api

import io.github.jsixface.codexvert.logger
import io.github.jsixface.codexvert.plugins.Watchers
import io.github.jsixface.common.Settings


class SettingsApi(private val watchers: Watchers) {
    private val logger = logger()

    fun getSettings(): Settings = SavedData.load().settings

    fun saveSettings(settings: Settings) {
        val savedData = SavedData.load()
        val oldSettings = savedData.settings
        logger.info("Replacing old settings $oldSettings with new $settings")
        settings.watchDuration?.let {
            if (it != oldSettings.watchDuration) {
                watchers.startWatching(it)
            }
        } ?: watchers.stopWatching()
        savedData.copy(settings = settings).save()
    }
}