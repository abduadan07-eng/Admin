package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector

enum class AdminNavDestination(
    val title: String,
    val icon: ImageVector,
    val subtitle: String
) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard, "Platform Overview & Telemetry"),
    USERS("Users", Icons.Default.People, "Account & Access Moderation"),
    CREATORS("Creators", Icons.Default.Badge, "Creator Verification & Applications"),
    TEMPLATES("Templates", Icons.Default.Description, "Community Template Moderation"),
    REPORTS("Reports", Icons.Default.Report, "Complaints & Moderation Tickets"),
    FINANCE("Finance", Icons.Default.AccountBalance, "Financial Ledger & Payouts"),
    ADMINISTRATORS("Administrators", Icons.Default.AdminPanelSettings, "Role-Based Access Control"),
    NOTIFICATIONS("Broadcasts", Icons.Default.Campaign, "Announcements & User Notices"),
    AUDIT_LOGS("Activity Logs", Icons.Default.History, "Immutable Audit Trail"),
    SETTINGS("Settings", Icons.Default.Tune, "Global System Configuration");
}
