package com.storytime.creators.features.pipeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storytime.creators.core.Session
import com.storytime.creators.core.model.CreatorProject
import com.storytime.creators.core.model.ProjectPhase
import com.storytime.creators.core.model.ProjectTool
import com.storytime.creators.core.model.ProjectsResponse
import com.storytime.creators.core.network.get
import com.storytime.creators.core.theme.STColor
import com.storytime.creators.core.theme.stIcon
import com.storytime.creators.ui.Loadable
import com.storytime.creators.ui.SectionHeader
import com.storytime.creators.ui.StatTile
import com.storytime.creators.ui.ToolCard
import com.storytime.creators.ui.rememberLoadable

@Composable
fun PhaseHubScreen(phase: ProjectPhase) {
    val client = Session.api
    val router = Session.router
    val loader = rememberLoadable()
    val tools = ProjectTool.hubTools(phase)
    val marketCount = tools.count { it.isMarketplaceStyle }
    val reportCount = tools.size - marketCount

    var selectedProjectId by remember(phase) { mutableStateOf(router.selectedProjectId) }
    var menu by remember { mutableStateOf(false) }

    Loadable(
        controller = loader,
        loader = { client.get<ProjectsResponse>("/api/creator/projects").projects },
    ) { projects ->
        if (selectedProjectId == null) selectedProjectId = router.selectedProjectId ?: projects.firstOrNull()?.id
        val selectedTitle = projects.firstOrNull { it.id == selectedProjectId }?.title ?: "Select a project"

        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // Hero
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(STColor.heroGradient).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(phase.title.uppercase(), color = STColor.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                Text(
                    when (phase) {
                        ProjectPhase.preProduction -> "Shape the story"
                        ProjectPhase.production -> "Run the shoot"
                        ProjectPhase.postProduction -> "Finish & deliver"
                    },
                    color = STColor.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                )
                Text(
                    when (phase) {
                        ProjectPhase.preProduction -> "Ideas, scripts, cast, crew, locations, and readiness — with live activity from your project."
                        ProjectPhase.production -> "Call sheets, continuity, dailies, expenses, and on-set ops in one place."
                        ProjectPhase.postProduction -> "Music, packaging, and distribution into your catalogue upload pipeline."
                    },
                    color = STColor.textSecondary, fontSize = 14.sp,
                )
            }

            // Project picker
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Active project", color = STColor.textMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Box {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(STColor.surface)
                            .clickable { menu = true }.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(stIcon("folder.fill"), null, tint = STColor.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(selectedTitle, color = STColor.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text(
                                if (selectedProjectId == null) "Required for activity reports" else "Reports & updates use this project",
                                color = STColor.textMuted, fontSize = 11.sp,
                            )
                        }
                        Icon(stIcon("chevron.up.chevron.down"), null, tint = STColor.textMuted, modifier = Modifier.size(14.dp))
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text("No project selected") }, onClick = {
                            selectedProjectId = null; router.selectedProjectId = null; menu = false
                        })
                        projects.forEach { p ->
                            DropdownMenuItem(text = { Text(p.title) }, onClick = {
                                selectedProjectId = p.id; router.selectedProjectId = p.id; menu = false
                            })
                        }
                    }
                }
            }

            // Insight strip
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("Project tools", "$reportCount", "chart.bar.doc.horizontal", Modifier.weight(1f))
                StatTile("Marketplaces", "$marketCount", "person.3.fill", Modifier.weight(1f))
                StatTile("Projects", "${projects.size}", "folder.fill", Modifier.weight(1f))
            }

            SectionHeader("Tools", "${tools.size}")
            if (Session.auth.needsPlanSetup) {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(STColor.primary.copy(alpha = 0.12f)).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("Pipeline access needs a creator plan", color = STColor.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        "Choose Full pipeline (monthly $19.99 or yearly $199.99) — or start with pay-per-film / catalogue unlimited. Server unlocks tools after purchase.",
                        color = STColor.textSecondary,
                        fontSize = 12.sp,
                    )
                }
            }
            tools.forEach { tool ->
                val needsProject = !tool.isMarketplaceStyle && selectedProjectId == null
                val subtitle = when {
                    tool.isMarketplaceStyle -> "Browse roster & inquire — payments stay on web"
                    selectedProjectId != null -> "Open live activity, versions, and workspace updates"
                    else -> "Select a project above to unlock this report"
                }
                Box(Modifier.alpha(if (needsProject) 0.45f else 1f)) {
                    ToolCard(tool.label, subtitle, tool.icon, {
                        if (needsProject) return@ToolCard
                        selectedProjectId?.let { router.selectedProjectId = it }
                        router.openTool(tool, selectedProjectId)
                    })
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}
