package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.CreatorApplication
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusPill
import com.example.ui.theme.*

@Composable
fun CreatorsScreen(
    applications: List<CreatorApplication>,
    onReviewApplication: (applicationId: String, userId: String, approve: Boolean, reason: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var applicationToReview by remember { mutableStateOf<CreatorApplication?>(null) }
    var isApproveAction by remember { mutableStateOf(true) }
    var reviewFeedback by remember { mutableStateOf("") }

    val tabs = listOf("Pending", "Approved", "Rejected", "All")

    val filteredApplications = remember(applications, selectedTab) {
        when (selectedTab) {
            0 -> applications.filter { it.status == "pending" }
            1 -> applications.filter { it.status == "approved" }
            2 -> applications.filter { it.status == "rejected" }
            else -> applications
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Tab Navigation
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = NavySurface,
            contentColor = BlueAccent
        ) {
            tabs.forEachIndexed { index, title ->
                val count = when (index) {
                    0 -> applications.count { it.status == "pending" }
                    1 -> applications.count { it.status == "approved" }
                    2 -> applications.count { it.status == "rejected" }
                    else -> applications.size
                }
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = "$title ($count)",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredApplications.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Badge,
                title = "No Applications in ${tabs[selectedTab]}",
                description = "Candidates who apply through the CV Maker app to become template creators will appear here for review."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredApplications, key = { it.id }) { app ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavySurface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.applicantName.ifBlank { "Applicant" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = app.applicantEmail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondaryDark
                                    )
                                }
                                StatusPill(status = app.status)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (app.portfolioUrl.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        tint = BlueAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Portfolio: ${app.portfolioUrl}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BlueAccent
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            if (app.experience.isNotBlank()) {
                                Text(
                                    text = "Experience & Background:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMutedDark,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = app.experience,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimaryDark
                                )
                            }

                            if (app.rejectionReason.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Review Feedback: ${app.rejectionReason}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DangerRose
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (app.status == "pending") {
                                    OutlinedButton(
                                        onClick = {
                                            applicationToReview = app
                                            isApproveAction = false
                                            reviewFeedback = ""
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRose),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Reject", fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            applicationToReview = app
                                            isApproveAction = true
                                            reviewFeedback = "Application approved. Creator privileges enabled."
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Approve Creator", fontSize = 12.sp)
                                    }
                                } else if (app.status == "approved") {
                                    OutlinedButton(
                                        onClick = {
                                            applicationToReview = app
                                            isApproveAction = false
                                            reviewFeedback = "Creator privileges suspended by administrator"
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningAmber),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Suspend Privileges", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Review Dialog
    if (applicationToReview != null) {
        val targetApp = applicationToReview!!
        AlertDialog(
            onDismissRequest = { applicationToReview = null },
            title = {
                Text(
                    text = if (isApproveAction) "Approve Creator Application" else "Reject Creator Application",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Applicant: ${targetApp.applicantName} (${targetApp.applicantEmail})",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = reviewFeedback,
                        onValueChange = { reviewFeedback = it },
                        label = { Text("Review Feedback / Notes") },
                        placeholder = { Text(if (isApproveAction) "Welcome note or publishing guidelines" else "Reason for rejection") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("creator_review_feedback_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reason = reviewFeedback.ifBlank {
                            if (isApproveAction) "Approved by administrator" else "Does not meet portfolio criteria"
                        }
                        onReviewApplication(targetApp.id, targetApp.userId, isApproveAction, reason)
                        applicationToReview = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isApproveAction) SuccessEmerald else DangerRose
                    ),
                    modifier = Modifier.testTag("confirm_creator_review_button")
                ) {
                    Text(if (isApproveAction) "Confirm Approval" else "Confirm Rejection")
                }
            },
            dismissButton = {
                TextButton(onClick = { applicationToReview = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
