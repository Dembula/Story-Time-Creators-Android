package com.storytime.creators.features.marketplace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.storytime.creators.core.Session
import com.storytime.creators.core.model.CreateCrewRosterBody
import com.storytime.creators.core.model.CrewTeam
import com.storytime.creators.core.model.CrewTeamRequestBody
import com.storytime.creators.core.model.IdResponse
import com.storytime.creators.core.model.OkResponse
import com.storytime.creators.core.model.RosterContact
import com.storytime.creators.core.network.delete
import com.storytime.creators.core.network.get
import com.storytime.creators.core.network.post
import com.storytime.creators.ui.EmptyStateView
import com.storytime.creators.ui.STTextField
import com.storytime.creators.ui.SegmentedTabs

@Composable
fun CrewScreen() {
    val client = Session.api
    val router = Session.router
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(0) }
    var roster by remember { mutableStateOf<List<RosterContact>>(emptyList()) }
    var teams by remember { mutableStateOf<List<CrewTeam>>(emptyList()) }
    var banner by remember { mutableStateOf<String?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    var requestTeam by remember { mutableStateOf<CrewTeam?>(null) }

    suspend fun loadRoster() { runCatching { roster = client.get("/api/creator/crew-roster") }.onFailure { roster = emptyList() } }
    suspend fun loadTeams() { runCatching { teams = client.get("/api/crew-teams") }.onFailure { teams = emptyList() } }
    LaunchedEffect(Unit) { loadRoster(); loadTeams() }

    MarketplaceScaffold("Crew", "Build your crew roster and request crew teams — payments stay on web.", banner) {
        SegmentedTabs(listOf("Roster", "Teams"), tab) { tab = it }
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (tab == 0) {
                AddButton("Add crew member") { showAdd = true }
                if (roster.isEmpty()) EmptyStateView("wrench.and.screwdriver.fill", "No crew yet", "Add crew to your roster.")
                roster.forEach { c ->
                    RosterCard(c.name, listOfNotNull(c.role, c.department, c.email).joinToString(" · "), c.notes) {
                        scope.launch { runCatching { client.delete<OkResponse>("/api/creator/crew-roster/${c.id}") }; loadRoster(); banner = "Contact removed." }
                    }
                }
            } else {
                if (teams.isEmpty()) EmptyStateView("person.3.fill", "No crew teams", "Check back soon.")
                teams.forEach { t -> BrowseCard(t.name, t.location, t.description ?: t.specialty, "Request") { requestTeam = t } }
            }
            Spacer(Modifier.height(30.dp))
        }
    }

    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var role by remember { mutableStateOf("") }
        var dept by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        FormDialog("Add crew member", { showAdd = false }, name.isNotBlank(), onConfirm = {
            scope.launch {
                runCatching {
                    client.post<RosterContact, CreateCrewRosterBody>("/api/creator/crew-roster",
                        CreateCrewRosterBody(name = name.trim(), role = role.ifBlank { null }, department = dept.ifBlank { null }, contactEmail = email.ifBlank { null }))
                }
                loadRoster(); showAdd = false; banner = "Crew member added."
            }
        }) {
            STTextField(name, { name = it }, "Name")
            STTextField(role, { role = it }, "Role")
            STTextField(dept, { dept = it }, "Department")
            STTextField(email, { email = it }, "Contact email")
        }
    }

    requestTeam?.let { team ->
        var message by remember { mutableStateOf("") }
        FormDialog("Request — ${team.name}", { requestTeam = null }, message.isNotBlank(), "Send", onConfirm = {
            scope.launch {
                runCatching {
                    client.post<IdResponse, CrewTeamRequestBody>("/api/crew-teams/requests",
                        CrewTeamRequestBody(crewTeamId = team.id, message = message.trim(), projectId = router.selectedProjectId))
                }
                requestTeam = null; banner = "Request sent to ${team.name}."
            }
        }) {
            STTextField(message, { message = it }, "Message", singleLine = false)
        }
    }
}
