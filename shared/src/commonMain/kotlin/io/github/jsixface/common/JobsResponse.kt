package io.github.jsixface.common

import kotlinx.serialization.Serializable

@Serializable
data class JobsResponse(
    val jobs: List<ConversionJob>,
    val totalCompleted: Long,
    val page: Int,
    val itemsPerPage: Int
)
