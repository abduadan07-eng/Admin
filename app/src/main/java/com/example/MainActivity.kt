package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.*
import com.example.data.repository.AdminRepository
import com.example.ui.auth.AuthUiState
import com.example.ui.auth.AuthViewModel
import com.example.ui.auth.SignInScreen
import com.example.ui.components.AdminNavigationDrawerContent
import com.example.ui.components.AdminTopBar
import com.example.ui.navigation.AdminNavDestination
import com.example.ui.screens.*
import com.example.ui.theme.CvAdminTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = AdminRepository(this)

        setContent {
            CvAdminTheme {
                val authViewModel: AuthViewModel = viewModel { AuthViewModel(repository) }
                val authState by authViewModel.uiState.collectAsStateWithLifecycle()

                when (val state = authState) {
                    is AuthUiState.Authenticated -> {
                        AdminApp(
                            currentAdmin = state.admin,
                            repository = repository,
                            onSignOut = { authViewModel.signOut(this) }
                        )
                    }
                    else -> {
                        SignInScreen(
                            uiState = state,
                            onSignInClicked = { authViewModel.signInWithGoogle(this) },
                            onSignOutClicked = { authViewModel.signOut(this) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminApp(
    currentAdmin: Administrator,
    repository: AdminRepository,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentDestination by remember { mutableStateOf(AdminNavDestination.DASHBOARD) }

    val users by repository.observeUsers().collectAsStateWithLifecycle(initialValue = emptyList())
    val templates by repository.observeTemplates().collectAsStateWithLifecycle(initialValue = emptyList())
    val creatorApplications by repository.observeCreatorApplications().collectAsStateWithLifecycle(initialValue = emptyList())
    val reports by repository.observeReports().collectAsStateWithLifecycle(initialValue = emptyList())
    val auditLogs by repository.observeAuditLogs().collectAsStateWithLifecycle(initialValue = emptyList())
    val appSettings by repository.observeAppSettings().collectAsStateWithLifecycle(initialValue = AppSettings())
    val notifications by repository.observeNotifications().collectAsStateWithLifecycle(initialValue = emptyList())
    val administrators by repository.observeAdministrators().collectAsStateWithLifecycle(initialValue = emptyList())

    val pendingCreatorsCount = creatorApplications.count { it.status == "pending" }
    val pendingTemplatesCount = templates.count { it.status == "pending" }
    val pendingReportsCount = reports.count { it.status == "pending" || it.status == "under_review" }

    BackHandler(enabled = drawerState.isOpen || currentDestination != AdminNavDestination.DASHBOARD) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            currentDestination = AdminNavDestination.DASHBOARD
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AdminNavigationDrawerContent(
                currentDestination = currentDestination,
                currentAdmin = currentAdmin,
                onSelectDestination = { dest ->
                    currentDestination = dest
                    scope.launch { drawerState.close() }
                },
                onSignOut = onSignOut,
                pendingCreatorsCount = pendingCreatorsCount,
                pendingTemplatesCount = pendingTemplatesCount,
                pendingReportsCount = pendingReportsCount
            )
        }
    ) {
        Scaffold(
            topBar = {
                AdminTopBar(
                    title = currentDestination.title,
                    currentAdmin = currentAdmin,
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = currentDestination, label = "screen_crossfade") { destination ->
                    when (destination) {
                        AdminNavDestination.DASHBOARD -> DashboardScreen(
                            users = users,
                            templates = templates,
                            creatorApplications = creatorApplications,
                            reports = reports,
                            auditLogs = auditLogs,
                            onNavigate = { currentDestination = it }
                        )

                        AdminNavDestination.USERS -> UsersScreen(
                            users = users,
                            onUpdateUserStatus = { userId, status, reason ->
                                scope.launch {
                                    repository.updateUserStatus(userId, status, reason)
                                }
                            }
                        )

                        AdminNavDestination.CREATORS -> CreatorsScreen(
                            applications = creatorApplications,
                            onReviewApplication = { appId, userId, approve, reason ->
                                scope.launch {
                                    repository.reviewCreatorApplication(appId, userId, approve, reason)
                                }
                            }
                        )

                        AdminNavDestination.TEMPLATES -> TemplatesScreen(
                            templates = templates,
                            onUpdateTemplateStatus = { tmplId, status, reason ->
                                scope.launch {
                                    repository.updateTemplateStatus(tmplId, status, reason)
                                }
                            }
                        )

                        AdminNavDestination.REPORTS -> ReportsScreen(
                            reports = reports,
                            onUpdateReportStatus = { reportId, status, notes ->
                                scope.launch {
                                    repository.updateReportStatus(reportId, status, notes)
                                }
                            }
                        )

                        AdminNavDestination.FINANCE -> FinanceScreen()

                        AdminNavDestination.ADMINISTRATORS -> AdministratorsScreen(
                            currentAdmin = currentAdmin,
                            administrators = administrators,
                            onCreateAdmin = { uid, email, name, role ->
                                scope.launch {
                                    repository.createAdministrator(uid, email, name, role)
                                }
                            },
                            onToggleAdminStatus = { adminId, isActive, reason ->
                                scope.launch {
                                    repository.updateAdministratorStatus(adminId, isActive, reason)
                                }
                            }
                        )

                        AdminNavDestination.NOTIFICATIONS -> NotificationsScreen(
                            notifications = notifications,
                            onSendNotification = { title, body, audience ->
                                scope.launch {
                                    repository.sendNotification(title, body, audience)
                                }
                            }
                        )

                        AdminNavDestination.AUDIT_LOGS -> ActivityLogsScreen(
                            auditLogs = auditLogs
                        )

                        AdminNavDestination.SETTINGS -> AppSettingsScreen(
                            currentAdmin = currentAdmin,
                            appSettings = appSettings,
                            onSaveSettings = { settings ->
                                scope.launch {
                                    repository.updateAppSettings(settings)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
