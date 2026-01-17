package io.github.jsixface.codexvert.db

import io.github.jsixface.common.ConversionJob
import io.github.jsixface.common.JobStatus
import io.github.jsixface.common.VideoFile
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

interface IJobsRepo {
    suspend fun save(job: ConversionJob, duration: String)
    suspend fun getCompletedJobs(offset: Long, limit: Int): List<ConversionJob>
    suspend fun countCompletedJobs(): Long
}

class JobsRepo(private val db: Database) : IJobsRepo {
    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }

    override suspend fun save(job: ConversionJob, duration: String) = dbQuery {
        CompletedJobEntity.new {
            jobId = job.jobId
            status = job.status.name
            filePath = job.file.path
            fileName = job.file.fileName
            startedAt = job.startedAt
            this.duration = duration
        }
        Unit
    }

    override suspend fun getCompletedJobs(offset: Long, limit: Int): List<ConversionJob> = dbQuery {
        CompletedJobEntity.all()
            .orderBy(CompletedJobsTable.id to SortOrder.DESC)
            .limit(limit)
            .offset(offset)
            .map {
                val duration = it.duration.let { iso ->
                    val dur = kotlin.time.Duration.parseIsoStringOrNull(iso)
                    dur?.let { d ->
                        val totalSeconds = d.inWholeSeconds
                        val hours = totalSeconds / 3600
                        val minutes = (totalSeconds % 3600) / 60
                        val seconds = totalSeconds % 60
                        if (hours > 0) String.format("%02d:%02d:%02d", hours, minutes, seconds)
                        else String.format("%02d:%02d", minutes, seconds)
                    } ?: iso
                }
                ConversionJob(
                    jobId = it.jobId,
                    status = JobStatus.valueOf(it.status),
                    progress = if (JobStatus.valueOf(it.status) == JobStatus.Completed) 100 else -1,
                    file = VideoFile(
                        path = it.filePath,
                        fileName = it.fileName,
                        modifiedTime = 0, // Not needed for the UI display in jobs list
                        videos = emptyList(),
                        audios = emptyList(),
                        subtitles = emptyList()
                    ),
                    startedAt = it.startedAt,
                    duration = duration
                )
            }
    }

    override suspend fun countCompletedJobs(): Long = dbQuery {
        CompletedJobEntity.count()
    }
}
