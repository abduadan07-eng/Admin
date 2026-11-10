package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CvTemplate
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusPill
import com.example.ui.theme.*

@Composable
fun TemplatesScreen(
    templates: List<CvTemplate>,
    onUpdateTemplateStatus: (templateId: String, newStatus: String, reason: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedStatus by remember { mutableStateOf("all") }
    var templateToModerate by remember { mutableStateOf<CvTemplate?>(null) }
    var actionType by remember { mutableStateOf("approved") }
    var moderationReason by remember { mutableStateOf("") }

    val categories = listOf("All", "Professional", "Modern", "Creative", "Executive", "Minimal", "Academic")

    val filteredTemplates = remember(templates, selectedCategory, selectedStatus) {
        templates.filter { tmpl ->
            val matchesCategory = selectedCategory == "All" || tmpl.category.equals(selectedCategory, ignoreCase = true)
            val matchesStatus = selectedStatus == "all" || tmpl.status.equals(selectedStatus, ignoreCase = true)
            matchesCategory && matchesStatus
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Status Tabs
        val statusList = listOf(
            "all" to "All (${templates.size})",
            "pending" to "Pending (${templates.count { it.status == "pending" }})",
            "approved" to "Approved (${templates.count { it.status == "approved" }})",
            "suspended" to "Suspended (${templates.count { it.status == "suspended" }})"
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(statusList) { (key, label) ->
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

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                SuggestionChip(
                    onClick = { selectedCategory = category },
                    label = { Text(category, fontSize = 11.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (selectedCategory == category) NavySurfaceVariant else NavySurface,
                        labelColor = if (selectedCategory == category) BlueAccent else TextSecondaryDark
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredTemplates.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Description,
                title = "No Templates in Selected View",
                description = "Templates submitted by community creators or bundled in the CV Maker library will be displayed here."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredTemplates, key = { it.id }) { template ->
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
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(NavySurfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Article,
                                            contentDescription = null,
                                            tint = BlueAccent,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = template.effectiveTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "By ${template.creatorName.ifBlank { "Verified Creator" }} • ${template.category}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondaryDark
                                        )
                                    }
                                }

                                StatusPill(status = template.status)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(NavySurfaceVariant.copy(alpha = 0.5f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Downloads: ${template.downloadsCount}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimaryDark
                                )
                                Text(
                                    text = "Rating: ★ ${template.rating}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GoldAccent
                                )
                                Text(
                                    text = if (template.isPremium) "Premium" else "Free",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (template.isPremium) GoldAccent else SuccessEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (template.rejectionReason.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Moderation Note: ${template.rejectionReason}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DangerRose,
                                    fontSize = 11.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                when (template.status) {
                                    "pending" -> {
                                        OutlinedButton(
                                            onClick = {
                                                templateToModerate = template
                                                actionType = "rejected"
                                                moderationReason = ""
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRose),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Reject", fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                templateToModerate = template
                                                actionType = "approved"
                                                moderationReason = "Meets CV Maker quality standards"
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Approve to Library", fontSize = 12.sp)
                                        }
                                    }
                                    "approved" -> {
                                        OutlinedButton(
                                            onClick = {
                                                templateToModerate = template
                                                actionType = "suspended"
                                                moderationReason = ""
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningAmber),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Suspend Template", fontSize = 12.sp)
                                        }
                                    }
                                    "suspended" -> {
                                        Button(
                                            onClick = {
                                                templateToModerate = template
                                                actionType = "approved"
                                                moderationReason = "Restored after moderation review"
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Restore to Library", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Moderation Dialog
    if (templateToModerate != null) {
        val targetTmpl = templateToModerate!!
        val isApproving = actionType == "approved"
        AlertDialog(
            onDismissRequest = { templateToModerate = null },
            title = {
                Text(
                    text = if (isApproving) "Approve Template to CV Maker Library" else "${actionType.replaceFirstChar { it.uppercase() }} Template",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Template: ${targetTmpl.effectiveTitle} (${targetTmpl.category})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isApproving) "Approved templates immediately appear in the CV Maker application library for all users to select and edit."
                        else "Suspended or rejected templates will be hidden from the CV Maker catalog. Please specify the reason below:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = moderationReason,
                        onValueChange = { moderationReason = it },
                        label = { Text("Reason / Notes") },
                        placeholder = { Text(if (isApproving) "Approval comments" else "Reason for suspension/rejection") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("template_moderation_reason_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reason = moderationReason.ifBlank {
                            if (isApproving) "Approved by template moderator" else "Administrative suspension"
                        }
                        onUpdateTemplateStatus(targetTmpl.id, actionType, reason)
                        templateToModerate = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isApproving) SuccessEmerald else DangerRose
                    ),
                    modifier = Modifier.testTag("confirm_template_action_button")
                ) {
                    Text(if (isApproving) "Confirm Approval" else "Confirm $actionType")
                }
            },
            dismissButton = {
                TextButton(onClick = { templateToModerate = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
