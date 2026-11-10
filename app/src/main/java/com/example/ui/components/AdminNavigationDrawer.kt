package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AdminRole
import com.example.data.model.Administrator
import com.example.ui.navigation.AdminNavDestination
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.DangerRose
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavyDark

@Composable
fun AdminNavigationDrawerContent(
    currentDestination: AdminNavDestination,
    currentAdmin: Administrator?,
    onSelectDestination: (AdminNavDestination) -> Unit,
    onSignOut: () -> Unit,
    pendingCreatorsCount: Int = 0,
    pendingTemplatesCount: Int = 0,
    pendingReportsCount: Int = 0,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier
            .width(320.dp)
            .fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        // Drawer Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyDark)
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BlueAccent.copy(alpha = 0.2f))
                        .border(1.dp, BlueAccent, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "CV Maker",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "SUPER ADMIN CONSOLE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = BlueAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Admin Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = currentAdmin?.displayName?.ifBlank { "Administrator" } ?: "Administrator",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = currentAdmin?.email ?: "yahy38989@gmail.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RoleBadge(role = currentAdmin?.adminRole ?: AdminRole.SUPER_ADMIN)
                }
            }
        }

        HorizontalDivider(color = NavyBorder, thickness = 1.dp)

        // Destinations List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(AdminNavDestination.entries) { destination ->
                val isSelected = currentDestination == destination
                val badgeCount = when (destination) {
                    AdminNavDestination.CREATORS -> pendingCreatorsCount
                    AdminNavDestination.TEMPLATES -> pendingTemplatesCount
                    AdminNavDestination.REPORTS -> pendingReportsCount
                    else -> 0
                }

                val isSuperAdminOnly = destination == AdminNavDestination.ADMINISTRATORS
                val hasAccess = !isSuperAdminOnly || (currentAdmin?.adminRole == AdminRole.SUPER_ADMIN)

                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = destination.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp
                            )
                            if (badgeCount > 0) {
                                Badge(
                                    containerColor = if (destination == AdminNavDestination.REPORTS) DangerRose else BlueAccent,
                                    contentColor = Color.White
                                ) {
                                    Text(text = badgeCount.toString(), fontWeight = FontWeight.Bold)
                                }
                            } else if (!hasAccess) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Super Admin Only",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    selected = isSelected,
                    onClick = {
                        if (hasAccess) {
                            onSelectDestination(destination)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nav_item_${destination.name.lowercase()}"),
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        unselectedContainerColor = Color.Transparent,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        HorizontalDivider(color = NavyBorder, thickness = 1.dp)

        // Sign Out action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onSignOut,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DangerRose.copy(alpha = 0.15f),
                    contentColor = DangerRose
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sign_out_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Sign Out", fontWeight = FontWeight.Bold)
            }
        }
    }
}
