package io.github.jsixface.codexvert.route

import io.github.jsixface.codexvert.api.JobsApi
import io.github.jsixface.codexvert.logger
import io.github.jsixface.common.Api
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.koin.ktor.ext.inject

fun Route.jobRoutes() {
    val jobsApi by inject<JobsApi>()
    val logger = logger()

    get<Api.Jobs> {
        val jobs = jobsApi.getJobs()
        call.respond(jobs)
    }

    delete<Api.Jobs> {
        call.respond(jobsApi.clearFinished())
    }

    delete<Api.Jobs.Job> { job ->
        logger.info("Going to delete $job")
        jobsApi.stopJob(job.id)
        call.respond(job)
    }
}