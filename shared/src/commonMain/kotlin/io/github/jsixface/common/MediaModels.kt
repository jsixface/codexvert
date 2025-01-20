package io.github.jsixface.common

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class Codec(
    val type: TrackType,
    val ffmpegParams: List<String> = emptyList(),
) {
    AAC(TrackType.Audio, listOf("aac")),
    Opus(TrackType.Audio, listOf("libopus", "-b:a", "128K")),
    MP3(TrackType.Audio, listOf("mp3", "-b:a", "128K")),
    HEVC(TrackType.Video, listOf("libx265")),
    H264(TrackType.Video, listOf("libx264")),
    MPEG4(TrackType.Video, listOf("mpeg4")),
}

@Serializable
data class CodecsCollection(
    val video: List<String>,
    val audio: List<String>,
    val subtitle: List<String>,
)

@Serializable
sealed class Conversion {
    @Serializable
    data object Copy : Conversion()

    @Serializable
    data object Drop : Conversion()

    @Serializable
    data class Convert(val codec: Codec) : Conversion()
}

enum class TrackType {
    Video, Audio, Subtitle
}

@Serializable
data class MediaTrack(
    val type: TrackType,
    val index: Int,
    val codec: String
)

fun MediaTrack.isDolby() = codec.lowercase() in listOf("ac3", "eac3")

@Serializable
data class VideoFile(
    val path: String,
    val fileName: String,
    val modifiedTime: Long,
    val audios: List<MediaTrack> = listOf(),
    val videos: List<MediaTrack> = listOf(),
    val subtitles: List<MediaTrack> = listOf()
) {
    val videoInfo: String
        get() = videos.joinToString { it.codec }

    val audioInfo: String
        get() = audios.joinToString { it.codec }

    val subtitleInfo: String
        get() = subtitles.joinToString { it.codec }

    val modified: String
        get() {
            val dateTime = Instant.fromEpochMilliseconds(modifiedTime)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            return "${dateTime.date} ${dateTime.time}"
        }

}

@Serializable
data class MediaStream(
    val index: Int,
    @SerialName("codec_name")
    val codecName: String = "",
    @SerialName("codec_type")
    val codecType: String,
    val channels: Int = 1
)

@Serializable
data class MediaProbeInfo(
    val streams: List<MediaStream>
)
