package io.github.jsixface.codexvert.api

import io.github.jsixface.common.ConversionJob
import io.github.jsixface.common.JobStatus
import kotlinx.coroutines.cancelAndJoin

class JobsApi(private val conversionApi: ConversionApi) {

    fun getJobs(): List<ConversionJob> = conversionApi.jobs.map {
        val startedTime = it.startedAt.time.apply { "$hour:$minute:$second" }
        ConversionJob(jobId = it.jobId,
                progress = it.progress.value,
                file = it.videoFile,
                status = if (it.job == null)
                    JobStatus.Queued
                else
                    when (it.progress.value) {
                        0 -> JobStatus.Starting
                        100 -> JobStatus.Completed
                        -1 -> JobStatus.Failed
                        else -> JobStatus.InProgress
                    },
                startedAt = "${it.startedAt.date} $startedTime"
        )
    }

    fun clearFinished(): List<ConversionJob> {
        conversionApi.clearFinished()
        return getJobs()
    }

    suspend fun stopJob(jobId: String) {
        val convertingJob = conversionApi.jobs.find { it.jobId == jobId }
        convertingJob?.let {
            if (it.job != null && it.job?.isActive == true) {
                it.job?.cancelAndJoin()
                it.progress.value = -1
                it.outFile.deleteOnExit()
            } else {
                conversionApi.jobs.remove(it)
            }
        }
    }
}