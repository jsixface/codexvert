package io.github.jsixface.codexvert.utils

import io.github.jsixface.codexvert.db.AudioEntity
import io.github.jsixface.codexvert.db.VideoEntity
import io.github.jsixface.codexvert.ffprobe.ProbeStream

fun VideoEntity.updateInfo(stream: ProbeStream) {
    index = stream.index
    codec = stream.codecName
    codecTag = stream.codecTagName
    profile = stream.profile
    pixelFormat = stream.pixelFormat
    resolution = "${stream.width}x${stream.height}"
    aspectRatio = AspectRatio(stream.aspectRatio).toString()
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

