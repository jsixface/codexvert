package io.github.jsixface.codexvert.api

import io.github.jsixface.codexvert.db.IVideoFilesRepo
import io.github.jsixface.codexvert.ffprobe.IParser
import io.github.jsixface.codexvert.logger
import io.github.jsixface.codexvert.utils.toVideoFile
import io.github.jsixface.common.CodecsCollection
import io.github.jsixface.common.VideoFile
import io.github.jsixface.common.VideoList
import java.nio.file.Path


class VideoApi(private val parser: IParser, private val repo: IVideoFilesRepo, private val preferences: IPreferences) {

    private val logger = logger()
    private var cache = emptyList<VideoFile>()

    suspend fun updateCache() {
        logger.info("${this::class.simpleName} updateCache()")
        cache = repo.getAllVideoFiles()
    }

    suspend fun refreshDirs(): Boolean {
        logger.info("Refreshing videos...")
        val settings = preferences.getSettings()
        val refreshed = parser.parseAll(settings.libraryLocations, settings.videoExtensions)
        if (refreshed) updateCache()
        return refreshed
    }

    fun getVideos(audioFilter: String?, videoFilter: String?): VideoList {
        val filtered = cache.filter { audioFilter?.let { af -> it.audios.any { a -> a.codec == af } } ?: true }
            .filter { videoFilter?.let { vf -> it.videos.any { v -> v.codec == vf } } ?: true }
        val codecs = getCodecsPresent(filtered)
        return VideoList(pathAndNames = filtered.associate { it.path to it.fileName }, codecs)
    }

    suspend fun getVideo(path: String): VideoFile? {
        return repo.getFile(Path.of(path))?.toVideoFile()
    }

    private fun getCodecsPresent(videoFiles: List<VideoFile>): CodecsCollection {
        val codecV = videoFiles.flatMap { it.videos }.map { it.codec }.distinct().sorted()
        val codecA = videoFiles.flatMap { it.audios }.map { it.codec }.distinct().sorted()
        val codecS = videoFiles.flatMap { it.subtitles }.map { it.codec }.distinct().sorted()
        return CodecsCollection(video = codecV, audio = codecA, subtitle = codecS)
    }

    suspend fun getCodecsPresent(): CodecsCollection {
        if (cache.isEmpty()) updateCache()
        return getCodecsPresent(cache)
    }
}