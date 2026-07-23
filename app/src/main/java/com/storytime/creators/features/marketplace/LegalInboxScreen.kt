package com.storytime.creators.features.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storytime.creators.core.Session
import com.storytime.creators.core.model.LegalInboxItem
import com.storytime.creators.core.model.LegalInboxResponse
import com.storytime.creators.core.network.get
import com.storytime.creators.core.theme.STColor
import com.storytime.creators.core.util.DateParser
import com.storytime.creators.ui.EmptyStateView
import com.storytime.creators.ui.Loadable
import com.storytime.creators.ui.Pill
import com.storytime.creators.ui.SectionHeader
import com.storytime.creators.ui.rememberLoadable

@Composable
fun LegalInboxScreen() {
    val client = Session.api
    val loader = rememberLoadable()

    Loadable(controller = loader, loader = { client.get<LegalInboxResponse>("/api/creator/legal/inbox").buckets }) { buckets ->
        val waiting = buckets?.waitingForYou.orEmpty()
        val pending = buckets?.pending.orEmpty()
        val completed = buckets?.completed.orEmpty()
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader("Legal Inbox", "${waiting.size + pending.size + completed.size}")
            if (waiting.isEmpty() && pending.isEmpty() && completed.isEmpty()) {
                EmptyStateView("doc.text.fill", "Inbox empty", "Contracts and signature requests appear here.")
            }
            if (waiting.isNotEmpty()) {
                Text("Waiting for you", color = STColor.danger, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                waiting.forEach { LegalRow(it, STColor.danger) }
            }
            if (pending.isNotEmpty()) {
                Text("Pending", color = STColor.accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                pending.forEach { LegalRow(it, STColor.accent) }
            }
            if (completed.isNotEmpty()) {
                Text("Completed", color = STColor.success, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                completed.forEach { LegalRow(it, STColor.success) }
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun LegalRow(item: LegalInboxItem, accent: androidx.compose.ui.graphics.Color) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(STColor.surface).padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row {
            Column(Modifier.weight(1f)) {
                Text(item.title, color = STColor.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                if (item.projectTitle.isNotEmpty()) Text(item.projectTitle, color = STColor.textMuted, fontSize = 12.sp)
            }
            (item.statusLabel ?: item.status)?.let { Pill(it.replace("_", " "), accent) }
        }
        item.senderName?.let { Text("From $it", color = STColor.textSecondary, fontSize = 12.sp) }
        item.requiredAction?.let { Text(it, color = STColor.textSecondary, fontSize = 12.sp) }
        item.signatureDeadline?.let { Text("Deadline: ${DateParser.displayDate(it)}", color = STColor.textMuted, fontSize = 11.sp) }
    }
}
