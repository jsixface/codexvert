package io.github.jsixface.common

import io.ktor.resources.Resource

sealed interface Api {

    @Resource("/videos")
    data object Videos : Api {
        @Resource("video")
        data class Video(val parent: Videos = Videos, val path: String?) : Api
    }

    @Resource("/backups")
    data object Backups: Api {
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