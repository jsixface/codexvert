package io.github.jsixface.codexvert.api

import io.github.jsixface.codexvert.db.IVideoFilesRepo
import io.github.jsixface.codexvert.ffprobe.IParser
import io.github.jsixface.codexvert.logger
import io.github.jsixface.codexvert.utils.toVideoFile
import io.github.jsixface.common.VideoFile
import java.nio.file.Path

typealias VideoList = List<VideoFile>

class VideoApi(private val parser: IParser, private val repo: IVideoFilesRepo) {

    private val logger = logger()

    suspend fun refreshDirs(): Boolean {
        logger.info("Refreshing videos...")
        val data = SavedData.load()
        return parser.parseAll(data.settings.libraryLocations, data.settings.videoExtensions)
    }

    suspend fun getVideos(): VideoList = repo.getAll().map { it.toVideoFile() }

    suspend fun getVideo(path: String): VideoFile? {
        return repo.getFile(Path.of(path))?.toVideoFile()
    }
}