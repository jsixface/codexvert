package io.github.jsixface.codexvert.db

import io.github.jsixface.codexvert.ffprobe.ProbeInfo
import io.github.jsixface.codexvert.ffprobe.ProbeStream
import io.github.jsixface.codexvert.logger
import io.github.jsixface.codexvert.utils.toVideoFile
import io.github.jsixface.codexvert.utils.updateInfo
import io.github.jsixface.common.VideoFile
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.fileSize
import kotlin.io.path.getLastModifiedTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction


interface IVideoFilesRepo {
    suspend fun getAllEntities(): List<VideoFileEntity>
    suspend fun getAllVideoFiles(): List<VideoFile>
    suspend fun get(id: Int): VideoFileEntity?
    suspend fun getEntity(path: Path): VideoFileEntity?
    suspend fun getFile(path: Path): VideoFile?
    suspend fun delete(id: Int)
    suspend fun update(videoInfo: ProbeInfo, entity: VideoFileEntity, file: Path): Boolean
    suspend fun create(videoInfo: ProbeInfo, file: Path): VideoFileEntity
}

class VideoFilesRepo(private val db: Database) : IVideoFilesRepo {
    private val logger = logger()

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun getAllEntities() = dbQuery { VideoFileEntity.all().toList() }

    override suspend fun getAllVideoFiles(): List<VideoFile> = dbQuery {
        VideoFileEntity.all().map { it.toVideoFile() }
    }

    override suspend fun get(id: Int) = dbQuery { VideoFileEntity.findById(id) }

    override suspend fun getEntity(path: Path): VideoFileEntity? = dbQuery {
        VideoFileEntity.find { VideoFilesTable.path eq path.toAbsolutePath().toString() }.firstOrNull()
    }

    override suspend fun getFile(path: Path): VideoFile? = dbQuery {
        getEntity(path)?.toVideoFile()
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun create(videoInfo: ProbeInfo, file: Path): VideoFileEntity {
        return dbQuery {
            val v = VideoFileEntity.new {
                path = file.absolutePathString()
                name = file.fileName.toString()
                sizeMb = file.fileSize().toInt() / 1024 / 1024
                modified = file.getLastModifiedTime().toMillis()
                added = Clock.System.now().toEpochMilliseconds()
            }
            logger.debug("Added video file: ${v.path}")
            videoInfo.streams.forEach { createStream(it, v) }
            v
        }
    }

    private fun createStream(stream: ProbeStream, v: VideoFileEntity) {
        when (stream.codecType) {
            "audio" -> {
                AudioEntity.new {
                    videoFile = v
                    updateInfo(stream)
                }
            }

            "video" -> {
                VideoEntity.new {
                    videoFile = v
                    updateInfo(stream)
                }
            }

            "subtitle" -> {
                SubtitleEntity.new {
                    videoFile = v
                    index = stream.index
                    codec = stream.codecName
                    language = stream.tags["language"] ?: ""
                }
            }
        }
    }


    override suspend fun delete(id: Int) {
        dbQuery {
            val v = get(id)
            logger.debug("deleting video file: ${v?.path}")
            v?.videoStream?.forEach { it.delete() }
            v?.audioStreams?.forEach { it.delete() }
            v?.subtitles?.forEach { it.delete() }
            v?.delete()
        }
    }

    override suspend fun update(videoInfo: ProbeInfo, entity: VideoFileEntity, file: Path) = dbQuery {
        try {
            entity.videoStream.forEach { it.delete() }
            entity.audioStreams.forEach { it.delete() }
            entity.subtitles.forEach { it.delete() }
            videoInfo.streams.forEach { createStream(it, entity) }
            with(entity) {
                sizeMb = file.fileSize().toInt() / 1024 / 1024
                modified = file.getLastModifiedTime().toMillis()
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}