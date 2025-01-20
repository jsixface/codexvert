package io.github.jsixface.codexvert.api

import io.github.jsixface.codexvert.db.IVideoFilesRepo
import io.github.jsixface.codexvert.ffprobe.IParser
import io.github.jsixface.codexvert.logger
import io.github.jsixface.codexvert.utils.toVideoFile
import io.github.jsixface.common.CodecsCollection
import io.github.jsixface.common.VideoFile
import java.nio.file.Path

typealias VideoList = Map<String, String>

class VideoApi(private val parser: IParser, private val repo: IVideoFilesRepo) {

    private val logger = logger()
    private var cache = emptyList<VideoFile>()

    private suspend fun updateCache() {
        logger.info("${this::class.simpleName} updateCache()")
        cache = repo.getAll().map { it.toVideoFile() }
    }

    suspend fun refreshDirs(): Boolean {
        logger.info("Refreshing videos...")
        val data = SavedData.load()
        val refreshed = parser.parseAll(data.settings.libraryLocations, data.settings.videoExtensions)
        if (refreshed) updateCache()
        return refreshed
    }

    fun getVideos(audioFilter: String?, videoFilter: String?): VideoList {
        return cache.filter { audioFilter?.let { af -> it.audios.any { a -> a.codec == af } } ?: true }
            .filter { videoFilter?.let { vf -> it.videos.any { v -> v.codec == vf } } ?: true }
            .associate { it.path to it.fileName }
    }

    suspend fun getVideo(path: String): VideoFile? {
        return repo.getFile(Path.of(path))?.toVideoFile()
    }

    suspend fun getCodecsPresent(): CodecsCollection {
        if (cache.isEmpty()) updateCache()

        val allVids = cache
        val codecV = allVids.flatMap { it.videos }.map { it.codec }.distinct().sorted()
        val codecA = allVids.flatMap { it.audios }.map { it.codec }.distinct().sorted()
        val codecS = allVids.flatMap { it.subtitles }.map { it.codec }.distinct().sorted()
        return CodecsCollection(video = codecV, audio = codecA, subtitle = codecS)
    }
}