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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SystemNotification
import com.example.ui.components.AdminSectionHeader
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    notifications: List<SystemNotification>,
    onSendNotification: (title: String, body: String, audience: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showComposer by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var selectedAudience by remember { mutableStateOf("all") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        AdminSectionHeader(
            title = "Broadcast Announcements",
            subtitle = "Push notices and in-app bulletins to CV Maker users",
            actionButton = {
                Button(
                    onClick = { showComposer = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimaryLight),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("new_announcement_button")
                ) {
                    Icon(imageVector = Icons.Default.AddComment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Broadcast Notice", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (notifications.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Campaign,
                title = "No Announcements Broadcasted Yet",
                description = "Send app-wide updates, new template releases, or maintenance notices to users."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(notifications, key = { it.id }) { notif ->
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Campaign,
                                        contentDescription = null,
                                        tint = BlueAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = notif.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BlueAccent.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Audience: ${notif.targetAudience.uppercase()}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BlueAccent
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = notif.body,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimaryDark
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Sent by: ${notif.sentBy}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMutedDark
                            )
                        }
                    }
                }
            }
        }
    }

    // Composer Dialog
    if (showComposer) {
        AlertDialog(
            onDismissRequest = { showComposer = false },
            title = { Text("Compose App Announcement", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Announcement Title") },
                        placeholder = { Text("e.g. New Creative CV Templates Available!") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("announcement_title_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text("Announcement Body") },
                        placeholder = { Text("Describe the update or notice in detail...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("announcement_body_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Column {
                        Text("Target Audience:", style = MaterialTheme.typography.labelMedium, color = TextMutedDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedAudience == "all",
                                onClick = { selectedAudience = "all" },
                                label = { Text("All Users") },
                                shape = RoundedCornerShape(8.dp)
                            )
                            FilterChip(
                                selected = selectedAudience == "creators",
                                onClick = { selectedAudience = "creators" },
                                label = { Text("Creators Only") },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() && body.isNotBlank()) {
                            onSendNotification(title.trim(), body.trim(), selectedAudience)
                            showComposer = false
                            title = ""
                            body = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimaryLight),
                    modifier = Modifier.testTag("confirm_send_announcement_button")
                ) {
                    Text("Broadcast Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showComposer = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
