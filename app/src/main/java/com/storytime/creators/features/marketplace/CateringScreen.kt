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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.storytime.creators.core.Session
import com.storytime.creators.core.model.CateringBookingBody
import com.storytime.creators.core.model.CateringCompany
import com.storytime.creators.core.model.CateringListResponse
import com.storytime.creators.core.model.IdResponse
import com.storytime.creators.core.network.get
import com.storytime.creators.core.network.post
import com.storytime.creators.ui.EmptyStateView
import com.storytime.creators.ui.Loadable
import com.storytime.creators.ui.STTextField
import com.storytime.creators.ui.rememberLoadable

@Composable
fun CateringScreen() {
    val client = Session.api
    val router = Session.router
    val scope = rememberCoroutineScope()
    val loader = rememberLoadable()
    var banner by remember { mutableStateOf<String?>(null) }
    var booking by remember { mutableStateOf<CateringCompany?>(null) }

    MarketplaceScaffold("Catering", "Browse caterers and send booking requests — payments stay on web.", banner) {
        Loadable(controller = loader, loader = { client.get<CateringListResponse>("/api/catering-companies").all }) { companies ->
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (companies.isEmpty()) EmptyStateView("fork.knife", "No caterers", "Check back soon.")
                companies.forEach { c ->
                    BrowseCard(c.name, listOfNotNull(c.cuisine, c.location).joinToString(" · "), c.description, "Book") { booking = c }
                }
                Spacer(Modifier.height(30.dp))
            }
        }
    }

    booking?.let { c ->
        var headCount by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }
        FormDialog("Book — ${c.name}", { booking = null }, note.isNotBlank(), "Send", onConfirm = {
            scope.launch {
                runCatching {
                    client.post<IdResponse, CateringBookingBody>("/api/catering-bookings",
                        CateringBookingBody(cateringCompanyId = c.id, headCount = headCount.toIntOrNull(), note = note.trim(), projectId = router.selectedProjectId))
                }
                booking = null; banner = "Booking request sent for ${c.name}."
            }
        }) {
            STTextField(headCount, { headCount = it }, "Head count", keyboardType = KeyboardType.Number)
            STTextField(note, { note = it }, "Note", singleLine = false)
        }
    }
}
