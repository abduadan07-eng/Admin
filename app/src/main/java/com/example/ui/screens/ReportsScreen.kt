package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SupportReport
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusPill
import com.example.ui.theme.*

@Composable
fun ReportsScreen(
    reports: List<SupportReport>,
    onUpdateReportStatus: (reportId: String, status: String, notes: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStatus by remember { mutableStateOf("all") }
    var reportToResolve by remember { mutableStateOf<SupportReport?>(null) }
    var newResolutionStatus by remember { mutableStateOf("resolved") }
    var resolutionNotes by remember { mutableStateOf("") }

    val filteredReports = remember(reports, selectedStatus) {
        if (selectedStatus == "all") reports
        else reports.filter { it.status == selectedStatus }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Status Tabs
        val filterOptions = listOf(
            "all" to "All (${reports.size})",
            "pending" to "Pending (${reports.count { it.status == "pending" }})",
            "under_review" to "Under Review (${reports.count { it.status == "under_review" }})",
            "resolved" to "Resolved (${reports.count { it.status == "resolved" }})"
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filterOptions) { (key, label) ->
                FilterChip(
                    selected = selectedStatus == key,
                    onClick = { selectedStatus = key },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BlueAccent.copy(alpha = 0.2f),
                        selectedLabelColor = BlueAccent
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredReports.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.CheckCircleOutline,
                title = "No Reports in Selected Filter",
                description = "User complaints, template copyright flags, and content issues reported by CV Maker users will appear here."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredReports, key = { it.id }) { report ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavySurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ReportProblem,
                                            contentDescription = null,
                                            tint = if (report.status == "resolved") SuccessEmerald else DangerRose,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = report.reason,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Target: [${report.targetType.uppercase()}] ${report.targetTitle.ifBlank { report.targetId }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BlueAccent
                                    )
                                }
                                StatusPill(status = report.status)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = report.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimaryDark
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Reporter: ${report.reporterEmail}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMutedDark
                            )

                            if (report.resolutionNotes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Resolution Notes (${report.resolvedBy}): ${report.resolutionNotes}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SuccessEmerald,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (report.status != "resolved") {
                                    if (report.status == "pending") {
                                        OutlinedButton(
                                            onClick = {
                                                onUpdateReportStatus(report.id, "under_review", "Investigating report details")
                                            },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Mark Under Review", fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Button(
                                        onClick = {
                                            reportToResolve = report
                                            newResolutionStatus = "resolved"
                                            resolutionNotes = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Resolve Ticket", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Resolution Modal
    if (reportToResolve != null) {
        val targetReport = reportToResolve!!
        AlertDialog(
            onDismissRequest = { reportToResolve = null },
            title = { Text("Resolve Support Report", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Report: ${targetReport.reason} (${targetReport.targetType})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = resolutionNotes,
                        onValueChange = { resolutionNotes = it },
                        label = { Text("Resolution Notes & Action Taken") },
                        placeholder = { Text("e.g. Inappropriate template was suspended; creator notified") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("report_resolution_notes_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val notes = resolutionNotes.ifBlank { "Resolved by support admin" }
                        onUpdateReportStatus(targetReport.id, newResolutionStatus, notes)
                        reportToResolve = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald)
                ) {
                    Text("Confirm Resolution")
                }
            },
            dismissButton = {
                TextButton(onClick = { reportToResolve = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
