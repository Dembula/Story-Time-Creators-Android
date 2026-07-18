package online.storytime.creators.features.originals

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.storytime.creators.core.Session
import online.storytime.creators.core.model.AppDestination
import online.storytime.creators.core.model.CompetitionStatsResponse
import online.storytime.creators.core.network.get
import online.storytime.creators.core.theme.STColor
import online.storytime.creators.core.theme.stIcon
import online.storytime.creators.ui.GradientButton
import online.storytime.creators.ui.SectionHeader
import online.storytime.creators.ui.StatTile

@Composable
fun OriginalsScreen() {
    val client = Session.api
    val router = Session.router
    var stats by remember { mutableStateOf<CompetitionStatsResponse?>(null) }

    LaunchedEffect(Unit) {
        stats = runCatching { client.get<CompetitionStatsResponse>("/api/competition/creator-stats") }.getOrNull()
    }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(STColor.surface).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(stIcon("star.circle.fill"), null, tint = STColor.accent, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(10.dp))
                Text("Story Time Originals", color = STColor.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                "Pitch bold new films and series for the Story Time Originals competition. The app keeps you connected to stats and projects — full submission flows run on the web.",
                color = STColor.textSecondary, fontSize = 14.sp,
            )
        }

        val s = stats
        if (s?.period != null) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader("Competition", s.period?.name ?: "Current period")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile("Your rank", s.rank?.let { "#$it" } ?: "—", "trophy.fill", Modifier.weight(1f))
                    StatTile("Votes", "${s.voteCount ?: 0}", "hand.thumbsup.fill", Modifier.weight(1f))
                }
                s.period?.endDate?.let { Text("Period ends $it", color = STColor.textMuted, fontSize = 12.sp) }
            }
        } else {
            Text(
                "Competition stats are not available right now. Submit your Originals pitch from the web studio to enter the current period.",
                color = STColor.textMuted, fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(STColor.surfaceElevated).padding(14.dp),
            )
        }

        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(STColor.surface).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionHeader("How to submit")
            InfoRow("star.circle.fill", "Story Time Originals", "Greenlit originals compete for platform support and audience votes. Build your project in My Projects and mark it as an Original when submitting.")
            InfoRow("globe", "Submit on the web", "Full Originals submission, legal review, and checkout live at story-time.online/creator/originals.")
            InfoRow("folder.fill", "Track in Projects", "Open a project from My Projects to continue development while your pitch is in review.")
        }

        GradientButton("Open My Projects", Modifier.fillMaxWidth()) { router.open(AppDestination.projects) }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun InfoRow(icon: String, title: String, detail: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(stIcon(icon), null, tint = STColor.primary, modifier = Modifier.size(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = STColor.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = STColor.textSecondary, fontSize = 13.sp)
        }
    }
}
