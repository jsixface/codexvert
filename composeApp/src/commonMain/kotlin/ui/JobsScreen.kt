package ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.jsixface.common.ConversionJob
import io.github.jsixface.common.JobStatus.Completed
import io.github.jsixface.common.JobStatus.Failed
import io.github.jsixface.common.JobStatus.InProgress
import io.github.jsixface.common.JobStatus.Queued
import io.github.jsixface.common.JobStatus.Starting
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ui.model.ModelState
import ui.model.Screen
import viewmodels.JobsScreenModel

object JobsScreen : Screen {

    private val padding = Modifier.padding(16.dp)
    private val paddingSmall = Modifier.padding(8.dp)

    override val name: String
        get() = "Jobs"

    @Composable
    override fun icon() {
        Icon(Icons.Filled.Inbox, contentDescription = name)
    }

    @Composable
    override fun content() {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val jobsScreenModel = koinInject<JobsScreenModel>()
            val scope = rememberCoroutineScope()
            var jobs by remember { mutableStateOf(listOf<ConversionJob>()) }
            LaunchedEffect(Unit) {
                scope.launch {
                    jobsScreenModel.jobs.collect { jobResult ->
                        when (jobResult) {
                            is ModelState.Error, is ModelState.Init -> {}
                            is ModelState.Success -> jobs = jobResult.result
                        }
                    }
                }
            }
            JobContent(
                jobs,
                onClear = { scope.launch { jobsScreenModel.clearJobs() } },
                onDelete = { scope.launch { jobsScreenModel.delete(it) } })
        }
    }

    @Composable
    fun JobContent(jobs: List<ConversionJob>, onDelete: (String) -> Unit, onClear: () -> Unit) {
        Card(
            modifier = Modifier.width(width = 900.dp).fillMaxHeight().padding(20.dp)
        ) {
            Column(modifier = padding) {
                Row {
                    Text(
                        text = "Jobs",
                        fontSize = 30.sp,
                        modifier = padding,
                        textAlign = TextAlign.Center,
                    )
                }
                Column {
                    jobs.forEach { job -> JobItem(job) { onDelete(job.jobId) } }
                }
                Row(modifier = Modifier.fillMaxSize()) {
                    Spacer(modifier = Modifier.weight(1f))
                    ElevatedButton(onClick = onClear, modifier = padding) {
                        Text("Clear Completed")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }

    @Composable
    fun JobItem(job: ConversionJob, onDelete: () -> Unit) {
        Column {
            val progressPadding = Modifier.padding(8.dp).fillMaxWidth()
            OutlinedCard(
                modifier = Modifier.padding(16.dp, 4.dp).fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f).padding(8.dp)) {
                        Text(job.file.fileName, modifier = paddingSmall)
                        when (job.status) {
                            Starting -> LinearProgressIndicator(modifier = progressPadding)
                            InProgress -> LinearProgressIndicator(
                                progress = { job.progress / 100.0f },
                                modifier = progressPadding,
                            )

                            Completed -> Text(
                                "Completed",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = paddingSmall
                            )

                            Failed -> Text(
                                "Failed",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = paddingSmall
                            )

                            Queued -> Text(
                                "Queued",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = paddingSmall
                            )
                        }
                    }
                    if (job.status !in listOf(Failed, Completed)) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Sharp.Close, "Cancel")
                        }
                    }
                }
            }
        }
    }
}
