package online.storytime.creators.features.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import online.storytime.creators.core.Session
import online.storytime.creators.core.model.OkResponse
import online.storytime.creators.core.model.ProductionWorkspaceResponse
import online.storytime.creators.core.model.ProjectTool
import online.storytime.creators.core.model.ToolActivityRow
import online.storytime.creators.core.model.ToolProgressBody
import online.storytime.creators.core.network.get
import online.storytime.creators.core.network.patch
import online.storytime.creators.core.service.ToolReportBuilder
import online.storytime.creators.core.theme.STColor
import online.storytime.creators.core.theme.stIcon
import online.storytime.creators.ui.EmptyStateView
import online.storytime.creators.ui.ErrorStateView
import online.storytime.creators.ui.GradientButton
import online.storytime.creators.ui.LoadingStateView
import online.storytime.creators.ui.Pill
import online.storytime.creators.ui.SectionHeader
import online.storytime.creators.ui.StatTile

@Composable
fun ProjectToolDetailScreen(tool: ProjectTool, projectId: String?) {
    val client = Session.api
    val router = Session.router
    val scope = rememberCoroutineScope()

    var loading by remember(tool, projectId) { mutableStateOf(true) }
    var error by remember(tool, projectId) { mutableStateOf<String?>(null) }
    var rows by remember(tool, projectId) { mutableStateOf<List<ToolActivityRow>>(emptyList()) }
    var summary by remember(tool, projectId) { mutableStateOf("Connected to your project workspace.") }
    var marking by remember { mutableStateOf(false) }
    var progressMsg by remember { mutableStateOf<String?>(null) }

    suspend fun load() {
        if (projectId == null) { loading = false; error = "No project selected."; return }
        loading = true
        error = null
        val combined = mutableListOf<ToolActivityRow>()
        var toolHit = false
        runCatching {
            val body = client.raw("GET", "/api/creator/projects/$projectId/${tool.apiPathSegment}")
            combined += ToolReportBuilder.build(projectId, tool, body)
            toolHit = true
        }.onFailure { error = it.message }

        var openTasks = 0
        runCatching {
            val ws: ProductionWorkspaceResponse = client.get("/api/creator/projects/$projectId/production-workspace")
            combined += ToolReportBuilder.merge(ws.activityFeed ?: emptyList(), tool)
            openTasks = ws.taskSummary?.get("OPEN") ?: ws.taskSummary?.get("IN_PROGRESS") ?: 0
        }

        val deduped = combined.distinctBy { it.id }.sortedByDescending { it.timestamp ?: "" }
        summary = if (deduped.isEmpty()) {
            "No recorded updates yet for ${tool.label}. Work in the web studio or mark progress below to seed the timeline."
        } else {
            buildList {
                add("${deduped.size} update${if (deduped.size == 1) "" else "s"}")
                if (toolHit) add("synced from ${tool.label}")
                if (openTasks > 0) add("$openTasks open workspace tasks")
            }.joinToString(" · ")
        }
        rows = deduped
        if (deduped.isNotEmpty()) error = null
        loading = false
    }

    LaunchedEffect(tool, projectId) { load() }

    Column(Modifier.fillMaxSize()) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(Modifier.weight(1f).clickable { router.leaveToolDetail() }, verticalAlignment = Alignment.CenterVertically) {
                Icon(stIcon("chevron.left"), null, tint = STColor.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(router.toolReturnDestination?.title ?: "Back", color = STColor.primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(STColor.surfaceElevated).clickable { scope.launch { load() } },
                contentAlignment = Alignment.Center,
            ) { Icon(stIcon("arrow.clockwise"), "Refresh", tint = STColor.textSecondary, modifier = Modifier.size(18.dp)) }
        }

        when {
            loading && rows.isEmpty() -> LoadingStateView("Loading ${tool.label}…")
            error != null && rows.isEmpty() -> ErrorStateView(error!!) { scope.launch { load() } }
            else -> Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Hero
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(STColor.surface).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(46.dp).clip(RoundedCornerShape(13.dp)).background(STColor.brandGradient), contentAlignment = Alignment.Center) {
                            Icon(stIcon(tool.icon), null, tint = androidx.compose.ui.graphics.Color.Black, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(tool.label, color = STColor.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(tool.phase.title, color = STColor.accent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    Text(summary, color = STColor.textSecondary, fontSize = 14.sp)
                }

                // Metrics
                val actors = rows.mapNotNull { it.actorName }.filter { it.isNotEmpty() }.distinct().size
                val fresh = rows.mapNotNull { it.timestamp }.firstOrNull { it.isNotEmpty() } ?: "—"
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("Updates", "${rows.size}", "bolt.fill", Modifier.weight(1f))
                    StatTile("Actors", "$actors", "person.2.fill", Modifier.weight(1f))
                    StatTile("Fresh", fresh, "clock.fill", Modifier.weight(1f))
                }

                SectionHeader("Latest updates", "${rows.size}")
                if (rows.isEmpty()) {
                    EmptyStateView("sparkles", "No activity yet", "Edits from the web studio — versions, notes, and tasks — land here.")
                } else {
                    rows.forEach { ActivityRow(it) }
                }

                GradientButton(
                    label = if (marking) "Updating…" else "Mark in progress",
                    modifier = Modifier.fillMaxWidth(),
                    busy = marking,
                ) {
                    if (projectId == null) return@GradientButton
                    scope.launch {
                        marking = true
                        progressMsg = null
                        val result = runCatching {
                            client.patch<OkResponse, ToolProgressBody>(
                                "/api/creator/projects/$projectId/tools/progress",
                                ToolProgressBody(phase = tool.phase.raw, toolId = tool.raw, status = "IN_PROGRESS", percent = 50.0),
                            )
                        }
                        marking = false
                        progressMsg = if (result.isSuccess) "Marked ${tool.label} as in progress." else result.exceptionOrNull()?.message
                        if (result.isSuccess) load()
                    }
                }
                progressMsg?.let { Text(it, color = STColor.success, fontSize = 12.sp) }
                Spacer(Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun ActivityRow(row: ToolActivityRow) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(STColor.surface).padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(STColor.primary.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
            Icon(stIcon(row.icon), null, tint = STColor.accent, modifier = Modifier.size(18.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(row.title, color = STColor.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                row.kind?.takeIf { it.isNotEmpty() }?.let { Pill(it.replace("_", " ")) }
            }
            row.detail?.takeIf { it.isNotEmpty() }?.let { Text(it, color = STColor.textSecondary, fontSize = 13.sp, maxLines = 5) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.actorName?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(stIcon("person.fill"), null, tint = STColor.textMuted, modifier = Modifier.size(11.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(it, color = STColor.textMuted, fontSize = 10.sp)
                    }
                }
                row.timestamp?.takeIf { it.isNotEmpty() }?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(stIcon("clock"), null, tint = STColor.textMuted, modifier = Modifier.size(11.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(it, color = STColor.textMuted, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
