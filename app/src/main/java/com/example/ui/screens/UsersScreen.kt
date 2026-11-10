package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.model.UserProfile
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusPill
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    users: List<UserProfile>,
    onUpdateUserStatus: (userId: String, newStatus: String, reason: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") }
    var userToModerate by remember { mutableStateOf<UserProfile?>(null) }
    var actionType by remember { mutableStateOf("suspended") }
    var moderationReason by remember { mutableStateOf("") }

    val filteredUsers = remember(users, searchQuery, selectedFilter) {
        users.filter { user ->
            val matchesSearch = user.email.contains(searchQuery, ignoreCase = true) ||
                    user.effectiveDisplayName.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "active" -> user.status == "active"
                "suspended" -> user.status == "suspended"
                "banned" -> user.status == "banned"
                "creators" -> user.isCreator
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("user_search_input"),
            placeholder = { Text("Search users by name or email...") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondaryDark)
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = NavySurface,
                unfocusedContainerColor = NavySurface,
                focusedBorderColor = BlueAccent,
                unfocusedBorderColor = NavyBorder
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val filters = listOf(
                "all" to "All (${users.size})",
                "active" to "Active (${users.count { it.status == "active" }})",
                "suspended" to "Suspended (${users.count { it.status == "suspended" }})",
                "banned" to "Banned (${users.count { it.status == "banned" }})",
                "creators" to "Creators (${users.count { it.isCreator }})"
            )
            items(filters) { (key, label) ->
                FilterChip(
                    selected = selectedFilter == key,
                    onClick = { selectedFilter = key },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BlueAccent.copy(alpha = 0.2f),
                        selectedLabelColor = BlueAccent
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // User List
        if (filteredUsers.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.PeopleOutline,
                title = "No Users Found",
                description = if (searchQuery.isNotBlank()) "No users matching '$searchQuery'" else "Registered users from CV Maker will populate here automatically."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredUsers, key = { it.id }) { user ->
                    UserCard(
                        user = user,
                        onModerate = { targetAction ->
                            userToModerate = user
                            actionType = targetAction
                            moderationReason = ""
                        }
                    )
                }
            }
        }
    }

    // Moderation Dialog
    if (userToModerate != null) {
        val targetUser = userToModerate!!
        val isActivating = actionType == "active"

        AlertDialog(
            onDismissRequest = { userToModerate = null },
            icon = {
                Icon(
                    imageVector = if (isActivating) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isActivating) SuccessEmerald else DangerRose
                )
            },
            title = {
                Text(
                    text = if (isActivating) "Reactivate User Account" else "${actionType.replaceFirstChar { it.uppercase() }} User Account",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "User: ${targetUser.email}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isActivating) "Reactivating will restore the user's ability to access the CV Maker application and author resumes."
                        else "Please specify the administrative reason for this moderation action. This reason is saved in the audit log.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = moderationReason,
                        onValueChange = { moderationReason = it },
                        label = { Text("Reason for moderation") },
                        placeholder = { Text("e.g. Terms violation, spam content, resolved dispute") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("moderation_reason_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reason = moderationReason.ifBlank { "Administrative decision" }
                        onUpdateUserStatus(targetUser.id, actionType, reason)
                        userToModerate = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActivating) SuccessEmerald else DangerRose
                    ),
                    modifier = Modifier.testTag("confirm_moderation_button")
                ) {
                    Text(if (isActivating) "Confirm Reactivate" else "Confirm $actionType")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToModerate = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun UserCard(
    user: UserProfile,
    onModerate: (action: String) -> Unit
) {
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
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(BlueAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (user.effectiveDisplayName.firstOrNull() ?: user.email.firstOrNull() ?: 'U').uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = BlueAccent,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.effectiveDisplayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (user.isCreator) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GoldAccent.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Creator", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                                }
                            }
                        }
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark
                        )
                    }
                }

                StatusPill(status = user.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(NavySurfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "CVs Authored: ${user.cvCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimaryDark
                )
                Text(
                    text = "ID: ${user.id.take(8)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMutedDark
                )
            }

            if (user.suspensionReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Moderation Note: ${user.suspensionReason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = DangerRose,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (user.status == "active") {
                    FilledTonalButton(
                        onClick = { onModerate("suspended") },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = WarningAmber.copy(alpha = 0.15f),
                            contentColor = WarningAmber
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Suspend", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = { onModerate("banned") },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = DangerRose.copy(alpha = 0.15f),
                            contentColor = DangerRose
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Ban", fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = { onModerate("active") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SuccessEmerald,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Reactivate", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
