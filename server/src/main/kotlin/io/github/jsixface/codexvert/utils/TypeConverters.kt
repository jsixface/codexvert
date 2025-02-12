package io.github.jsixface.codexvert.utils

import io.github.jsixface.codexvert.db.AudioEntity
import io.github.jsixface.codexvert.db.SubtitleEntity
import io.github.jsixface.codexvert.db.VideoEntity
import io.github.jsixface.codexvert.db.VideoFileEntity
import io.github.jsixface.codexvert.ffprobe.ProbeStream
import io.github.jsixface.common.MediaTrack
import io.github.jsixface.common.VideoFile
import org.jetbrains.exposed.sql.transactions.transaction

fun VideoEntity.updateInfo(stream: ProbeStream) {
    index = stream.index
    codec = stream.codecName
    codecTag = stream.codecTagName
    profile = stream.profile
    pixelFormat = stream.pixelFormat
    resolution = "${stream.width}x${stream.height}"
    aspectRatio = AspectRatio.from(stream.aspectRatio)?.toString() ?: ""
    frameRate = stream.frameRate.let {
        if (it.contains('/').not()) it.toFloatOrNull() else {
            val (f, s) = it.split("/").mapNotNull { x -> x.toFloatOrNull() }
            (f / s)
        }
    } ?: 0f

    bitRate = stream.bitRate.toIntOrNull() ?: 0
    bitDepth = 0
}

fun AudioEntity.updateInfo(stream: ProbeStream) {
    index = stream.index
    codec = stream.codecName
    channels = stream.channels
    layout = stream.channelLayout
    sampleRate = stream.sampleRate
    language = stream.tags["language"] ?: ""
    bitrate = stream.bitRate.toIntOrNull() ?: 0
}


fun Float.shortString() = if (this == toInt().toFloat()) this.toInt().toString() else "%.2f".format(this)

fun VideoFileEntity.toVideoFile(): VideoFile = transaction {
    val videoTracks = videoStream.map { it.toMediaTrack() }
    val audioTracks = audioStreams.map { it.toMediaTrack() }
    val subtitleTracks = subtitles.map { it.toMediaTrack() }
    VideoFile(
        path = path,
        fileName = name,
        modifiedTime = modified,
        audios = audioTracks,
        videos = videoTracks,
        subtitles = subtitleTracks,
    )
}

private fun VideoEntity.toMediaTrack() = MediaTrack.VideoTrack(
    codec, index, codecTag, profile, resolution, aspectRatio, frameRate ?: 0f, bitRate, bitDepth, pixelFormat
)

private fun AudioEntity.toMediaTrack() =
    MediaTrack.AudioTrack(codec, index, channels, layout, bitrate, sampleRate, language)

private fun SubtitleEntity.toMediaTrack() = MediaTrack.SubtitleTrack(codec, index, language)
