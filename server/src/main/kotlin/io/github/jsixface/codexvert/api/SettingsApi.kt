package io.github.jsixface.codexvert.api

import io.github.jsixface.codexvert.logger
import io.github.jsixface.codexvert.plugins.Watchers
import io.github.jsixface.common.Settings


class SettingsApi(private val watchers: Watchers, private val preferences: IPreferences) {
    private val logger = logger()

    fun getSettings(): Settings = preferences.getSettings()

    fun saveSettings(settings: Settings) {
        val oldSettings = preferences.getSettings()
        logger.info("Replacing old settings $oldSettings with new $settings")
        settings.autoConversion.watchDuration?.let {
            if (settings.autoConversion != oldSettings.autoConversion) {
                watchers.startWatching(settings.autoConversion)
            }
        } ?: watchers.stopWatching()
        preferences.saveSettings(settings)
    }
}