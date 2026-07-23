package com.storytime.creators.core.service

import com.storytime.creators.core.model.ProjectActivityItem
import com.storytime.creators.core.model.ProjectTool
import com.storytime.creators.core.model.ToolActivityRow
import com.storytime.creators.core.util.DateParser
import org.json.JSONArray
import org.json.JSONObject

/** Mirrors the iOS ToolReportBuilder — turns tool JSON into a unified activity timeline. */
object ToolReportBuilder {

    fun build(projectId: String, tool: ProjectTool, jsonString: String?): List<ToolActivityRow> {
        if (jsonString.isNullOrBlank()) return emptyList()
        val obj = runCatching { JSONObject(jsonString) }.getOrNull() ?: return emptyList()
        return when (tool) {
            ProjectTool.ideaDevelopment -> ideasRows(obj)
            ProjectTool.scriptWriting -> scriptRows(obj)
            ProjectTool.scriptReview -> scriptReviewRows(obj)
            ProjectTool.budgetBuilder, ProjectTool.expenseTracker -> expenseRows(obj)
            ProjectTool.productionScheduling, ProjectTool.callSheetGenerator -> scheduleRows(obj)
            ProjectTool.castingPortal -> castingRows(obj)
            ProjectTool.continuityManager -> continuityRows(obj)
            ProjectTool.incidentReporting -> incidentRows(obj)
            ProjectTool.tableReads -> tableReadRows(obj)
            ProjectTool.legalContracts -> contractRows(obj)
            ProjectTool.fundingHub -> fundingRows(obj)
            else -> genericRows(obj, tool)
        }
    }

    fun merge(activity: List<ProjectActivityItem>, tool: ProjectTool): List<ToolActivityRow> =
        activity.map { item ->
            ToolActivityRow(
                id = item.id,
                title = item.message ?: item.type ?: "Activity",
                detail = item.metadata,
                actorName = item.user?.name,
                timestamp = DateParser.display(item.createdAt),
                kind = item.type,
                icon = tool.icon,
            )
        }

    private fun JSONObject.strOrNull(key: String): String? =
        if (has(key) && !isNull(key)) optString(key).ifEmpty { null } else null

    private fun JSONObject.objOrNull(key: String): JSONObject? =
        if (has(key) && !isNull(key)) optJSONObject(key) else null

    private fun JSONArray.objectsWithId(): List<JSONObject> {
        val out = mutableListOf<JSONObject>()
        for (i in 0 until length()) optJSONObject(i)?.let { out.add(it) }
        return out
    }

    private fun ideasRows(obj: JSONObject): List<ToolActivityRow> {
        val ideas = obj.optJSONArray("ideas") ?: return emptyList()
        return ideas.objectsWithId().mapNotNull { idea ->
            val id = idea.strOrNull("id") ?: return@mapNotNull null
            ToolActivityRow(
                id = id,
                title = idea.strOrNull("title") ?: "Idea",
                detail = idea.strOrNull("logline") ?: idea.strOrNull("notes"),
                actorName = null,
                timestamp = DateParser.display(idea.strOrNull("updatedAt") ?: idea.strOrNull("createdAt")),
                kind = "IDEA",
                icon = "lightbulb.fill",
            )
        }
    }

    private fun scriptRows(obj: JSONObject): List<ToolActivityRow> {
        val script = obj.objOrNull("script") ?: return emptyList()
        val versions = script.optJSONArray("versions") ?: return emptyList()
        return versions.objectsWithId().mapNotNull { v ->
            val id = v.strOrNull("id") ?: return@mapNotNull null
            val label = v.strOrNull("versionLabel") ?: "Version"
            val preview = v.strOrNull("content")?.take(120)
            ToolActivityRow(
                id = id,
                title = "Script $label",
                detail = preview,
                actorName = v.strOrNull("createdById"),
                timestamp = DateParser.display(v.strOrNull("createdAt") ?: v.strOrNull("autoSavedAt")),
                kind = "SCRIPT_VERSION",
                icon = "doc.text.fill",
            )
        }
    }

    private fun scriptReviewRows(obj: JSONObject): List<ToolActivityRow> {
        val requests = obj.optJSONArray("requests") ?: return emptyList()
        return requests.objectsWithId().mapNotNull { req ->
            val id = req.strOrNull("id") ?: return@mapNotNull null
            ToolActivityRow(
                id = id,
                title = "Review request — ${req.strOrNull("status") ?: "pending"}",
                detail = req.objOrNull("scriptVersion")?.strOrNull("versionLabel"),
                actorName = req.objOrNull("requester")?.strOrNull("name"),
                timestamp = DateParser.display(req.strOrNull("createdAt")),
                kind = "SCRIPT_REVIEW",
                icon = "doc.text.magnifyingglass",
            )
        }
    }

    private fun expenseRows(obj: JSONObject): List<ToolActivityRow> {
        val expenses = obj.optJSONArray("expenses") ?: return emptyList()
        return expenses.objectsWithId().mapNotNull { e ->
            val id = e.strOrNull("id") ?: return@mapNotNull null
            val amount = if (e.has("amount") && !e.isNull("amount")) e.optDouble("amount") else null
            ToolActivityRow(
                id = id,
                title = e.strOrNull("description") ?: e.strOrNull("vendor") ?: "Expense",
                detail = amount?.let { "R%.2f".format(it) },
                actorName = e.objOrNull("createdBy")?.strOrNull("name"),
                timestamp = DateParser.display(e.strOrNull("createdAt") ?: e.strOrNull("spentAt")),
                kind = "EXPENSE",
                icon = "creditcard.fill",
            )
        }
    }

    private fun scheduleRows(obj: JSONObject): List<ToolActivityRow> {
        val days = obj.optJSONArray("shootDays") ?: obj.optJSONArray("callSheets") ?: return emptyList()
        return days.objectsWithId().mapNotNull { d ->
            val id = d.strOrNull("id") ?: return@mapNotNull null
            ToolActivityRow(
                id = id,
                title = d.strOrNull("title") ?: "Shoot day ${d.optInt("shootDayNumber", 0)}",
                detail = listOfNotNull(d.strOrNull("locationSummary"), d.strOrNull("callTime")).joinToString(" · "),
                actorName = null,
                timestamp = DateParser.display(d.strOrNull("date")),
                kind = "SCHEDULE",
                icon = "calendar",
            )
        }
    }

    private fun castingRows(obj: JSONObject): List<ToolActivityRow> {
        val roles = obj.optJSONArray("roles") ?: return emptyList()
        return roles.objectsWithId().mapNotNull { r ->
            val id = r.strOrNull("id") ?: return@mapNotNull null
            ToolActivityRow(
                id = id,
                title = r.strOrNull("name") ?: "Role",
                detail = listOfNotNull(r.strOrNull("status"), r.strOrNull("importance")).joinToString(" · "),
                actorName = r.strOrNull("assignedCast"),
                timestamp = null,
                kind = "CASTING",
                icon = "theatermasks.fill",
            )
        }
    }

    private fun continuityRows(obj: JSONObject): List<ToolActivityRow> {
        val notes = obj.optJSONArray("notes") ?: return emptyList()
        return notes.objectsWithId().mapNotNull { n ->
            val id = n.strOrNull("id") ?: return@mapNotNull null
            val scene = n.objOrNull("scene")
            val title = when {
                scene != null && scene.has("number") && scene.strOrNull("heading") != null ->
                    "Scene ${scene.optInt("number")}: ${scene.strOrNull("heading")}"
                scene?.strOrNull("heading") != null -> scene.strOrNull("heading")!!
                else -> "Continuity note"
            }
            ToolActivityRow(
                id = id,
                title = title,
                detail = n.strOrNull("body"),
                actorName = n.objOrNull("createdBy")?.strOrNull("name"),
                timestamp = DateParser.display(n.strOrNull("createdAt")),
                kind = "CONTINUITY",
                icon = "film.fill",
            )
        }
    }

    private fun incidentRows(obj: JSONObject): List<ToolActivityRow> {
        val incidents = obj.optJSONArray("incidents") ?: return emptyList()
        return incidents.objectsWithId().mapNotNull { i ->
            val id = i.strOrNull("id") ?: return@mapNotNull null
            ToolActivityRow(
                id = id,
                title = i.strOrNull("title") ?: "Incident",
                detail = i.strOrNull("description"),
                actorName = i.objOrNull("createdBy")?.strOrNull("name"),
                timestamp = DateParser.display(i.strOrNull("createdAt")),
                kind = "INCIDENT",
                icon = "exclamationmark.triangle.fill",
            )
        }
    }

    private fun tableReadRows(obj: JSONObject): List<ToolActivityRow> {
        val sessions = obj.optJSONArray("sessions") ?: return emptyList()
        return sessions.objectsWithId().mapNotNull { s ->
            val id = s.strOrNull("id") ?: return@mapNotNull null
            ToolActivityRow(
                id = id,
                title = s.strOrNull("title") ?: "Table read",
                detail = s.strOrNull("notes"),
                actorName = null,
                timestamp = DateParser.display(s.strOrNull("scheduledAt") ?: s.strOrNull("createdAt")),
                kind = "TABLE_READ",
                icon = "book.fill",
            )
        }
    }

    private fun contractRows(obj: JSONObject): List<ToolActivityRow> {
        val contracts = obj.optJSONArray("contracts") ?: return emptyList()
        return contracts.objectsWithId().mapNotNull { c ->
            val id = c.strOrNull("id") ?: return@mapNotNull null
            ToolActivityRow(
                id = id,
                title = c.strOrNull("subject") ?: c.strOrNull("type") ?: "Contract",
                detail = c.strOrNull("status"),
                actorName = c.objOrNull("createdBy")?.strOrNull("name"),
                timestamp = DateParser.display(c.strOrNull("createdAt")),
                kind = "CONTRACT",
                icon = "doc.text.fill",
            )
        }
    }

    private fun fundingRows(obj: JSONObject): List<ToolActivityRow> {
        val funding = obj.objOrNull("funding") ?: return emptyList()
        val id = funding.strOrNull("id") ?: return emptyList()
        return listOf(
            ToolActivityRow(
                id = id,
                title = "Funding — ${funding.strOrNull("status") ?: "status"}",
                detail = funding.strOrNull("option"),
                actorName = null,
                timestamp = DateParser.display(funding.strOrNull("updatedAt")),
                kind = "FUNDING",
                icon = "banknote.fill",
            )
        )
    }

    private fun genericRows(obj: JSONObject, tool: ProjectTool): List<ToolActivityRow> {
        val rows = mutableListOf<ToolActivityRow>()
        val keys = obj.keys().asSequence().toList().sorted()
        for (key in keys) {
            val array = obj.optJSONArray(key) ?: continue
            val items = array.objectsWithId()
            if (items.isEmpty() || !items.first().has("id")) continue
            for (item in items.take(20)) {
                val id = item.strOrNull("id") ?: continue
                rows.add(
                    ToolActivityRow(
                        id = "$key-$id",
                        title = item.strOrNull("title") ?: item.strOrNull("name") ?: key,
                        detail = item.strOrNull("description") ?: item.strOrNull("status"),
                        actorName = item.objOrNull("createdBy")?.strOrNull("name")
                            ?: item.objOrNull("user")?.strOrNull("name"),
                        timestamp = DateParser.display(item.strOrNull("createdAt") ?: item.strOrNull("updatedAt")),
                        kind = key.uppercase(),
                        icon = tool.icon,
                    )
                )
            }
        }
        return rows
    }
}
