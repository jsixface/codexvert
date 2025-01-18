package io.github.jsixface.codexvert.ffprobe

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProbeStream(
    val index: Int,
    @SerialName("codec_name") val codecName: String = "",
    @SerialName("codec_type") val codecType: String,
    @SerialName("codec_tag_string") val codecTagName: String = "",
    val profile: String = "",
    val width: Int = 0,
    val height: Int = 0,
    @SerialName("display_aspect_ratio") val aspectRatio: String = "",
    @SerialName("avg_frame_rate") val frameRate: String = "",
    val bitRate: String = "",
    val bitDepth: String = "",
    @SerialName("pix_fmt") val pixelFormat: String = "",

    val channels: Int = 1,
    @SerialName("channel_layout") val channelLayout: String = "",
    @SerialName("sample_rate") val sampleRate: String = "",
    @SerialName("tags") val tags: Map<String, String> = emptyMap(),
)

@Serializable
data class ProbeFormat(
    @SerialName("filename") val fileName: String,
    @SerialName("nb_streams") val numStreams: Int,
    @SerialName("format_long_name") val formatName: String,
    @SerialName("duration") val duration: String = "",
)

@Serializable
data class ProbeInfo(
    val streams: List<ProbeStream>,
    val format: ProbeFormat,
)