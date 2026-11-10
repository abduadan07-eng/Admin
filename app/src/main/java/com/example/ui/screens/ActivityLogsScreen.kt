package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
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
import com.example.data.model.AuditLog
import com.example.ui.components.AdminSectionHeader
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*

@Composable
fun ActivityLogsScreen(
    auditLogs: List<AuditLog>,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredLogs = remember(auditLogs, searchQuery) {
        if (searchQuery.isBlank()) auditLogs
        else {
            auditLogs.filter { log ->
                log.action.contains(searchQuery, ignoreCase = true) ||
                        log.adminEmail.contains(searchQuery, ignoreCase = true) ||
                        log.details.contains(searchQuery, ignoreCase = true) ||
                        log.targetType.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        AdminSectionHeader(
            title = "System Audit Trail",
            subtitle = "Immutable record of all administrative operations"
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter audit logs by action, admin, or target...") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondaryDark)
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("audit_search_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = NavySurface,
                unfocusedContainerColor = NavySurface,
                focusedBorderColor = BlueAccent,
                unfocusedBorderColor = NavyBorder
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredLogs.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.History,
                title = "No Audit Records Found",
                description = "All privileged actions (moderating users, approving templates, role assignments) are recorded here automatically."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredLogs, key = { it.id }) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = NavySurface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(BlueAccent.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = BlueAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = log.action.replace("_", " ").uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Target: [${log.targetType.uppercase()}] ${log.targetId}",
                                            fontSize = 11.sp,
                                            color = BlueAccent
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = log.details,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimaryDark
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Operator: ${log.adminEmail}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMutedDark
                                )
                                Text(
                                    text = "Doc: ${log.id.take(8)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMutedDark
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
