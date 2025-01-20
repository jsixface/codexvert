package io.github.jsixface.common

import io.ktor.resources.Resource

sealed interface Api {

    @Resource("/videos")
    data class Videos(val videoFilter: String? = null, val audioFilter: String? = null) : Api {
        @Resource("video")
        data class Video(val parent: Videos = Videos(), val path: String?) : Api
    }

    @Resource("/codecs")
    data object Codecs : Api

    @Resource("/backups")
    data object Backups : Api {
        @Resource("/backup")
        data class Backup(val parent: Backups = Backups, val path: String) : Api
    }

    @Resource("/settings")
    data object Settings : Api

    @Resource("/jobs")
    data object Jobs : Api {
        @Resource("{id}")
        data class Job(val parent: Jobs = Jobs, val id: String) : Api
    }
}