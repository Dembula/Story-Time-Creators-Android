package online.storytime.creators.features.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import online.storytime.creators.core.Session
import online.storytime.creators.core.model.AppDestination
import online.storytime.creators.core.theme.STColor
import online.storytime.creators.core.theme.stIcon

@Composable
fun SideMenu() {
    val router = Session.router
    val auth = Session.auth
    val scope = rememberCoroutineScope()
    val user = auth.currentUser

    Column(
        Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(STColor.surface)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(STColor.brandGradient),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    (user?.displayName?.firstOrNull() ?: 'C').uppercase(),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(user?.displayName ?: "Creator", color = STColor.textPrimary, fontWeight = FontWeight.Bold)
                user?.email?.let { Text(it, color = STColor.textSecondary, fontSize = 12.sp) }
            }
        }

        Spacer(Modifier.height(8.dp))
        MenuSection("Operating", AppDestination.operating, router.destination) { router.open(it) }
        MenuSection("Monetization", AppDestination.monetization, router.destination) { router.open(it) }
        MenuSection("Pipeline", AppDestination.pipeline, router.destination) { router.open(it) }
        MenuSection("Marketplace", listOf(
            AppDestination.cast, AppDestination.crew, AppDestination.locations,
            AppDestination.equipment, AppDestination.catering, AppDestination.music,
            AppDestination.legalInbox, AppDestination.originals,
        ), router.destination) { router.open(it) }

        Spacer(Modifier.height(20.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { scope.launch { auth.signOut() } }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(stIcon("rectangle.portrait.and.arrow.right"), null, tint = STColor.danger, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text("Sign Out", color = STColor.danger, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun MenuSection(
    title: String,
    items: List<AppDestination>,
    current: AppDestination,
    onSelect: (AppDestination) -> Unit,
) {
    Text(
        title.uppercase(),
        color = STColor.textMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(top = 14.dp, bottom = 6.dp),
    )
    items.forEach { dest ->
        val selected = dest == current
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected) STColor.primary.copy(alpha = 0.15f) else Color.Transparent)
                .clickable { onSelect(dest) }
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                stIcon(dest.icon),
                null,
                tint = if (selected) STColor.primary else STColor.textSecondary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(14.dp))
            Text(
                dest.title,
                color = if (selected) STColor.textPrimary else STColor.textSecondary,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 14.sp,
            )
        }
    }
}
