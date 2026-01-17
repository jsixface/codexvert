package io.github.jsixface.common

import kotlinx.serialization.Serializable

@Serializable
data class ConversionJob(
    val jobId: String,
    val status: JobStatus,
    val progress: Int,
    val file: VideoFile,
    val startedAt: String,
    val duration: String? = null
)

enum class JobStatus {
    Starting,
    InProgress,
    Completed,
    Failed,
    Queued,
}