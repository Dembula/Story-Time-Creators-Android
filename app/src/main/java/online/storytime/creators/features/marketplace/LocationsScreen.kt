package online.storytime.creators.features.marketplace

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
import online.storytime.creators.core.Session
import online.storytime.creators.core.model.IdResponse
import online.storytime.creators.core.model.LocationBookingBody
import online.storytime.creators.core.model.LocationListResponse
import online.storytime.creators.core.model.LocationListing
import online.storytime.creators.core.network.get
import online.storytime.creators.core.network.post
import online.storytime.creators.core.util.formatZAR
import online.storytime.creators.ui.EmptyStateView
import online.storytime.creators.ui.STTextField
import online.storytime.creators.ui.rememberLoadable
import online.storytime.creators.ui.Loadable

@Composable
fun LocationsScreen() {
    val client = Session.api
    val router = Session.router
    val scope = rememberCoroutineScope()
    val loader = rememberLoadable()
    var banner by remember { mutableStateOf<String?>(null) }
    var booking by remember { mutableStateOf<LocationListing?>(null) }

    MarketplaceScaffold("Locations", "Browse locations and send booking requests — payments stay on web.", banner) {
        Loadable(controller = loader, loader = { client.get<LocationListResponse>("/api/locations").all }) { locations ->
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (locations.isEmpty()) EmptyStateView("mappin.and.ellipse", "No locations", "Check back soon.")
                locations.forEach { loc ->
                    BrowseCard(loc.name, listOfNotNull(loc.type, loc.city).joinToString(" · "), loc.description, "Book",
                        onAction = { booking = loc }, meta = loc.dailyRate?.let { formatZAR(it) })
                }
                Spacer(Modifier.height(30.dp))
            }
        }
    }

    booking?.let { loc ->
        var note by remember { mutableStateOf("") }
        var shootType by remember { mutableStateOf("") }
        var crewSize by remember { mutableStateOf("") }
        FormDialog("Book — ${loc.name}", { booking = null }, note.isNotBlank(), "Send", onConfirm = {
            scope.launch {
                runCatching {
                    client.post<IdResponse, LocationBookingBody>("/api/location-bookings",
                        LocationBookingBody(locationId = loc.id, note = note.trim(), shootType = shootType.ifBlank { null }, crewSize = crewSize.toIntOrNull(), projectId = router.selectedProjectId))
                }
                booking = null; banner = "Booking request sent for ${loc.name}."
            }
        }) {
            STTextField(shootType, { shootType = it }, "Shoot type")
            STTextField(crewSize, { crewSize = it }, "Crew size", keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            STTextField(note, { note = it }, "Note", singleLine = false)
        }
    }
}
