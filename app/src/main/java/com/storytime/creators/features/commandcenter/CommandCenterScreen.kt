package com.storytime.creators.features.commandcenter

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.storytime.creators.core.Session
import com.storytime.creators.core.model.CommandCenterAPIResponse
import com.storytime.creators.core.model.CommandCenterCalendarEvent
import com.storytime.creators.core.model.CommandCenterCalendarPayload
import com.storytime.creators.core.model.CommandCenterOverview
import com.storytime.creators.core.model.CreateCalendarEventBody
import com.storytime.creators.core.model.CreatorProject
import com.storytime.creators.core.model.IdResponse
import com.storytime.creators.core.model.OkResponse
import com.storytime.creators.core.model.ProjectsResponse
import com.storytime.creators.core.model.UpdateCalendarEventBody
import com.storytime.creators.core.network.delete
import com.storytime.creators.core.network.get
import com.storytime.creators.core.network.patch
import com.storytime.creators.core.network.post
import com.storytime.creators.core.theme.STColor
import com.storytime.creators.core.theme.stIcon
import com.storytime.creators.core.util.DateParser
import com.storytime.creators.core.util.formatDuration
import com.storytime.creators.core.util.formatPercent
import com.storytime.creators.core.util.formatZAR
import com.storytime.creators.ui.Loadable
import com.storytime.creators.ui.Pill
import com.storytime.creators.ui.SectionHeader
import com.storytime.creators.ui.StatTile
import com.storytime.creators.ui.rememberLoadable
import java.time.LocalDate

private data class CommandCenterData(
    val response: CommandCenterAPIResponse,
    val projects: List<CreatorProject>,
)

@Composable
fun CommandCenterScreen() {
    val client = Session.api
    val router = Session.router
    val loader = rememberLoadable()
    val scope = rememberCoroutineScope()

    var month by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    var events by remember { mutableStateOf<List<CommandCenterCalendarEvent>>(emptyList()) }
    var editing by remember { mutableStateOf<CommandCenterCalendarEvent?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var projectOptions by remember { mutableStateOf<List<CreatorProject>>(emptyList()) }

    suspend fun loadCalendar() {
        runCatching {
            val payload: CommandCenterCalendarPayload = client.get(
                "/api/creator/command-center/calendar",
                listOf("month" to DateParser.monthKey(month)),
            )
            events = payload.events ?: emptyList()
        }
    }

    Loadable(
        controller = loader,
        loader = {
            val response: CommandCenterAPIResponse =
                client.get("/api/creator/command-center", listOf("range" to "month"))
            val projs: ProjectsResponse = client.get("/api/creator/projects")
            projectOptions = projs.projects
            val payload: CommandCenterCalendarPayload = try {
                client.get("/api/creator/command-center/calendar", listOf("month" to DateParser.monthKey(month)))
            } catch (e: Exception) { CommandCenterCalendarPayload() }
            events = payload.events ?: emptyList()
            CommandCenterData(response, projs.projects)
        },
    ) { data ->
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            WelcomeHero()
            data.response.overview?.let { Spotlight(it) }
            ProjectsPreview(data.projects) { router.openProject(it) }
            data.response.analytics?.revenue?.let { RevenueBlock(it) }
            data.response.analytics?.engagement?.let { EngagementBlock(it) }
            data.response.production?.let { ProductionBlock(it) }

            val top = data.response.analytics?.contentPerformance.orEmpty()
            if (top.isNotEmpty()) TopContentBlock(top)

            // Calendar
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionHeader("Calendar", "${events.size} events", modifier = Modifier.weight(1f))
                    Icon(
                        stIcon("plus.circle.fill"), "Add", tint = STColor.primary,
                        modifier = Modifier.size(26.dp).clickable { editing = null; showEditor = true },
                    )
                }
                CalendarGrid(
                    month = month,
                    events = events,
                    onPrev = { month = month.minusMonths(1); scope.launch { loadCalendar() } },
                    onNext = { month = month.plusMonths(1); scope.launch { loadCalendar() } },
                    onSelectEvent = { if (it.editable == true) { editing = it; showEditor = true } },
                )
            }

            data.response.retention?.curve?.takeIf { it.isNotEmpty() }?.let { curve ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("Audience retention", "n=${data.response.retention?.sampleSize ?: 0}")
                    curve.forEach { p ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${p.checkpoint}%", color = STColor.textMuted, fontSize = 12.sp, modifier = Modifier.width(40.dp))
                            Box(
                                Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(STColor.surfaceElevated),
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxWidth((p.retainedPct / 100.0).toFloat().coerceIn(0.02f, 1f))
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(STColor.brandGradient),
                                )
                            }
                            Text("${p.retainedPct.toInt()}%", color = STColor.textSecondary, fontSize = 11.sp, modifier = Modifier.width(44.dp), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                        }
                    }
                }
            }

            data.response.ai?.let { ai ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader("MODOC activity")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatTile("Chats", "${ai.modocConversationsInRange ?: 0}", "bubble.left.and.bubble.right.fill", Modifier.weight(1f))
                        StatTile("Messages", "${ai.modocUserMessagesInRange ?: 0}", "text.bubble.fill", Modifier.weight(1f))
                    }
                    ai.topTasks?.take(3)?.forEach { t ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Text(t.task, color = STColor.textPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text("${t.count}", color = STColor.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(40.dp))
        }
    }

    if (showEditor) {
        CalendarEventEditor(
            event = editing,
            month = month,
            projects = projectOptions,
            onDismiss = { showEditor = false },
            onSave = { body ->
                scope.launch {
                    val ed = editing
                    runCatching {
                        if (ed == null) {
                            client.post<IdResponse, CreateCalendarEventBody>("/api/creator/command-center/calendar", body)
                        } else {
                            client.patch<OkResponse, UpdateCalendarEventBody>(
                                "/api/creator/command-center/calendar/${ed.id}",
                                UpdateCalendarEventBody(
                                    title = body.title, description = body.description,
                                    startAt = body.startAt, endAt = body.endAt, allDay = body.allDay,
                                    visibility = body.visibility, projectId = body.projectId,
                                ),
                            )
                        }
                    }
                    loadCalendar()
                    showEditor = false
                }
            },
            onDelete = editing?.let { ed ->
                {
                    scope.launch {
                        runCatching { client.delete<OkResponse>("/api/creator/command-center/calendar/${ed.id}") }
                        loadCalendar()
                        showEditor = false
                    }
                }
            },
        )
    }
}

@Composable
private fun WelcomeHero() {
    val user = Session.auth.currentUser
    val first = user?.displayName?.split(" ")?.firstOrNull() ?: "Creator"
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(STColor.heroGradient)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("COMMAND CENTER", color = STColor.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp)
        Text("Hey, $first", color = STColor.textPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(
            "Your production pulse, catalogue performance, and calendar — live from the studio.",
            color = STColor.textSecondary, fontSize = 14.sp,
        )
    }
}

@Composable
private fun Spotlight(o: CommandCenterOverview) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Today's snapshot")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("Active projects", "${o.activeProjects ?: 0}", "folder.fill", Modifier.weight(1f))
            StatTile("Views · 7d", "${o.viewsLast7d ?: 0}", "eye.fill", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("Growth", o.viewerGrowth7dPct?.let { formatPercent(it) } ?: "—", "chart.line.uptrend.xyaxis", Modifier.weight(1f))
            StatTile("Engagement", o.engagementRateApprox?.let { formatPercent(it) } ?: "—", "heart.fill", Modifier.weight(1f))
        }
        o.topFilmTitle?.let { title ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(STColor.surface).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(stIcon("star.circle.fill"), null, tint = STColor.accent, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Top performing title", color = STColor.textMuted, fontSize = 11.sp)
                    Text(title, color = STColor.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    o.topFilmViews?.let { Text("$it views", color = STColor.textSecondary, fontSize = 12.sp) }
                }
                o.topFilmRevenueRand?.let { Text(formatZAR(it), color = STColor.accent, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            }
        }
    }
}

@Composable
private fun ProjectsPreview(projects: List<CreatorProject>, onOpen: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("My projects", "${projects.size}")
        if (projects.isEmpty()) {
            Text("Create a project to unlock the pipeline.", color = STColor.textSecondary, fontSize = 13.sp)
        } else {
            projects.take(4).forEach { p ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(STColor.surface)
                        .clickable { onOpen(p.id) }.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(STColor.primary.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                        Icon(stIcon("film.stack.fill"), null, tint = STColor.primary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(p.title, color = STColor.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Spacer(Modifier.height(4.dp))
                        Pill(p.phaseLabel)
                    }
                    p.pipelineRollup?.overallPercent?.let {
                        Text("${it.toInt()}%", color = STColor.accent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun RevenueBlock(r: com.storytime.creators.core.model.AnalyticsRevenue) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Revenue & watch time")
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(STColor.heroGradient).padding(16.dp),
        ) {
            Text("Period revenue", color = STColor.textMuted, fontSize = 11.sp)
            Text(formatZAR(r.amount), color = STColor.textPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Share ${formatPercent(r.sharePercent)}", color = STColor.accent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("Streams", "${r.streamCount ?: 0}", "film.fill", Modifier.weight(1f))
            StatTile("Per view", formatZAR(r.perViewRand), "chart.bar.fill", Modifier.weight(1f))
            StatTile("Per stream", formatZAR(r.perStreamRand), "play.circle.fill", Modifier.weight(1f))
        }
    }
}

@Composable
private fun EngagementBlock(e: com.storytime.creators.core.model.AnalyticsEngagement) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Viewer engagement")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("Total views", "${e.totalViews ?: 0}", "eye.fill", Modifier.weight(1f))
            StatTile("Unique", "${e.uniqueWatchers ?: 0}", "person.2.fill", Modifier.weight(1f))
            StatTile("Avg watch", formatDuration(e.averageWatchTimeSeconds), "timer", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("Comments", "${e.totalComments ?: 0}", "bubble.left.fill", Modifier.weight(1f))
            StatTile("Ratings", "${e.totalRatings ?: 0}", "star.fill", Modifier.weight(1f))
            StatTile("Watchlist", "${e.watchlistCount ?: 0}", "bookmark.fill", Modifier.weight(1f))
        }
    }
}

@Composable
private fun ProductionBlock(p: com.storytime.creators.core.model.CommandCenterProduction) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Production pulse")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("Shoot days", "${p.shootDaysTotal ?: 0}", "video.fill", Modifier.weight(1f))
            StatTile("Incidents", "${p.openIncidents ?: 0}", "exclamationmark.triangle.fill", Modifier.weight(1f))
            StatTile("Call sheets", "${p.callSheetsSaved ?: 0}", "doc.richtext.fill", Modifier.weight(1f))
        }
    }
}

@Composable
private fun TopContentBlock(rows: List<com.storytime.creators.core.model.ContentPerformanceRow>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader("Top catalogue", "${minOf(rows.size, 5)}")
        rows.take(5).forEachIndexed { i, row ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(STColor.surface).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(28.dp).clip(CircleShape).background(if (i == 0) STColor.primary else STColor.primary.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                    Text("${i + 1}", color = if (i == 0) Color.Black else STColor.accent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(row.title, color = STColor.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(listOfNotNull(row.type, row.reviewStatus).joinToString(" · "), color = STColor.textMuted, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("${row.views ?: 0}", color = STColor.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(formatDuration(row.watchTimeSeconds), color = STColor.textMuted, fontSize = 10.sp)
                }
            }
        }
    }
}
