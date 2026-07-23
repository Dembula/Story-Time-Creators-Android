package com.storytime.creators.features.catalogue

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storytime.creators.core.Session
import com.storytime.creators.core.model.RevenueAPIResponse
import com.storytime.creators.core.network.get
import com.storytime.creators.core.theme.STColor
import com.storytime.creators.core.theme.stIcon
import com.storytime.creators.core.util.formatHours
import com.storytime.creators.core.util.formatPercent
import com.storytime.creators.core.util.formatZAR
import com.storytime.creators.ui.EmptyStateView
import com.storytime.creators.ui.KeyValueRow
import com.storytime.creators.ui.Loadable
import com.storytime.creators.ui.SectionHeader
import com.storytime.creators.ui.StatTile
import com.storytime.creators.ui.rememberLoadable

@Composable
fun RevenueScreen() {
    val client = Session.api
    val loader = rememberLoadable()

    Loadable(
        controller = loader,
        loader = { client.get<RevenueAPIResponse>("/api/creator/revenue", listOf("period" to "month")) },
    ) { payload ->
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SectionHeader("Revenue", "This month")

            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(STColor.heroGradient).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("This month's revenue", color = STColor.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text(formatZAR(payload.revenue), color = STColor.textPrimary, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Text("Based on your ${formatPercent(payload.share, 2)} share of platform watch time", color = STColor.textSecondary, fontSize = 12.sp)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("Watch-time share", formatPercent(payload.share, 2), "percent", Modifier.weight(1f))
                StatTile("Watch time", formatHours(payload.watchTime), "clock.fill", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("Total views", "${payload.totalViews ?: 0}", "eye.fill", Modifier.weight(1f))
                StatTile("Streams", "${payload.streamCount ?: 0}", "play.rectangle.fill", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("Wallet available", formatZAR(payload.walletAvailable), "wallet.pass.fill", Modifier.weight(1f))
                StatTile("Total earnings", formatZAR(payload.walletTotalEarnings), "chart.line.uptrend.xyaxis", Modifier.weight(1f))
            }

            SectionHeader("Breakdown")
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(STColor.surface).padding(14.dp)) {
                payload.perViewRand?.let { KeyValueRow("Per view", formatZAR(it)) }
                payload.perStreamRand?.let { KeyValueRow("Per stream", formatZAR(it)) }
                payload.projectedRevenue?.let { KeyValueRow("Projected", formatZAR(it)) }
            }

            payload.banking?.let { banking ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(STColor.surface).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(stIcon("building.columns.fill"), null, tint = STColor.primary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(banking.bankName ?: "Bank account", color = STColor.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("•••• ${banking.accountNumberLast4 ?: "****"} · ${banking.accountType ?: ""}", color = STColor.textMuted, fontSize = 11.sp)
                    }
                    Icon(
                        stIcon(if (banking.verified == true) "checkmark.seal.fill" else "info.circle.fill"), null,
                        tint = if (banking.verified == true) STColor.success else STColor.textMuted, modifier = Modifier.size(20.dp),
                    )
                }
            }

            val payouts = payload.payouts.orEmpty()
            if (payouts.isNotEmpty()) {
                SectionHeader("Recent Payouts", "${payouts.size}")
                payouts.forEach { p ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(STColor.surface).padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(formatZAR(p.amount), color = STColor.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            p.status?.let { Text(it, color = STColor.textMuted, fontSize = 12.sp) }
                        }
                        p.createdAt?.let { Text(it, color = STColor.textMuted, fontSize = 11.sp) }
                    }
                }
            } else {
                Text("No payouts yet. Earnings accrue to your wallet and are paid out per cycle.", color = STColor.textMuted, fontSize = 12.sp)
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}
