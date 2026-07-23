package com.storytime.creators.features.marketplace

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.storytime.creators.core.Session
import com.storytime.creators.core.model.CastInquiryBody
import com.storytime.creators.core.model.CastingAgency
import com.storytime.creators.core.model.CreateCastRosterBody
import com.storytime.creators.core.model.IdResponse
import com.storytime.creators.core.model.OkResponse
import com.storytime.creators.core.model.RosterContact
import com.storytime.creators.core.network.delete
import com.storytime.creators.core.network.get
import com.storytime.creators.core.network.post
import com.storytime.creators.core.theme.STColor
import com.storytime.creators.core.theme.stIcon
import com.storytime.creators.ui.EmptyStateView
import com.storytime.creators.ui.STTextField
import com.storytime.creators.ui.SegmentedTabs

@Composable
fun CastingPortalScreen() {
    val client = Session.api
    val router = Session.router
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(0) }
    var roster by remember { mutableStateOf<List<RosterContact>>(emptyList()) }
    var agencies by remember { mutableStateOf<List<CastingAgency>>(emptyList()) }
    var banner by remember { mutableStateOf<String?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    var inquiryAgency by remember { mutableStateOf<CastingAgency?>(null) }

    suspend fun loadRoster() { runCatching { roster = client.get("/api/creator/cast-roster") }.onFailure { roster = emptyList() } }
    suspend fun loadAgencies() { runCatching { agencies = client.get("/api/casting-agencies") }.onFailure { agencies = emptyList() } }
    LaunchedEffect(Unit) { loadRoster(); loadAgencies() }

    MarketplaceScaffold(
        title = "Casting Portal",
        subtitle = "Manage your cast roster and inquire with agencies — payments stay on web.",
        banner = banner,
    ) {
        SegmentedTabs(listOf("Roster", "Agencies"), tab) { tab = it }
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (tab == 0) {
                AddButton("Add cast member") { showAdd = true }
                if (roster.isEmpty()) EmptyStateView("theatermasks.fill", "No cast yet", "Add talent to your project roster.")
                roster.forEach { c ->
                    RosterCard(c.name, listOfNotNull(c.role, c.email).joinToString(" · "), c.notes) {
                        scope.launch {
                            runCatching { client.delete<OkResponse>("/api/creator/cast-roster/${c.id}") }
                            loadRoster(); banner = "Contact removed."
                        }
                    }
                }
            } else {
                if (agencies.isEmpty()) EmptyStateView("building.2", "No agencies", "Check back soon.")
                agencies.forEach { a ->
                    BrowseCard(a.name, a.location, a.description, actionLabel = "Inquire") { inquiryAgency = a }
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }

    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var role by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        FormDialog(
            title = "Add cast member",
            onDismiss = { showAdd = false },
            confirmEnabled = name.isNotBlank(),
            onConfirm = {
                scope.launch {
                    runCatching {
                        client.post<RosterContact, CreateCastRosterBody>(
                            "/api/creator/cast-roster",
                            CreateCastRosterBody(name = name.trim(), roleType = role.ifBlank { null }, contactEmail = email.ifBlank { null }, notes = notes.ifBlank { null }),
                        )
                    }
                    loadRoster(); showAdd = false; banner = "Cast member added."
                }
            },
        ) {
            STTextField(name, { name = it }, "Name")
            STTextField(role, { role = it }, "Role type")
            STTextField(email, { email = it }, "Contact email")
            STTextField(notes, { notes = it }, "Notes", singleLine = false)
        }
    }

    inquiryAgency?.let { agency ->
        var roleName by remember { mutableStateOf("") }
        var message by remember { mutableStateOf("") }
        FormDialog(
            title = "Inquire — ${agency.name}",
            onDismiss = { inquiryAgency = null },
            confirmEnabled = message.isNotBlank(),
            confirmLabel = "Send",
            onConfirm = {
                scope.launch {
                    runCatching {
                        client.post<IdResponse, CastInquiryBody>(
                            "/api/casting-agencies/inquiries",
                            CastInquiryBody(agencyId = agency.id, roleName = roleName.ifBlank { null }, message = message.trim(), projectId = router.selectedProjectId),
                        )
                    }
                    inquiryAgency = null; banner = "Inquiry sent to ${agency.name}."
                }
            },
        ) {
            STTextField(roleName, { roleName = it }, "Role name")
            STTextField(message, { message = it }, "Message", singleLine = false)
        }
    }
}
