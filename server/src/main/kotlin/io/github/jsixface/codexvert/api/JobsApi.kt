package io.github.jsixface.codexvert.api

import io.github.jsixface.codexvert.db.IJobsRepo
import io.github.jsixface.common.ConversionJob
import io.github.jsixface.common.JobStatus
import io.github.jsixface.common.JobsResponse
import kotlinx.coroutines.cancelAndJoin

class JobsApi(private val conversionApi: ConversionApi, private val jobsRepo: IJobsRepo) {

    suspend fun getJobs(page: Int = 1, limit: Int = 10): JobsResponse {
        val inMemoryJobs = conversionApi.jobs.map {
            val startedTime = it.startedAt.time.let { t ->
                "${t.hour.toString().padStart(2, '0')}:${t.minute.toString().padStart(2, '0')}:${
                    t.second.toString().padStart(2, '0')
                }"
            }
            ConversionJob(
                jobId = it.jobId,
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

        val offset = (page - 1).coerceAtLeast(0).toLong() * limit
        val completedJobs = jobsRepo.getCompletedJobs(offset, limit)
        val totalCompleted = jobsRepo.countCompletedJobs()

        return JobsResponse(
            jobs = inMemoryJobs + completedJobs,
            totalCompleted = totalCompleted,
            page = page,
            itemsPerPage = limit
        )
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