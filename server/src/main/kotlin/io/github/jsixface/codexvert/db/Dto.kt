package io.github.jsixface.codexvert.db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object VideosTable : IntIdTable() {
    val index = integer("index")
    val codec = varchar("codec", 10)
    val codecTag = varchar("codec_tag", 25)
    val profile = varchar("profile", 50)
    val resolution = varchar("resolution", 50)
    val aspectRatio = varchar("aspect_ratio", 20)
    val frameRate = float("frame_rate")
    val bitRate = integer("bit_rate")
    val bitDepth = integer("bit_depth")
    val pixelFormat = varchar("pixel_format", 50)
    val videoFile = reference("video_file_id", VideoFilesTable, onDelete = ReferenceOption.CASCADE)
}


class VideoEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<VideoEntity>(VideosTable)

    var index by VideosTable.index
    var codec by VideosTable.codec
    var codecTag by VideosTable.codecTag
    var profile by VideosTable.profile
    var resolution by VideosTable.resolution
    var aspectRatio by VideosTable.aspectRatio
    var frameRate by VideosTable.frameRate
    var bitRate by VideosTable.bitRate
    var bitDepth by VideosTable.bitDepth
    var pixelFormat by VideosTable.pixelFormat
    var videoFile by VideoFileEntity referencedOn VideosTable.videoFile
}

object AudiosTable : IntIdTable() {
    val index = integer("index")
    val codec = varchar("codec", 50)
    val channels = integer("channels")
    val layout = varchar("layout", 50)
    val bitrate = integer("bit_rate")
    val sampleRate = varchar("sample_rate", 25)
    val language = varchar("language", 50)
    val videoFile = reference("video_file_id", VideoFilesTable, onDelete = ReferenceOption.CASCADE)
}

class AudioEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AudioEntity>(AudiosTable)

    var index by AudiosTable.index
    var codec by AudiosTable.codec
    var channels by AudiosTable.channels
    var layout by AudiosTable.layout
    var bitrate by AudiosTable.bitrate
    var sampleRate by AudiosTable.sampleRate
    var language by AudiosTable.language
    var videoFile by VideoFileEntity referencedOn AudiosTable.videoFile
}

object SubtitlesTable : IntIdTable() {
    val index = integer("index")
    val codec = varchar("codec", 50)
    val language = varchar("language", 50)
    val videoFile = reference("video_file_id", VideoFilesTable, onDelete = ReferenceOption.CASCADE)
}

class SubtitleEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<SubtitleEntity>(SubtitlesTable)

    var index by SubtitlesTable.index
    var codec by SubtitlesTable.codec
    var language by SubtitlesTable.language
    var videoFile by VideoFileEntity referencedOn SubtitlesTable.videoFile
}

object VideoFilesTable : IntIdTable() {
    val path = varchar("path", 500)
    val name = varchar("name", 255)
    val sizeMb = integer("size_mb")
    val modified = long("modified")
    val added = long("added")
}

class VideoFileEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<VideoFileEntity>(VideoFilesTable)

    var path by VideoFilesTable.path
    var name by VideoFilesTable.name
    var sizeMb by VideoFilesTable.sizeMb
    val videoStream by VideoEntity referrersOn VideosTable.videoFile
    val audioStreams by AudioEntity referrersOn AudiosTable.videoFile
    val subtitles by SubtitleEntity referrersOn SubtitlesTable.videoFile
    var modified by VideoFilesTable.modified
    var added by VideoFilesTable.added
}