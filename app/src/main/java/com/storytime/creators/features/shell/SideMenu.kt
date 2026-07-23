package com.storytime.creators.features.shell

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.storytime.creators.R
import com.storytime.creators.core.Session
import com.storytime.creators.core.model.AppDestination
import com.storytime.creators.core.theme.STColor
import com.storytime.creators.core.theme.stIcon

@Composable
fun SideMenu() {
    val router = Session.router
    val auth = Session.auth
    val scope = rememberCoroutineScope()

    Column(
        Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(STColor.surface)
            .statusBarsPadding(),
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(18.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.st_logo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Story Time", color = STColor.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Creators", color = STColor.primary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(STColor.surfaceElevated).clickable { router.closeMenu() },
                contentAlignment = Alignment.Center,
            ) { Icon(stIcon("xmark"), "Close", tint = STColor.textSecondary, modifier = Modifier.size(13.dp)) }
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            MenuSection("Operating", AppDestination.operating, router.destination) { router.open(it) }
            MenuSection("Catalogue", AppDestination.monetization, router.destination) { router.open(it) }
            MenuRow(AppDestination.originals, router.destination == AppDestination.originals, highlight = true) { router.open(AppDestination.originals) }
            MenuSection("Pipeline", AppDestination.pipeline, router.destination) { router.open(it) }
            Spacer(Modifier.height(8.dp))
        }

        // Sign out
        Row(
            Modifier
                .fillMaxWidth()
                .background(STColor.surfaceElevated)
                .clickable { scope.launch { auth.signOut() } }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(stIcon("rectangle.portrait.and.arrow.right"), null, tint = STColor.textPrimary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text("Log out", color = STColor.textPrimary, fontWeight = FontWeight.Medium, fontSize = 15.sp)
        }
    }
}

@Composable
private fun MenuSection(
    title: String,
    items: List<AppDestination>,
    current: AppDestination,
    onSelect: (AppDestination) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            title.uppercase(),
            color = STColor.textMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
        items.forEach { dest -> MenuRow(dest, dest == current) { onSelect(dest) } }
    }
}

@Composable
private fun MenuRow(dest: AppDestination, active: Boolean, highlight: Boolean = false, onClick: () -> Unit) {
    val bg = when {
        active -> STColor.primary.copy(alpha = 0.14f)
        highlight -> STColor.primary.copy(alpha = 0.08f)
        else -> Color.Transparent
    }
    val tint = if (highlight || active) STColor.accent else STColor.textPrimary
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(stIcon(dest.icon), null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            dest.title,
            color = tint,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 15.sp,
        )
    }
}
