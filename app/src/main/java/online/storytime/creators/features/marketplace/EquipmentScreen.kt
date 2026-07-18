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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import online.storytime.creators.core.Session
import online.storytime.creators.core.model.EquipmentItem
import online.storytime.creators.core.model.EquipmentListResponse
import online.storytime.creators.core.model.EquipmentRequestBody
import online.storytime.creators.core.model.IdResponse
import online.storytime.creators.core.network.get
import online.storytime.creators.core.network.post
import online.storytime.creators.core.util.formatZAR
import online.storytime.creators.ui.EmptyStateView
import online.storytime.creators.ui.Loadable
import online.storytime.creators.ui.STTextField
import online.storytime.creators.ui.rememberLoadable

@Composable
fun EquipmentScreen() {
    val client = Session.api
    val router = Session.router
    val scope = rememberCoroutineScope()
    val loader = rememberLoadable()
    var banner by remember { mutableStateOf<String?>(null) }
    var request by remember { mutableStateOf<EquipmentItem?>(null) }

    MarketplaceScaffold("Equipment", "Browse gear and send rental requests — payments stay on web.", banner) {
        Loadable(controller = loader, loader = { client.get<EquipmentListResponse>("/api/equipment").all }) { equipment ->
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (equipment.isEmpty()) EmptyStateView("camera.fill", "No equipment", "Check back soon.")
                equipment.forEach { item ->
                    BrowseCard(item.name, listOfNotNull(item.category, item.location).joinToString(" · "), item.description, "Request",
                        onAction = { request = item }, meta = item.dailyRate?.let { formatZAR(it) })
                }
                Spacer(Modifier.height(30.dp))
            }
        }
    }

    request?.let { item ->
        var note by remember { mutableStateOf("") }
        FormDialog("Request — ${item.name}", { request = null }, note.isNotBlank(), "Send", onConfirm = {
            scope.launch {
                runCatching {
                    client.post<IdResponse, EquipmentRequestBody>("/api/equipment-requests",
                        EquipmentRequestBody(equipmentId = item.id, note = note.trim(), projectId = router.selectedProjectId))
                }
                request = null; banner = "Request sent for ${item.name}."
            }
        }) {
            STTextField(note, { note = it }, "Note", singleLine = false)
        }
    }
}
