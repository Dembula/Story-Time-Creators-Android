package online.storytime.creators.features.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.storytime.creators.core.theme.STColor
import online.storytime.creators.core.theme.stIcon
import online.storytime.creators.ui.Pill

@Composable
fun MarketplaceScaffold(
    title: String,
    subtitle: String,
    banner: String?,
    content: @Composable () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = STColor.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = STColor.textSecondary, fontSize = 13.sp)
            banner?.let {
                Text(
                    it, color = STColor.success, fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(STColor.success.copy(alpha = 0.12f)).padding(10.dp),
                )
            }
        }
        content()
    }
}

@Composable
fun AddButton(label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(STColor.primary.copy(alpha = 0.15f)).clickable { onClick() }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(stIcon("plus.circle.fill"), null, tint = STColor.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = STColor.primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
fun RosterCard(name: String, subtitle: String, notes: String?, onDelete: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(STColor.surface).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(name, color = STColor.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            if (subtitle.isNotEmpty()) Text(subtitle, color = STColor.textSecondary, fontSize = 12.sp)
            notes?.takeIf { it.isNotEmpty() }?.let { Text(it, color = STColor.textMuted, fontSize = 12.sp, maxLines = 2) }
        }
        Icon(stIcon("trash"), "Delete", tint = STColor.danger, modifier = Modifier.size(20.dp).clickable { onDelete() })
    }
}

@Composable
fun BrowseCard(name: String, location: String?, description: String?, actionLabel: String, meta: String? = null, onAction: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(STColor.surface).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(name, color = STColor.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                location?.takeIf { it.isNotEmpty() }?.let { Text(it, color = STColor.textMuted, fontSize = 12.sp) }
            }
            meta?.let { Pill(it) }
        }
        description?.takeIf { it.isNotEmpty() }?.let { Text(it, color = STColor.textSecondary, fontSize = 13.sp, maxLines = 3) }
        Box(
            Modifier.clip(RoundedCornerShape(20.dp)).background(STColor.primary).clickable { onAction() }.padding(horizontal = 18.dp, vertical = 8.dp),
        ) { Text(actionLabel, color = androidx.compose.ui.graphics.Color.Black, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
fun FormDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmEnabled: Boolean,
    confirmLabel: String = "Save",
    onConfirm: () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = STColor.surface,
        title = { Text(title, color = STColor.textPrimary) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { content() } },
        confirmButton = { TextButton(enabled = confirmEnabled, onClick = onConfirm) { Text(confirmLabel, color = STColor.primary) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = STColor.textSecondary) } },
    )
}
