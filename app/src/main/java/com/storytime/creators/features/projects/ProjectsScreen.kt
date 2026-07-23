package com.storytime.creators.features.projects

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.storytime.creators.core.Session
import com.storytime.creators.core.model.CreateProjectBody
import com.storytime.creators.core.model.CreateProjectResponse
import com.storytime.creators.core.model.CreatorProject
import com.storytime.creators.core.model.ProjectPhaseResolver
import com.storytime.creators.core.model.ProjectTool
import com.storytime.creators.core.model.ProjectsResponse
import com.storytime.creators.core.network.get
import com.storytime.creators.core.network.post
import com.storytime.creators.core.theme.STColor
import com.storytime.creators.core.theme.stIcon
import com.storytime.creators.ui.EmptyStateView
import com.storytime.creators.ui.Loadable
import com.storytime.creators.ui.LoadableController
import com.storytime.creators.ui.Pill
import com.storytime.creators.ui.STTextField
import com.storytime.creators.ui.SectionHeader
import com.storytime.creators.ui.StatTile
import com.storytime.creators.ui.ToolCard
import com.storytime.creators.ui.rememberLoadable

@Composable
fun ProjectsScreen() {
    val client = Session.api
    val router = Session.router
    val loader = rememberLoadable()
    var showCreate by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Loadable(
        controller = loader,
        key = router.selectedProjectId,
        loader = { client.get<ProjectsResponse>("/api/creator/projects").projects },
    ) { projects ->
        val selectedId = router.selectedProjectId
        val selected = projects.firstOrNull { it.id == selectedId }
        if (selected != null) {
            ProjectWorkspace(
                project = selected,
                onOpenTool = { router.openTool(it, selected.id) },
                onBack = { router.selectedProjectId = null; router.selectedTool = null },
            )
        } else {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionHeader("My Projects", "${projects.size}", modifier = Modifier.weight(1f))
                    Row(
                        Modifier.clip(RoundedCornerShape(10.dp)).clickable { showCreate = true }.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(stIcon("plus.circle.fill"), null, tint = STColor.primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("New", color = STColor.primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
                if (projects.isEmpty()) {
                    EmptyStateView("folder.badge.plus", "No projects yet", "Start a new film or series to unlock the pipeline.")
                } else {
                    projects.forEach { ProjectRow(it) { router.openProject(it.id) } }
                }
            }
        }
    }

    if (showCreate) {
        CreateProjectDialog(
            onDismiss = { showCreate = false },
            onCreate = { title, logline, type ->
                scope.launch {
                    val created = runCatching {
                        client.post<CreateProjectResponse, CreateProjectBody>(
                            "/api/creator/projects",
                            CreateProjectBody(title = title, logline = logline, type = type),
                        ).project
                    }.getOrNull()
                    showCreate = false
                    if (created != null) router.openProject(created.id) else loader.reload()
                }
            },
        )
    }
}

@Composable
private fun ProjectRow(project: CreatorProject, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(STColor.surface).clickable { onClick() }.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(project.title, color = STColor.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Spacer(Modifier.height(6.dp))
                Pill(project.phaseLabel)
                project.logline?.takeIf { it.isNotEmpty() }?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, color = STColor.textSecondary, fontSize = 12.sp, maxLines = 2)
                }
            }
            project.pipelineRollup?.let { rollup ->
                Column(horizontalAlignment = Alignment.End) {
                    Text("${(rollup.overallPercent ?: 0.0).toInt()}%", color = STColor.accent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    if (rollup.completedTools != null && rollup.totalTools != null) {
                        Text("${rollup.completedTools}/${rollup.totalTools} done", color = STColor.textMuted, fontSize = 10.sp)
                    }
                }
            }
            Icon(stIcon("chevron.right"), null, tint = STColor.textMuted, modifier = Modifier.size(16.dp))
        }
        project.pipelineRollup?.let { rollup ->
            ProgressBar((rollup.overallPercent ?: 0.0))
            if (rollup.inProgress > 0) {
                Text("${rollup.inProgress} tool${if (rollup.inProgress == 1) "" else "s"} in progress", color = STColor.primary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ProgressBar(percent: Double) {
    Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(STColor.surfaceElevated)) {
        Box(
            Modifier.fillMaxWidth((percent.coerceIn(0.0, 100.0) / 100.0).toFloat()).height(6.dp)
                .clip(RoundedCornerShape(3.dp)).background(STColor.brandGradient),
        )
    }
}

@Composable
private fun ProjectWorkspace(project: CreatorProject, onOpenTool: (ProjectTool) -> Unit, onBack: () -> Unit) {
    val phase = ProjectPhaseResolver.resolve(project)
    val tools = ProjectTool.tools(phase)
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(
            Modifier.clickable { onBack() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(stIcon("chevron.left"), null, tint = STColor.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Projects", color = STColor.primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }

        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(STColor.surface).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(project.title, color = STColor.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(project.phaseLabel, color = STColor.accent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            project.logline?.takeIf { it.isNotEmpty() }?.let {
                Text(it, color = STColor.textSecondary, fontSize = 14.sp)
            }
        }

        project.pipelineRollup?.let { rollup ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("Pipeline", "${(rollup.overallPercent ?: 0.0).toInt()}%", "chart.pie.fill", Modifier.weight(1f))
                StatTile("Tools Done", "${rollup.completedTools ?: 0}/${rollup.totalTools ?: 0}", "checkmark.circle.fill", Modifier.weight(1f))
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            project.type?.let { StatTile("Type", it, "film.fill", Modifier.weight(1f)) }
            project.genre?.let { StatTile("Genre", it, "sparkles", Modifier.weight(1f)) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            project.ideasCount?.let { StatTile("Ideas", "$it", "lightbulb.fill", Modifier.weight(1f)) }
            project.budget?.takeIf { it > 0 }?.let { StatTile("Budget", "R%.0f".format(it), "dollarsign.circle.fill", Modifier.weight(1f)) }
        }

        SectionHeader("${phase.title} Tools", "${tools.size}")
        tools.forEach { tool ->
            val progress = project.projectToolProgress?.firstOrNull { it.toolId == tool.raw }
            val subtitle = when {
                progress == null -> "Not started"
                progress.percent != null -> "${(progress.status ?: "In progress").replace("_", " ")} · ${progress.percent.toInt()}%"
                else -> (progress.status ?: "In progress").replace("_", " ")
            }
            ToolCard(tool.label, subtitle, tool.icon, { onOpenTool(tool) })
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun CreateProjectDialog(onDismiss: () -> Unit, onCreate: (String, String?, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var logline by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("FILM") }
    var typeMenu by remember { mutableStateOf(false) }
    val types = listOf("FILM", "SHORT", "SERIES", "DOCUMENTARY", "MUSIC_VIDEO")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = STColor.surface,
        title = { Text("New Project", color = STColor.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                STTextField(title, { title = it }, "Title")
                STTextField(logline, { logline = it }, "Logline", singleLine = false)
                Box {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(STColor.surfaceElevated)
                            .clickable { typeMenu = true }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(type, color = STColor.textPrimary, modifier = Modifier.weight(1f), fontSize = 14.sp)
                        Icon(stIcon("chevron.down"), null, tint = STColor.textMuted, modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = typeMenu, onDismissRequest = { typeMenu = false }) {
                        types.forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { type = t; typeMenu = false }) }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(enabled = title.isNotBlank(), onClick = { onCreate(title.trim(), logline.ifBlank { null }, type) }) {
                Text("Create", color = STColor.primary)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = STColor.textSecondary) } },
    )
}
