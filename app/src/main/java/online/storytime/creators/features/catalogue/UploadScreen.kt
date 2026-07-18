package online.storytime.creators.features.catalogue

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import online.storytime.creators.core.Session
import online.storytime.creators.core.model.CreateContentBody
import online.storytime.creators.core.model.CreatorContentItem
import online.storytime.creators.core.network.post
import online.storytime.creators.core.service.MediaUploadService
import online.storytime.creators.core.theme.STColor
import online.storytime.creators.core.theme.stIcon
import online.storytime.creators.ui.GradientButton
import online.storytime.creators.ui.STTextField
import online.storytime.creators.ui.SectionHeader
import java.util.UUID

private enum class Slot { poster, backdrop, video, trailer }

@Composable
fun UploadScreen() {
    val client = Session.api
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("FILM") }
    var typeMenu by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var genres by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var episodes by remember { mutableStateOf("") }

    var posterUrl by remember { mutableStateOf<String?>(null) }
    var videoUrl by remember { mutableStateOf<String?>(null) }
    var trailerUrl by remember { mutableStateOf<String?>(null) }

    var busySlot by remember { mutableStateOf<Slot?>(null) }
    var submitting by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var savedContentId by remember { mutableStateOf<String?>(null) }

    val types = listOf("FILM", "SHORT", "SERIES", "DOCUMENTARY", "MUSIC_VIDEO")
    val isLongForm = type == "SERIES"

    suspend fun uploadUri(uri: Uri, slot: Slot) {
        busySlot = slot
        status = null
        try {
            val bytes = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            } ?: run { status = "Could not read file."; return }
            val isImage = slot == Slot.poster || slot == Slot.backdrop
            val contentType = if (isImage) "image/jpeg" else "video/mp4"
            val ext = if (isImage) "jpg" else "mp4"
            val fileName = "${slot.name}-${UUID.randomUUID()}.$ext"
            val result = MediaUploadService.upload(client, bytes, fileName, contentType)
            when (slot) {
                Slot.poster -> posterUrl = result.resolvedURL
                Slot.video -> videoUrl = result.resolvedURL
                Slot.trailer -> trailerUrl = result.resolvedURL
                Slot.backdrop -> {}
            }
            status = "${slot.name.replaceFirstChar { it.uppercase() }} uploaded."
        } catch (e: Exception) {
            status = e.message ?: "Upload failed."
        } finally {
            busySlot = null
        }
    }

    val posterPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { scope.launch { uploadUri(it, Slot.poster) } }
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { scope.launch { uploadUri(it, Slot.video) } }
    }
    val trailerPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { scope.launch { uploadUri(it, Slot.trailer) } }
    }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionHeader("Catalogue Upload", "Draft")
        Text(
            "Upload assets and save a draft to your catalogue. Review submission and checkout stay on the web studio.",
            color = STColor.textSecondary, fontSize = 13.sp,
        )

        STTextField(title, { title = it }, "Title")

        Box {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(STColor.surface).clickable { typeMenu = true }.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Type: $type", color = STColor.textPrimary, modifier = Modifier.weight(1f), fontSize = 14.sp)
                Icon(stIcon("chevron.down"), null, tint = STColor.textMuted, modifier = Modifier.size(18.dp))
            }
            DropdownMenu(expanded = typeMenu, onDismissRequest = { typeMenu = false }) {
                types.forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { type = t; typeMenu = false }) }
            }
        }

        STTextField(description, { description = it }, "Description / logline", singleLine = false)
        STTextField(genres, { genres = it }, "Genres (comma-separated)")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            STTextField(language, { language = it }, "Language", modifier = Modifier.weight(1f))
            STTextField(country, { country = it }, "Country", modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            STTextField(year, { year = it }, "Year", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
            STTextField(duration, { duration = it }, "Duration (min)", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number)
        }
        if (isLongForm) {
            STTextField(episodes, { episodes = it }, "Episodes", keyboardType = KeyboardType.Number)
        }

        SectionHeader("Assets")
        AssetRow("Poster image", posterUrl, busySlot == Slot.poster) { posterPicker.launch("image/*") }
        if (!isLongForm) {
            AssetRow("Main video", videoUrl, busySlot == Slot.video) { videoPicker.launch("video/*") }
        }
        AssetRow("Trailer", trailerUrl, busySlot == Slot.trailer) { trailerPicker.launch("video/*") }

        status?.let { Text(it, color = STColor.accent, fontSize = 13.sp) }

        GradientButton(
            label = "Save draft",
            modifier = Modifier.fillMaxWidth(),
            enabled = title.isNotBlank(),
            busy = submitting,
        ) {
            scope.launch {
                submitting = true
                status = null
                val body = CreateContentBody(
                    contentId = savedContentId,
                    title = title.trim(),
                    type = type,
                    description = description.ifBlank { null },
                    posterUrl = posterUrl,
                    videoUrl = if (isLongForm) null else videoUrl,
                    trailerUrl = trailerUrl,
                    category = genres.split(",").map { it.trim() }.filter { it.isNotEmpty() }.sorted().joinToString(", ").ifBlank { null },
                    language = language.ifBlank { null },
                    country = country.ifBlank { null },
                    year = year.toIntOrNull(),
                    duration = duration.toIntOrNull(),
                    episodes = if (isLongForm) episodes.toIntOrNull() else null,
                    reviewStatus = "DRAFT",
                )
                val result = runCatching { client.post<CreatorContentItem, CreateContentBody>("/api/creator/content", body) }
                submitting = false
                if (result.isSuccess) {
                    savedContentId = result.getOrNull()?.id
                    status = "Draft saved to My Catalogue."
                } else {
                    status = result.exceptionOrNull()?.message ?: "Save failed."
                }
            }
        }
        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun AssetRow(label: String, url: String?, busy: Boolean, onPick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(STColor.surface).clickable(enabled = !busy) { onPick() }.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            stIcon(if (url != null) "checkmark.circle.fill" else "arrow.up.doc.fill"), null,
            tint = if (url != null) STColor.success else STColor.primary, modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = STColor.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(if (url != null) "Uploaded" else "Tap to select", color = STColor.textMuted, fontSize = 12.sp)
        }
        if (busy) CircularProgressIndicator(color = STColor.primary, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
    }
}
