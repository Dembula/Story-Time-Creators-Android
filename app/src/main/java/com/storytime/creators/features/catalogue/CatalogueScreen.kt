package com.storytime.creators.features.catalogue

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.storytime.creators.core.Session
import com.storytime.creators.core.model.CreatorContentItem
import com.storytime.creators.core.network.get
import com.storytime.creators.core.theme.STColor
import com.storytime.creators.core.theme.stIcon
import com.storytime.creators.ui.EmptyStateView
import com.storytime.creators.ui.Loadable
import com.storytime.creators.ui.Pill
import com.storytime.creators.ui.SectionHeader
import com.storytime.creators.ui.StatTile
import com.storytime.creators.ui.rememberLoadable

@Composable
fun CatalogueScreen() {
    val client = Session.api
    val loader = rememberLoadable()
    var selected by remember { mutableStateOf<CreatorContentItem?>(null) }

    Loadable(
        controller = loader,
        loader = { client.get<List<CreatorContentItem>>("/api/creator/content") },
    ) { items ->
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SectionHeader("My Catalogue", "${items.size}")
            if (items.isEmpty()) {
                EmptyStateView("film.stack.fill", "No titles yet", "Upload a title from the Catalogue Upload screen.")
            } else {
                items.forEach { item -> CatalogueRow(item) { selected = item } }
            }
            Spacer(Modifier.height(30.dp))
        }
    }

    selected?.let { item -> ContentDetailDialog(item) { selected = null } }
}

@Composable
private fun CatalogueRow(item: CreatorContentItem, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(STColor.surface).clickable { onClick() }.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Poster(item.posterUrl, 56.dp, 72.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(item.title, color = STColor.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item.type?.let { Text(it.replace("_", " "), color = STColor.accent, fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                item.reviewStatus?.let { Text(it.replace("_", " "), color = STColor.textMuted, fontSize = 11.sp) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniStat("eye", "${item.count?.watchSessions ?: 0}")
                MiniStat("bubble.left", "${item.count?.comments ?: 0}")
                item.avgRating?.let { MiniStat("star.fill", "%.1f".format(it)) }
            }
        }
        Icon(stIcon("chevron.right"), null, tint = STColor.textMuted, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun MiniStat(icon: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(stIcon(icon), null, tint = STColor.textSecondary, modifier = Modifier.size(11.dp))
        Spacer(Modifier.width(3.dp))
        Text(value, color = STColor.textSecondary, fontSize = 10.sp)
    }
}

@Composable
fun Poster(url: String?, width: androidx.compose.ui.unit.Dp, height: androidx.compose.ui.unit.Dp) {
    Box(Modifier.size(width, height).clip(RoundedCornerShape(10.dp)).background(STColor.primary.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
        if (!url.isNullOrEmpty()) {
            AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(width, height))
        } else {
            Icon(stIcon("film"), null, tint = STColor.primary)
        }
    }
}

@Composable
private fun ContentDetailDialog(item: CreatorContentItem, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        containerColor = STColor.surface,
        confirmButton = { TextButton(onClick = onClose) { Text("Close", color = STColor.primary) } },
        title = { Text(item.title, color = STColor.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!item.posterUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = item.posterUrl, contentDescription = null, contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("Views", "${item.count?.watchSessions ?: 0}", "eye.fill", Modifier.weight(1f))
                    StatTile("Comments", "${item.count?.comments ?: 0}", "bubble.left.fill", Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("Ratings", "${item.count?.ratings ?: 0}", "star.fill", Modifier.weight(1f))
                    StatTile("Status", (item.reviewStatus ?: "DRAFT").replace("_", " "), "checkmark.seal.fill", Modifier.weight(1f))
                }
                item.description?.takeIf { it.isNotEmpty() }?.let { Text(it, color = STColor.textSecondary, fontSize = 14.sp) }
                (item.reviewFeedback ?: item.reviewNote)?.takeIf { it.isNotEmpty() }?.let { fb ->
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(STColor.surfaceElevated).padding(12.dp)) {
                        Text("Review feedback", color = STColor.textMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(fb, color = STColor.textSecondary, fontSize = 13.sp)
                    }
                }
            }
        },
    )
}
