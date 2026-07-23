package com.storytime.creators.features.commandcenter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storytime.creators.core.model.CommandCenterCalendarEvent
import com.storytime.creators.core.model.CreateCalendarEventBody
import com.storytime.creators.core.model.CreatorProject
import com.storytime.creators.core.theme.STColor
import com.storytime.creators.core.theme.stIcon
import com.storytime.creators.core.util.DateParser
import com.storytime.creators.ui.STTextField
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun eventIcon(kind: String?): String = when (kind) {
    "SHOOT_DAY" -> "video.fill"
    "CALL_SHEET" -> "doc.richtext.fill"
    "TABLE_READ" -> "book.fill"
    "INCIDENT", "INCIDENT_RESOLVED" -> "exclamationmark.triangle.fill"
    "PROJECT_TASK" -> "checklist"
    else -> "calendar"
}

@Composable
fun CalendarGrid(
    month: LocalDate,
    events: List<CommandCenterCalendarEvent>,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSelectEvent: (CommandCenterCalendarEvent) -> Unit,
) {
    var selectedDay by remember(month) { mutableStateOf<LocalDate?>(null) }
    val eventsByDay = remember(events) {
        events.groupBy { DateParser.localDate(it.startAt) }
    }
    val ym = YearMonth.of(month.year, month.month)
    val first = ym.atDay(1)
    val leading = (first.dayOfWeek.value % 7) // Sunday-first grid
    val daysInMonth = ym.lengthOfMonth()
    val today = LocalDate.now()

    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(STColor.surface).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(stIcon("chevron.left"), "Prev", tint = STColor.primary, modifier = Modifier.size(22.dp).clickable { onPrev() })
            Text(
                month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                color = STColor.textPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center, modifier = Modifier.weight(1f),
            )
            Icon(stIcon("chevron.right"), "Next", tint = STColor.primary, modifier = Modifier.size(22.dp).clickable { onNext() })
        }

        Row(Modifier.fillMaxWidth()) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                Text(it, color = STColor.textMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
        }

        val cells = leading + daysInMonth
        val rows = (cells + 6) / 7
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (r in 0 until rows) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (c in 0 until 7) {
                        val cellIndex = r * 7 + c
                        val dayNum = cellIndex - leading + 1
                        Box(Modifier.weight(1f)) {
                            if (dayNum in 1..daysInMonth) {
                                val date = ym.atDay(dayNum)
                                val dayEvents = eventsByDay[date].orEmpty()
                                val isToday = date == today
                                val isSelected = date == selectedDay
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            when {
                                                isSelected -> STColor.primary.copy(alpha = 0.18f)
                                                isToday -> STColor.surfaceElevated
                                                else -> Color.Transparent
                                            }
                                        )
                                        .clickable { selectedDay = date }
                                        .padding(vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        "$dayNum",
                                        color = if (isToday) STColor.accent else STColor.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        repeat(minOf(dayEvents.size, 3)) {
                                            Box(Modifier.size(4.dp).clip(CircleShape).background(STColor.primary))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        val visible = selectedDay?.let { eventsByDay[it] } ?: events
        if (visible.isNotEmpty()) {
            Text(
                if (selectedDay == null) "This month" else "Selected day",
                color = STColor.textMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
            )
            visible.take(8).forEach { event ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(STColor.surfaceElevated)
                        .clickable { onSelectEvent(event) }.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(stIcon(eventIcon(event.kind)), null, tint = STColor.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(event.title, color = STColor.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Text(
                            listOfNotNull(DateParser.display(event.startAt), event.projectTitle, event.assigneeName)
                                .filter { it.isNotEmpty() }.joinToString(" · "),
                            color = STColor.textSecondary, fontSize = 11.sp, maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CalendarEventEditor(
    event: CommandCenterCalendarEvent?,
    month: LocalDate,
    projects: List<CreatorProject>,
    onDismiss: () -> Unit,
    onSave: (CreateCalendarEventBody) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val isEditing = event != null
    var title by remember { mutableStateOf(event?.title ?: "") }
    var notes by remember { mutableStateOf(event?.description ?: "") }
    var allDay by remember { mutableStateOf(event?.allDay ?: true) }
    var visibility by remember { mutableStateOf(event?.visibility ?: "PERSONAL") }
    var projectId by remember { mutableStateOf(event?.projectId ?: "") }
    val defaultDate = DateParser.localDate(event?.startAt) ?: LocalDate.now()
    var startDate by remember { mutableStateOf(defaultDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    var projectMenu by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = STColor.surface,
        title = { Text(if (isEditing) "Edit event" else "New event", color = STColor.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                STTextField(title, { title = it }, "Title")
                STTextField(notes, { notes = it }, "Notes", singleLine = false)

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("All day", color = STColor.textPrimary, modifier = Modifier.weight(1f), fontSize = 14.sp)
                    Switch(checked = allDay, onCheckedChange = { allDay = it })
                }

                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(STColor.surfaceElevated)
                        .clickable { showDatePicker = true }.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(stIcon("calendar"), null, tint = STColor.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(startDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")), color = STColor.textPrimary, fontSize = 14.sp)
                }

                // Project dropdown
                Box {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(STColor.surfaceElevated)
                            .clickable { projectMenu = true }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            projects.firstOrNull { it.id == projectId }?.title ?: "No project",
                            color = STColor.textPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f),
                        )
                        Icon(stIcon("chevron.down"), null, tint = STColor.textMuted, modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = projectMenu, onDismissRequest = { projectMenu = false }) {
                        DropdownMenuItem(text = { Text("No project") }, onClick = { projectId = ""; projectMenu = false })
                        projects.forEach { p ->
                            DropdownMenuItem(text = { Text(p.title) }, onClick = { projectId = p.id; projectMenu = false })
                        }
                    }
                }

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Team visible", color = STColor.textPrimary, modifier = Modifier.weight(1f), fontSize = 14.sp)
                    Switch(checked = visibility == "TEAM", onCheckedChange = { visibility = if (it) "TEAM" else "PERSONAL" })
                }

                if (isEditing && onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text("Delete event", color = STColor.danger)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = {
                    val startInstant = startDate.atStartOfDay().toInstant(ZoneOffset.UTC)
                    onSave(
                        CreateCalendarEventBody(
                            title = title.trim(),
                            description = notes.ifBlank { null },
                            startAt = DateParser.isoInstant(startInstant),
                            endAt = null,
                            allDay = allDay,
                            visibility = visibility,
                            projectId = projectId.ifBlank { null },
                            assigneeId = null,
                        )
                    )
                },
            ) { Text(if (isEditing) "Save" else "Add", color = STColor.primary) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = STColor.textSecondary) } },
    )

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = startDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        startDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK", color = STColor.primary) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = state) }
    }
}
