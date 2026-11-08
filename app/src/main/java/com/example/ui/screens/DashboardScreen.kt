package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.AdminSectionHeader
import com.example.ui.components.MetricCard
import com.example.ui.components.StatusPill
import com.example.ui.navigation.AdminNavDestination
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    users: List<UserProfile>,
    templates: List<CvTemplate>,
    creatorApplications: List<CreatorApplication>,
    reports: List<SupportReport>,
    auditLogs: List<AuditLog>,
    onNavigate: (AdminNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalUsers = users.size
    val activeUsers = users.count { it.status == "active" }
    val totalCvs = users.sumOf { it.cvCount }
    val pendingTemplates = templates.count { it.status == "pending" }
    val approvedTemplates = templates.count { it.status == "approved" }
    val pendingCreators = creatorApplications.count { it.status == "pending" }
    val pendingReports = reports.count { it.status == "pending" || it.status == "under_review" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = NavySurface
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Platform Telemetry",
                                style = MaterialTheme.typography.labelMedium,
                                color = BlueAccent,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "CV Maker Operations",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SuccessEmerald.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "● LIVE SYNC",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessEmerald
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Real-time moderation and analytics for CV Maker. Managing $totalUsers registered users, $approvedTemplates published templates, and $pendingCreators pending creator requests.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark
                    )
                }
            }
        }

        // Metrics Grid
        item {
            AdminSectionHeader(
                title = "Core Metrics",
                subtitle = "Live activity from production Firestore"
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Total Users",
                    value = totalUsers.toString(),
                    icon = Icons.Default.People,
                    iconTint = BlueAccent,
                    subtitle = "$activeUsers active accounts",
                    onClick = { onNavigate(AdminNavDestination.USERS) },
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "CVs Created",
                    value = totalCvs.toString(),
                    icon = Icons.Default.Description,
                    iconTint = SuccessEmerald,
                    subtitle = "User authored",
                    onClick = { onNavigate(AdminNavDestination.USERS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Pending Templates",
                    value = pendingTemplates.toString(),
                    icon = Icons.Default.FolderOpen,
                    iconTint = WarningAmber,
                    subtitle = "$approvedTemplates approved in library",
                    onClick = { onNavigate(AdminNavDestination.TEMPLATES) },
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Creator Requests",
                    value = pendingCreators.toString(),
                    icon = Icons.Default.Badge,
                    iconTint = if (pendingCreators > 0) WarningAmber else BlueAccent,
                    subtitle = "Awaiting review",
                    onClick = { onNavigate(AdminNavDestination.CREATORS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Open Reports",
                    value = pendingReports.toString(),
                    icon = Icons.Default.Report,
                    iconTint = if (pendingReports > 0) DangerRose else SuccessEmerald,
                    subtitle = "Tickets & complaints",
                    onClick = { onNavigate(AdminNavDestination.REPORTS) },
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Total Templates",
                    value = templates.size.toString(),
                    icon = Icons.Default.Category,
                    iconTint = Color(0xFFC084FC),
                    subtitle = "Catalog size",
                    onClick = { onNavigate(AdminNavDestination.TEMPLATES) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Action Shortcuts
        item {
            AdminSectionHeader(title = "Administrative Actions")
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onNavigate(AdminNavDestination.TEMPLATES) },
                    colors = ButtonDefaults.buttonColors(containerColor = NavySurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Templates", fontSize = 12.sp)
                }
                Button(
                    onClick = { onNavigate(AdminNavDestination.CREATORS) },
                    colors = ButtonDefaults.buttonColors(containerColor = NavySurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.AssignmentInd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Creators", fontSize = 12.sp)
                }
                Button(
                    onClick = { onNavigate(AdminNavDestination.NOTIFICATIONS) },
                    colors = ButtonDefaults.buttonColors(containerColor = NavySurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Broadcast", fontSize = 12.sp)
                }
            }
        }

        // Recent Audit Activity
        item {
            AdminSectionHeader(
                title = "Recent Audit Trail",
                subtitle = "Recent administrator operations"
            )
        }

        if (auditLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No audit events recorded yet. Admin operations will appear here in real-time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark
                        )
                    }
                }
            }
        } else {
            items(auditLogs.take(5)) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = NavySurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BlueAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = BlueAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = log.action.replace("_", " ").uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = log.details,
                                fontSize = 12.sp,
                                color = TextSecondaryDark,
                                maxLines = 2
                            )
                            Text(
                                text = "By: ${log.adminEmail}",
                                fontSize = 10.sp,
                                color = TextMutedDark
                            )
                        }
                    }
                }
            }
        }
    }
}
