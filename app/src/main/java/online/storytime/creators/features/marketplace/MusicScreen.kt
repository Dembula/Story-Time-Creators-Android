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
import online.storytime.creators.core.model.MusicSelectionBody
import online.storytime.creators.core.model.MusicSelectionResponse
import online.storytime.creators.core.model.MusicTrack
import online.storytime.creators.core.network.get
import online.storytime.creators.core.network.post
import online.storytime.creators.core.util.formatTrackDuration
import online.storytime.creators.ui.EmptyStateView
import online.storytime.creators.ui.LoadingStateView
import online.storytime.creators.ui.STTextField

@Composable
fun MusicScreen() {
    val client = Session.api
    val router = Session.router
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var tracks by remember { mutableStateOf<List<MusicTrack>>(emptyList()) }
    var banner by remember { mutableStateOf<String?>(null) }
    var selecting by remember { mutableStateOf<MusicTrack?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        val loaded = runCatching { client.get<List<MusicTrack>>("/api/music/catalogue") }.getOrNull()
            ?: runCatching { client.get<List<MusicTrack>>("/api/music") }.getOrNull()
            ?: emptyList()
        tracks = loaded
        loading = false
    }

    MarketplaceScaffold("Music & Scoring", "Browse licensed tracks and attach them to your project.", banner) {
        if (loading) {
            LoadingStateView()
        } else {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (tracks.isEmpty()) EmptyStateView("music.note.list", "No tracks", "Check back soon.")
                tracks.forEach { t ->
                    val meta = listOfNotNull(t.genre, t.mood, t.duration?.let { formatTrackDuration(it) }).joinToString(" · ")
                    BrowseCard(t.title, t.artistName, listOfNotNull(t.description, meta).joinToString("\n"), "Add to project",
                        onAction = { selecting = t }, meta = t.licenseType)
                }
                Spacer(Modifier.height(30.dp))
            }
        }
    }

    selecting?.let { track ->
        var usage by remember { mutableStateOf("SCORE") }
        var notes by remember { mutableStateOf("") }
        if (router.selectedProjectId == null) {
            FormDialog("Select a project", { selecting = null }, false, onConfirm = {}) {
                androidx.compose.material3.Text("Open a project first to attach music.", color = online.storytime.creators.core.theme.STColor.textSecondary)
            }
        } else {
            FormDialog("Add — ${track.title}", { selecting = null }, true, "Add", onConfirm = {
                scope.launch {
                    runCatching {
                        client.post<MusicSelectionResponse, MusicSelectionBody>(
                            "/api/creator/projects/${router.selectedProjectId}/music-selection",
                            MusicSelectionBody(trackId = track.id, usage = usage.ifBlank { null }, notes = notes.ifBlank { null }),
                        )
                    }
                    selecting = null; banner = "Added \"${track.title}\" to project."
                }
            }) {
                STTextField(usage, { usage = it }, "Usage (e.g. SCORE, THEME)")
                STTextField(notes, { notes = it }, "Notes", singleLine = false)
            }
        }
    }
}
