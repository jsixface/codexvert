package ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.sharp.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.sharp.KeyboardArrowRight
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import io.github.jsixface.common.JobsResponse
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ui.model.ModelState
import viewmodels.JobsScreenModel


private val padding = Modifier.padding(16.dp)
private val paddingSmall = Modifier.padding(8.dp)


@Composable
fun JobsScreen() {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val jobsScreenModel = koinInject<JobsScreenModel>()
        val scope = rememberCoroutineScope()
        var jobsResponse by remember { mutableStateOf<JobsResponse?>(null) }
        LaunchedEffect(Unit) {
            scope.launch {
                jobsScreenModel.jobsResponse.collect { jobResult ->
                    when (jobResult) {
                        is ModelState.Error, is ModelState.Init -> {}
                        is ModelState.Success -> jobsResponse = jobResult.result
                    }
                }
            }
        }
        JobContent(
            jobsResponse,
            onPageChange = { jobsScreenModel.setPage(it) },
            onItemsPerPageChange = { jobsScreenModel.setItemsPerPage(it) },
            onDelete = { scope.launch { jobsScreenModel.delete(it) } })
    }
}

@Composable
fun JobContent(
    jobsResponse: JobsResponse?,
    onPageChange: (Int) -> Unit,
    onItemsPerPageChange: (Int) -> Unit,
    onDelete: (String) -> Unit
) {
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
                jobsResponse?.jobs?.forEach { job -> JobItem(job) { onDelete(job.jobId) } }
            }
            if (jobsResponse != null) {
                PaginationControls(
                    totalItems = jobsResponse.totalCompleted,
                    currentPage = jobsResponse.page,
                    itemsPerPage = jobsResponse.itemsPerPage,
                    onPageChange = onPageChange,
                    onItemsPerPageChange = onItemsPerPageChange
                )
            }
        }
    }
}

@Composable
fun PaginationControls(
    totalItems: Long,
    currentPage: Int,
    itemsPerPage: Int,
    onPageChange: (Int) -> Unit,
    onItemsPerPageChange: (Int) -> Unit
) {
    val totalPages = kotlin.math.ceil(totalItems.toDouble() / itemsPerPage).toInt().coerceAtLeast(1)
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text("Items per page:")
        Spacer(modifier = Modifier.width(8.dp))
        var expanded by remember { mutableStateOf(false) }
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(itemsPerPage.toString())
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                listOf(10, 20, 50, 100).forEach { limit ->
                    DropdownMenuItem(
                        text = { Text(limit.toString()) },
                        onClick = {
                            onItemsPerPageChange(limit)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = { onPageChange(currentPage - 1) }, enabled = currentPage > 1) {
            Icon(Icons.AutoMirrored.Sharp.KeyboardArrowLeft, contentDescription = "Previous Page")
        }
        Text("Page $currentPage of $totalPages")
        IconButton(onClick = { onPageChange(currentPage + 1) }, enabled = currentPage < totalPages) {
            Icon(Icons.AutoMirrored.Sharp.KeyboardArrowRight, contentDescription = "Next Page")
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
                        Starting -> Column {
                            LinearProgressIndicator(modifier = progressPadding)
                            Text(
                                "Started at: ${job.startedAt}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = paddingSmall
                            )
                        }

                        InProgress -> Column {
                            LinearProgressIndicator(
                                progress = { job.progress / 100.0f },
                                modifier = progressPadding,
                            )
                            Text(
                                "Started at: ${job.startedAt}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = paddingSmall
                            )
                        }

                        Completed -> Row {
                            Text(
                                "Completed",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = paddingSmall
                            )
                            Text(
                                "Started at: ${job.startedAt}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = paddingSmall
                            )
                            job.duration?.let {
                                Text(
                                    "Duration: $it",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = paddingSmall
                                )
                            }
                        }

                        Failed -> Row {
                            Text(
                                "Failed",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = paddingSmall
                            )
                            Text(
                                "Started at: ${job.startedAt}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = paddingSmall
                            )
                            job.duration?.let {
                                Text(
                                    "Duration: $it",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = paddingSmall
                                )
                            }
                        }

                        Queued -> Column {
                            Text(
                                "Queued",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = paddingSmall
                            )
                            Text(
                                "Started at: ${job.startedAt}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = paddingSmall
                            )
                        }
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

