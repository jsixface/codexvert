package io.github.jsixface.common

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
sealed class MediaTrack {
    abstract val codec: String
    abstract val index: Int

    override fun toString(): String {
        return "[$index:${codec.uppercase()}]"
    }

    @Serializable
    @SerialName("video")
    class VideoTrack(
        override val codec: String,
        override val index: Int,
        val codecTag: String,
        val profile: String,
        val resolution: String,
        val aspectRatio: String,
        val frameRate: Float,
        val bitRate: Int,
        val bitDepth: Int,
        val pixelFormat: String,
    ) : MediaTrack()

    @Serializable
    @SerialName("audio")
    class AudioTrack(
        override val codec: String,
        override val index: Int,
        val channels: Int,
        val layout: String,
        val bitRate: Int,
        val sampleRate: String,
        val language: String,
    ) : MediaTrack()

    @Serializable
    @SerialName("subtitle")
    class SubtitleTrack(
        override val codec: String,
        override val index: Int,
        val language: String
    ) : MediaTrack()
}

fun MediaTrack.isDolby() = codec.lowercase() in listOf("ac3", "eac3")

@Serializable
data class VideoList(
    val pathAndNames: Map<String, String>,
    val codecsCollection: CodecsCollection,
)

@Serializable
data class VideoFile(
    val path: String,
    val fileName: String,
    val modifiedTime: Long,
    val audios: List<MediaTrack> = listOf(),
    val videos: List<MediaTrack> = listOf(),
    val subtitles: List<MediaTrack> = listOf()
)

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
