package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.data.model.AdminRole
import com.example.data.model.Administrator
import com.example.data.model.AppSettings
import com.example.ui.components.AdminSectionHeader
import com.example.ui.theme.*

@Composable
fun AppSettingsScreen(
    currentAdmin: Administrator?,
    appSettings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSuperAdmin = currentAdmin?.adminRole == AdminRole.SUPER_ADMIN
    val scrollState = rememberScrollState()

    var maintenanceMode by remember(appSettings) { mutableStateOf(appSettings.maintenanceMode) }
    var announcementActive by remember(appSettings) { mutableStateOf(appSettings.announcementActive) }
    var announcementText by remember(appSettings) { mutableStateOf(appSettings.announcementText) }
    var communitySubmissionsEnabled by remember(appSettings) { mutableStateOf(appSettings.communitySubmissionsEnabled) }
    var supportEmail by remember(appSettings) { mutableStateOf(appSettings.supportEmail) }

    var hasChanges by remember { mutableStateOf(false) }

    fun checkDirty() {
        hasChanges = (maintenanceMode != appSettings.maintenanceMode ||
                announcementActive != appSettings.announcementActive ||
                announcementText != appSettings.announcementText ||
                communitySubmissionsEnabled != appSettings.communitySubmissionsEnabled ||
                supportEmail != appSettings.supportEmail)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        AdminSectionHeader(
            title = "Global Platform Settings",
            subtitle = "Control runtime availability and app configuration for CV Maker",
            actionButton = {
                if (isSuperAdmin && hasChanges) {
                    Button(
                        onClick = {
                            val updated = appSettings.copy(
                                maintenanceMode = maintenanceMode,
                                announcementActive = announcementActive,
                                announcementText = announcementText.trim(),
                                communitySubmissionsEnabled = communitySubmissionsEnabled,
                                supportEmail = supportEmail.trim()
                            )
                            onSaveSettings(updated)
                            hasChanges = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_settings_button")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Changes", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )

        // Maintenance Mode Toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (maintenanceMode) DangerRose.copy(alpha = 0.15f) else NavySurface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = if (maintenanceMode) DangerRose else WarningAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Emergency Maintenance Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (maintenanceMode) DangerRose else Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "When enabled, the CV Maker app shows a friendly maintenance banner and restricts creating new resumes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark
                        )
                    }
                    Switch(
                        checked = maintenanceMode,
                        onCheckedChange = {
                            if (isSuperAdmin) {
                                maintenanceMode = it
                                checkDirty()
                            }
                        },
                        enabled = isSuperAdmin,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = DangerRose
                        ),
                        modifier = Modifier.testTag("maintenance_mode_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // In-App Announcement Banner Settings
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
                            text = "In-App Announcement Banner",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Displays a top banner inside the client CV Maker app",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark
                        )
                    }
                    Switch(
                        checked = announcementActive,
                        onCheckedChange = {
                            if (isSuperAdmin) {
                                announcementActive = it
                                checkDirty()
                            }
                        },
                        enabled = isSuperAdmin,
                        modifier = Modifier.testTag("announcement_active_switch")
                    )
                }

                if (announcementActive) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = announcementText,
                        onValueChange = {
                            if (isSuperAdmin) {
                                announcementText = it
                                checkDirty()
                            }
                        },
                        label = { Text("Banner Message") },
                        placeholder = { Text("e.g. Scheduled system upgrade on Sunday at 2 AM UTC.") },
                        enabled = isSuperAdmin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("announcement_text_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Community Submissions Setting
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
                            text = "Community Template Submissions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Allow approved creators to submit new template designs for review",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark
                        )
                    }
                    Switch(
                        checked = communitySubmissionsEnabled,
                        onCheckedChange = {
                            if (isSuperAdmin) {
                                communitySubmissionsEnabled = it
                                checkDirty()
                            }
                        },
                        enabled = isSuperAdmin,
                        modifier = Modifier.testTag("community_submissions_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Support Email Setting
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Official Support Contact",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Contact email surfaced to CV Maker users in the Help & Feedback screen",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = supportEmail,
                    onValueChange = {
                        if (isSuperAdmin) {
                            supportEmail = it
                            checkDirty()
                        }
                    },
                    label = { Text("Support Email") },
                    enabled = isSuperAdmin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("support_email_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
