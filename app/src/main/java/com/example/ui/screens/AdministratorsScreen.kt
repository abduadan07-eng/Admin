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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminRole
import com.example.data.model.Administrator
import com.example.ui.components.AdminSectionHeader
import com.example.ui.components.EmptyStateView
import com.example.ui.components.RoleBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdministratorsScreen(
    currentAdmin: Administrator?,
    administrators: List<Administrator>,
    onCreateAdmin: (uid: String, email: String, name: String, role: AdminRole) -> Unit,
    onToggleAdminStatus: (adminId: String, isActive: Boolean, reason: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSuperAdmin = currentAdmin?.adminRole == AdminRole.SUPER_ADMIN
    var showCreateDialog by remember { mutableStateOf(false) }

    var newUid by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(AdminRole.USER_MANAGER) }
    var roleExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        AdminSectionHeader(
            title = "Role-Based Access Control",
            subtitle = "Active administrators and privilege tiers",
            actionButton = {
                if (isSuperAdmin) {
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimaryLight),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_admin_button")
                    ) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Admin", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )

        if (!isSuperAdmin) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = WarningAmber)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Viewing in read-only mode. Only the Super Admin (Owner) can appoint, alter, or revoke administrator roles.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimaryDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (administrators.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.AdminPanelSettings,
                title = "No Administrators Registered",
                description = "Administrator profiles provisioned in Firestore will appear here."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(administrators, key = { it.id }) { admin ->
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
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (admin.adminRole == AdminRole.SUPER_ADMIN) GoldAccent.copy(alpha = 0.2f)
                                                else BlueAccent.copy(alpha = 0.2f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = if (admin.adminRole == AdminRole.SUPER_ADMIN) GoldAccent else BlueAccent,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = admin.displayName.ifBlank { admin.email.substringBefore("@") },
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = admin.email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondaryDark
                                        )
                                    }
                                }

                                RoleBadge(role = admin.adminRole)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Privileges: ${admin.adminRole.description}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMutedDark
                            )

                            if (admin.assignedBy.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Provisioned by: ${admin.assignedBy}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMutedDark
                                )
                            }

                            if (isSuperAdmin && admin.id != currentAdmin?.id && admin.adminRole != AdminRole.SUPER_ADMIN) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (admin.isActive) {
                                        OutlinedButton(
                                            onClick = {
                                                onToggleAdminStatus(admin.id, false, "Deactivated by Super Admin")
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRose),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Deactivate Role", fontSize = 12.sp)
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                onToggleAdminStatus(admin.id, true, "Reactivated by Super Admin")
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Reactivate Role", fontSize = 12.sp)
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

    // Add Administrator Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Appoint Administrator", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Assign an operational role to an authorized staff member. All administrative activities are auditable.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("Administrator Google Email") },
                        placeholder = { Text("staff@example.com") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_admin_email_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Display Name") },
                        placeholder = { Text("e.g. Sarah Jenkins") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_admin_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = newUid,
                        onValueChange = { newUid = it },
                        label = { Text("User Firebase UID") },
                        placeholder = { Text("Google Auth UID (from user profile)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_admin_uid_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Role Picker Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            onClick = { roleExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Assigned Role", style = MaterialTheme.typography.labelSmall, color = TextMutedDark)
                                    Text(selectedRole.displayName, fontWeight = FontWeight.Bold, color = BlueAccent)
                                }
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = roleExpanded,
                            onDismissRequest = { roleExpanded = false }
                        ) {
                            // Only sub-roles (excluding self-appointing duplicate super admin)
                            val assignableRoles = listOf(
                                AdminRole.USER_MANAGER,
                                AdminRole.TEMPLATE_MODERATOR,
                                AdminRole.CREATOR_MANAGER,
                                AdminRole.SUPPORT_ADMIN,
                                AdminRole.FINANCE_MANAGER
                            )
                            assignableRoles.forEach { role ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(role.displayName, fontWeight = FontWeight.Bold)
                                            Text(role.description, fontSize = 11.sp, color = TextSecondaryDark)
                                        }
                                    },
                                    onClick = {
                                        selectedRole = role
                                        roleExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newEmail.isNotBlank()) {
                            val uid = newUid.ifBlank { "admin_" + newEmail.replace(".", "_").replace("@", "_") }
                            onCreateAdmin(uid, newEmail.trim(), newName.trim(), selectedRole)
                            showCreateDialog = false
                            newEmail = ""
                            newName = ""
                            newUid = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimaryLight),
                    modifier = Modifier.testTag("confirm_create_admin_button")
                ) {
                    Text("Provision Role")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
