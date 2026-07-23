package com.storytime.creators.features.catalogue

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.storytime.creators.core.Session
import com.storytime.creators.core.model.CreateContentBody
import com.storytime.creators.core.model.CreatorContentItem
import com.storytime.creators.core.network.post
import com.storytime.creators.core.service.MediaUploadService
import com.storytime.creators.core.theme.STColor
import com.storytime.creators.core.theme.stIcon
import com.storytime.creators.ui.STTextField
import java.util.UUID

private enum class Slot { poster, backdrop, video, trailer, script }

@Composable
fun UploadScreen() {
    val client = Session.api
    val router = Session.router
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var step by remember { mutableStateOf(1) }
    var type by remember { mutableStateOf(CatalogueContentType.MOVIE) }
    var showMore by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var synopsis by remember { mutableStateOf("") }
    var logline by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    val selectedGenres = remember { mutableStateListOf<String>() }
    var genreQuery by remember { mutableStateOf("") }

    var language by remember { mutableStateOf("English") }
    var country by remember { mutableStateOf("South Africa") }
    var ageRating by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var episodes by remember { mutableStateOf("") }

    var posterUrl by remember { mutableStateOf<String?>(null) }
    var backdropUrl by remember { mutableStateOf<String?>(null) }
    var videoUrl by remember { mutableStateOf<String?>(null) }
    var trailerUrl by remember { mutableStateOf<String?>(null) }
    var scriptUrl by remember { mutableStateOf<String?>(null) }

    var busySlot by remember { mutableStateOf<Slot?>(null) }
    var submitting by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var succeeded by remember { mutableStateOf(false) }
    var savedContentId by remember { mutableStateOf<String?>(null) }

    val isLongForm = type.isLongForm

    suspend fun uploadUri(uri: Uri, slot: Slot) {
        busySlot = slot
        statusMessage = null
        try {
            val bytes = withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            } ?: run { statusMessage = "Could not read file."; succeeded = false; return }
            val (contentType, ext) = when (slot) {
                Slot.poster, Slot.backdrop -> "image/jpeg" to "jpg"
                Slot.script -> "application/pdf" to "pdf"
                else -> "video/mp4" to "mp4"
            }
            val fileName = "${slot.name}-${UUID.randomUUID()}.$ext"
            val result = MediaUploadService.upload(client, bytes, fileName, contentType)
            when (slot) {
                Slot.poster -> posterUrl = result.resolvedURL
                Slot.backdrop -> backdropUrl = result.resolvedURL
                Slot.video -> videoUrl = result.resolvedURL
                Slot.trailer -> trailerUrl = result.resolvedURL
                Slot.script -> scriptUrl = result.resolvedURL
            }
            statusMessage = "${slot.name.replaceFirstChar { it.uppercase() }} uploaded."
            succeeded = true
        } catch (e: Exception) {
            statusMessage = e.message ?: "Upload failed."
            succeeded = false
        } finally {
            busySlot = null
        }
    }

    fun picker(slot: Slot) = { uri: Uri? -> uri?.let { scope.launch { uploadUri(it, slot) } }; Unit }
    val posterPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent(), picker(Slot.poster))
    val backdropPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent(), picker(Slot.backdrop))
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent(), picker(Slot.video))
    val trailerPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent(), picker(Slot.trailer))
    val scriptPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent(), picker(Slot.script))

    val canAdvance = when (step) {
        2 -> title.trim().isNotEmpty()
        else -> true
    }

    fun submit() {
        scope.launch {
            submitting = true
            statusMessage = null
            val body = CreateContentBody(
                contentId = savedContentId,
                title = title.trim(),
                type = type.raw,
                description = synopsis.ifBlank { logline.ifBlank { null } },
                posterUrl = posterUrl,
                backdropUrl = backdropUrl,
                videoUrl = if (isLongForm) null else videoUrl,
                trailerUrl = trailerUrl,
                scriptUrl = scriptUrl,
                category = selectedGenres.sorted().joinToString(", ").ifBlank { null },
                tags = tags.ifBlank { null },
                language = language.ifBlank { null },
                country = country.ifBlank { null },
                ageRating = ageRating.ifBlank { null },
                year = year.toIntOrNull(),
                duration = duration.toIntOrNull(),
                episodes = if (isLongForm) episodes.toIntOrNull() else null,
                linkedProjectId = router.selectedProjectId,
                reviewStatus = "DRAFT",
            )
            val result = runCatching { client.post<CreatorContentItem, CreateContentBody>("/api/creator/content", body) }
            submitting = false
            if (result.isSuccess) {
                savedContentId = result.getOrNull()?.id
                succeeded = true
                statusMessage = "Draft saved to My Catalogue."
            } else {
                succeeded = false
                statusMessage = result.exceptionOrNull()?.message ?: "Save failed."
            }
        }
    }

    Column(Modifier.fillMaxWidth()) {
        // Step header
        Column(
            Modifier.fillMaxWidth().background(STColor.background).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Catalogue Upload", color = STColor.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (1..5).forEach { n ->
                    Box(
                        Modifier.weight(1f).height(4.dp).clip(CircleShape)
                            .background(if (n <= step) STColor.primary else STColor.border),
                    )
                }
            }
            Text(
                when (step) {
                    1 -> "1 · Content type"
                    2 -> "2 · Title & details"
                    3 -> "3 · Media & assets"
                    4 -> "4 · Metadata"
                    else -> "5 · Review & save draft"
                },
                color = STColor.textSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(STColor.border))

        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            NoPayBanner("Upload media and save drafts from your device. Review submission checkout stays on the web studio — no charges here.")

            when (step) {
                1 -> TypeStep(type, showMore, { type = it }, { showMore = it })
                2 -> DetailsStep(title, { title = it }, synopsis, { synopsis = it }, logline, { logline = it }, tags, { tags = it }, selectedGenres, genreQuery, { genreQuery = it })
                3 -> MediaStep(
                    isLongForm, posterUrl, backdropUrl, videoUrl, trailerUrl, scriptUrl, busySlot,
                    { posterPicker.launch("image/*") },
                    { backdropPicker.launch("image/*") },
                    { videoPicker.launch("video/*") },
                    { trailerPicker.launch("video/*") },
                    { scriptPicker.launch("application/pdf") },
                )
                4 -> MetadataStep(language, { language = it }, country, { country = it }, ageRating, { ageRating = it }, year, { year = it }, duration, { duration = it }, episodes, { episodes = it }, isLongForm)
                else -> ReviewStep(type, title, selectedGenres, language, country, ageRating, posterUrl, videoUrl, trailerUrl, scriptUrl, isLongForm)
            }

            statusMessage?.let { Text(it, color = if (succeeded) STColor.success else STColor.danger, fontSize = 13.sp) }
            if (busySlot != null) LinearProgressIndicator(color = STColor.primary, modifier = Modifier.fillMaxWidth())
        }

        // Nav bar
        Row(
            Modifier.fillMaxWidth().background(STColor.surface).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (step > 1) {
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                        .background(STColor.surfaceElevated).clickable { step -= 1 }.padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) { Text("Back", color = STColor.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp) }
            }
            val enabled = canAdvance && !submitting
            Box(
                Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                    .background(if (enabled) STColor.primary else STColor.textMuted)
                    .clickable(enabled = enabled) { if (step < 5) step += 1 else submit() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (submitting) CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    Text(
                        if (step < 5) "Continue" else if (submitting) "Saving…" else "Save draft",
                        color = Color.Black, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun NoPayBanner(text: String) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(STColor.primary.copy(alpha = 0.1f)).padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(stIcon("info.circle.fill"), null, tint = STColor.primary, modifier = Modifier.size(18.dp))
        Text(text, color = STColor.textSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun TypeStep(selected: CatalogueContentType, showMore: Boolean, onSelect: (CatalogueContentType) -> Unit, onToggleMore: (Boolean) -> Unit) {
    TypeGrid(CatalogueContentType.primary, selected, onSelect)
    if (showMore || selected in CatalogueContentType.more) {
        Text("More formats", color = STColor.textSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        TypeGrid(CatalogueContentType.more, selected, onSelect)
    }
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(STColor.surface).clickable { onToggleMore(!showMore) }.padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(stIcon(if (showMore) "chevron.up" else "chevron.down"), null, tint = STColor.primary, modifier = Modifier.size(16.dp))
            Text(
                if (showMore) "Hide extra formats" else "View more formats (${CatalogueContentType.more.size})",
                color = STColor.primary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun TypeGrid(types: List<CatalogueContentType>, selected: CatalogueContentType, onSelect: (CatalogueContentType) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        types.chunked(2).forEach { rowTypes ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowTypes.forEach { t ->
                    val on = t == selected
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(18.dp)).background(STColor.surface)
                            .clickable { onSelect(t) }.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                                .background(if (on) Color.White.copy(alpha = 0.9f) else STColor.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center,
                        ) { Icon(stIcon(t.icon), null, tint = if (on) Color.Black else STColor.primary, modifier = Modifier.size(22.dp)) }
                        Text(t.label, color = STColor.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(t.detail, color = STColor.textSecondary, fontSize = 11.sp, maxLines = 2)
                    }
                }
                if (rowTypes.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DetailsStep(
    title: String, onTitle: (String) -> Unit,
    synopsis: String, onSynopsis: (String) -> Unit,
    logline: String, onLogline: (String) -> Unit,
    tags: String, onTags: (String) -> Unit,
    selectedGenres: MutableList<String>,
    genreQuery: String, onGenreQuery: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        STTextField(title, onTitle, "Title")
        STTextField(synopsis, onSynopsis, "Synopsis", singleLine = false)
        STTextField(logline, onLogline, "Logline", singleLine = false)
        STTextField(tags, onTags, "Tags (comma separated)")
        Text("Genres", color = STColor.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        GenreSelect(selectedGenres, genreQuery, onGenreQuery)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GenreSelect(selected: MutableList<String>, query: String, onQuery: (String) -> Unit) {
    val filtered = remember(query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) CatalogueGenres.all else CatalogueGenres.all.filter { it.lowercase().contains(q) }
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (selected.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                selected.sorted().forEach { g ->
                    Row(
                        Modifier.clip(CircleShape).background(STColor.primary).clickable { selected.remove(g) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text(g, color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Icon(stIcon("xmark"), null, tint = Color.Black.copy(alpha = 0.7f), modifier = Modifier.size(9.dp))
                    }
                }
            }
        }
        STTextField(query, onQuery, "Search genres…")
        Box(Modifier.fillMaxWidth().heightIn(max = 240.dp).verticalScroll(rememberScrollState())) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filtered.forEach { g ->
                    val on = g in selected
                    Box(
                        Modifier.clip(CircleShape).background(if (on) STColor.primary else STColor.surfaceElevated)
                            .clickable { if (on) selected.remove(g) else selected.add(g) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) { Text(g, color = if (on) Color.Black else STColor.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                }
            }
        }
        Text("${filtered.size} shown · ${selected.size} selected", color = STColor.textMuted, fontSize = 10.sp)
    }
}

@Composable
private fun MediaStep(
    isLongForm: Boolean,
    posterUrl: String?, backdropUrl: String?, videoUrl: String?, trailerUrl: String?, scriptUrl: String?,
    busySlot: Slot?,
    onPoster: () -> Unit, onBackdrop: () -> Unit, onVideo: () -> Unit, onTrailer: () -> Unit, onScript: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            if (isLongForm) "Long-form titles use episode uploads on web. Add poster, trailer, and assets here for your draft."
            else "Upload the same asset slots as the web studio: main video, trailer, poster, backdrop, and script.",
            color = STColor.textSecondary, fontSize = 12.sp,
        )
        AssetRow("Poster", if (posterUrl != null) "Uploaded" else "Cover image", "photo", posterUrl != null, busySlot == Slot.poster, onPoster)
        AssetRow("Backdrop", if (backdropUrl != null) "Uploaded" else "Wide hero image", "rectangle.on.rectangle", backdropUrl != null, busySlot == Slot.backdrop, onBackdrop)
        if (!isLongForm) {
            AssetRow("Main video", if (videoUrl != null) "Uploaded" else "Feature film / short", "film", videoUrl != null, busySlot == Slot.video, onVideo)
        }
        AssetRow("Trailer", if (trailerUrl != null) "Uploaded" else "Optional promo cut", "play.rectangle", trailerUrl != null, busySlot == Slot.trailer, onTrailer)
        AssetRow("Script / document", if (scriptUrl != null) "Uploaded" else "PDF or text", "doc.richtext", scriptUrl != null, busySlot == Slot.script, onScript)
    }
}

@Composable
private fun AssetRow(title: String, subtitle: String, icon: String, done: Boolean, loading: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(STColor.surfaceElevated)
            .clickable(enabled = !loading) { onClick() }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(11.dp)).background(STColor.primary.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
            Icon(stIcon(icon), null, tint = if (done) STColor.success else STColor.primary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = STColor.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = STColor.textMuted, fontSize = 11.sp)
        }
        when {
            loading -> CircularProgressIndicator(color = STColor.primary, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            done -> Icon(stIcon("checkmark.circle.fill"), null, tint = STColor.success, modifier = Modifier.size(20.dp))
            else -> Icon(stIcon("plus.circle"), null, tint = STColor.textMuted, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun MetadataStep(
    language: String, onLanguage: (String) -> Unit,
    country: String, onCountry: (String) -> Unit,
    ageRating: String, onAgeRating: (String) -> Unit,
    year: String, onYear: (String) -> Unit,
    duration: String, onDuration: (String) -> Unit,
    episodes: String, onEpisodes: (String) -> Unit,
    isLongForm: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        DropdownField("Language", language, CatalogueGenres.languages, onLanguage)
        STTextField(country, onCountry, "Country")
        DropdownField("Age rating", ageRating.ifEmpty { "Select" }, CatalogueGenres.ageRatings, onAgeRating)
        STTextField(year, onYear, "Year", keyboardType = KeyboardType.Number)
        STTextField(duration, onDuration, "Duration (minutes)", keyboardType = KeyboardType.Number)
        if (isLongForm) STTextField(episodes, onEpisodes, "Episode count", keyboardType = KeyboardType.Number)
    }
}

@Composable
private fun DropdownField(label: String, value: String, options: List<String>, onSelect: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = STColor.textMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Box {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(STColor.surfaceElevated)
                    .clickable { open = true }.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(value, color = STColor.textPrimary, modifier = Modifier.weight(1f), fontSize = 14.sp)
                Icon(stIcon("chevron.down"), null, tint = STColor.textMuted, modifier = Modifier.size(18.dp))
            }
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                options.forEach { o -> DropdownMenuItem(text = { Text(o) }, onClick = { onSelect(o); open = false }) }
            }
        }
    }
}

@Composable
private fun ReviewStep(
    type: CatalogueContentType, title: String, genres: List<String>, language: String, country: String,
    ageRating: String, posterUrl: String?, videoUrl: String?, trailerUrl: String?, scriptUrl: String?, isLongForm: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ReviewRow("Type", type.label)
        ReviewRow("Title", title.ifEmpty { "—" })
        ReviewRow("Genres", if (genres.isEmpty()) "—" else genres.joinToString(", "))
        ReviewRow("Language", language)
        ReviewRow("Country", country)
        ReviewRow("Rating", ageRating.ifEmpty { "—" })
        ReviewRow("Poster", if (posterUrl == null) "Missing" else "Ready")
        ReviewRow("Main video", if (isLongForm) "Episodes on web" else if (videoUrl == null) "Optional for draft" else "Ready")
        ReviewRow("Trailer", if (trailerUrl == null) "—" else "Ready")
        ReviewRow("Script", if (scriptUrl == null) "—" else "Ready")
        Text(
            "Saves as DRAFT to My Catalogue — finish review & payment on the web studio when ready.",
            color = STColor.textSecondary, fontSize = 12.sp,
        )
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(label, color = STColor.textMuted, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(value, color = STColor.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

// MARK: - Content types (mirrors web content-types.ts / iOS CatalogueContentType)

private enum class CatalogueContentType(val raw: String, val label: String, val detail: String, val icon: String, val isLongForm: Boolean) {
    MOVIE("MOVIE", "Movie", "Feature film or theatrical-length title", "film", false),
    SERIES("SERIES", "Series", "Multi-episode scripted series", "tv", true),
    SHOW("SHOW", "Show", "Variety, talk, or entertainment show", "star.circle", true),
    DOCUMENTARY("DOCUMENTARY", "Documentary", "Feature or episodic documentary", "doc.richtext", false),
    SHORT_FILM("SHORT_FILM", "Short Film", "Short-form narrative or experimental", "film", false),
    PODCAST("PODCAST", "Podcast", "Audio or video podcast series", "mic.fill", true),
    COMEDY_SKIT("COMEDY_SKIT", "Comedy Skit", "Sketch comedy and short comedy bits", "face.smiling", false),
    STAND_UP("STAND_UP", "Stand-Up", "Stand-up specials and comedy sets", "face.smiling", false),
    ANIMATION("ANIMATION", "Animation", "Animated films, series, or shorts", "paintpalette.fill", false),
    SPORTS("SPORTS", "Sports", "Matches, highlights, and sports coverage", "sportscourt.fill", false),
    MUSIC_VIDEO("MUSIC_VIDEO", "Music Video", "Music videos and visual singles", "music.note.tv", false),
    LIVE_EVENT("LIVE_EVENT", "Live Event", "Concerts, festivals, and live captures", "theatermasks.fill", false),
    REALITY("REALITY", "Reality", "Reality and unscripted formats", "star.circle", true),
    WEB_SERIES("WEB_SERIES", "Web Series", "Episode-based web / digital series", "tv", true),
    NEWS("NEWS", "News", "News, current affairs, and reports", "newspaper.fill", true),
    EDUCATIONAL("EDUCATIONAL", "Educational", "Learning, tutorials, and explainers", "book.fill", false);

    companion object {
        val primary = listOf(MOVIE, SERIES, SHOW, DOCUMENTARY, SHORT_FILM, PODCAST)
        val more = listOf(COMEDY_SKIT, STAND_UP, ANIMATION, SPORTS, MUSIC_VIDEO, LIVE_EVENT, REALITY, WEB_SERIES, NEWS, EDUCATIONAL)
    }
}

private object CatalogueGenres {
    val languages = listOf(
        "English", "isiZulu", "isiXhosa", "Afrikaans", "Sesotho", "Setswana",
        "Sepedi", "Xitsonga", "siSwati", "Tshivenda", "isiNdebele",
        "French", "Portuguese", "Swahili", "Other",
    )
    val ageRatings = listOf("G", "PG", "PG-13", "16", "18", "R")
    val all = listOf(
        "Action", "Adventure", "Animation", "Anime", "Biography", "Comedy", "Crime",
        "Documentary", "Drama", "Family", "Fantasy", "History", "Horror", "Music",
        "Musical", "Mystery", "Romance", "Sci-Fi", "Sport", "Thriller", "War", "Western",
        "Dark Comedy", "Romantic Comedy", "Satire", "Parody", "Slapstick", "Coming-of-Age",
        "Slice of Life", "Anthology", "Experimental", "Art House", "Indie",
        "Short Form", "Feature", "Miniseries", "Limited Series", "Reality", "Unscripted",
        "Variety", "Talk Show", "Game Show", "Sketch Comedy", "Stand-Up", "Live Performance",
        "Concert Film", "Music Video", "Podcast", "Interview", "News / Current Affairs",
        "Action-Comedy", "Action-Thriller", "Psychological Thriller", "Political Thriller",
        "Legal Drama", "Medical Drama", "Police Procedural", "Detective", "Noir", "Neo-Noir",
        "Heist", "Spy / Espionage", "Survival", "Disaster", "Found Footage", "Supernatural",
        "Paranormal", "Ghost Story", "Vampire", "Zombie", "Monster", "Slasher", "Gothic",
        "Mythology", "Fairy Tale", "Epic Fantasy", "Urban Fantasy", "Space Opera",
        "Cyberpunk", "Steampunk", "Dystopian", "Post-Apocalyptic", "Time Travel",
        "Alternate History", "Superhero", "Martial Arts", "Nature", "Wildlife", "Travel",
        "Food & Culinary", "Lifestyle", "Fashion", "Art & Culture", "True Crime",
        "Investigative", "Social Issue", "Politics", "Education", "Faith & Spirituality",
        "Health & Wellness", "Mental Health", "LGBTQ+", "Women's Stories", "Youth / Teen",
        "Children", "Kids & Family", "Sports Drama", "Sports Documentary",
        "Football / Soccer", "Rugby", "Cricket", "Period Romance", "Contemporary Romance",
        "Love Story", "Melodrama", "Soap", "Telenovela", "Afro-Futurism", "African Cinema",
        "Nollywood", "South African", "Township Drama", "Township Comedy", "Kasi Story",
        "Oral Tradition / Folklore", "Indigenous Stories", "Pan-African", "Diaspora",
        "Historical Drama", "Biopic", "Memoir", "Feel-Good", "Inspirational", "Whodunnit",
        "Courtroom", "Workplace", "Campus / School", "Road Movie", "Buddy Film", "Ensemble",
        "Mockumentary", "Stop-Motion", "3D Animation", "2D Animation", "Mixed Media", "Other",
    )
}
