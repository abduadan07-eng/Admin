package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.ui.components.AdminSectionHeader
import com.example.ui.theme.*

@Composable
fun FinanceScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Not-Configured Notice Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(WarningAmber.copy(alpha = 0.5f))
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(WarningAmber.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = WarningAmber,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Payment Gateway Not Configured",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = WarningAmber
                        )
                        Text(
                            text = "No active monetization system detected",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "In accordance with strict security and auditing standards, no simulated or fake financial transactions are displayed. The existing CV Maker application (com.cvmaker.app) currently does not have an active payment gateway (such as Google Play Billing or Stripe) provisioned in its Firebase backend.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimaryDark,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        AdminSectionHeader(
            title = "Monetization Architecture Roadmap",
            subtitle = "Requirements to activate financial ledger"
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FinanceRequirementItem(
                    step = "1",
                    title = "Google Play Billing / Payment Processor",
                    description = "Integrate Google Play In-App Purchases in the client CV Maker app for template purchases and premium exports."
                )
                FinanceRequirementItem(
                    step = "2",
                    title = "Cloud Functions Payment Webhook",
                    description = "Deploy server-side purchase verification to record transactions in the `financial_transactions` Firestore collection."
                )
                FinanceRequirementItem(
                    step = "3",
                    title = "Creator Commission Ledger",
                    description = "Automated split calculation (e.g. 70% Creator / 30% Platform) with balance tracking in `/creators/{id}/ledger`."
                )
                FinanceRequirementItem(
                    step = "4",
                    title = "Withdrawal & Payout Approval",
                    description = "Once configured, this Super Admin module will automatically display live transaction volume, pending payout requests, and revenue charts."
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun FinanceRequirementItem(
    step: String,
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(BlueAccent.copy(alpha = 0.2f))
                .border(1.dp, BlueAccent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = step, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BlueAccent)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark
            )
        }
    }
}
