package com.storytime.creators.features.shell

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.storytime.creators.core.Session
import com.storytime.creators.core.model.AppDestination
import com.storytime.creators.core.theme.STColor
import com.storytime.creators.core.theme.stIcon
import com.storytime.creators.features.va.VAFloatingButton
import com.storytime.creators.features.va.VAPanel

@Composable
fun AppShell() {
    val router = Session.router
    val va = Session.va
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { va.bootstrap() }
    LaunchedEffect(router.destination, router.selectedProjectId) {
        va.setContext(router.destination.title, router.selectedProjectId)
    }

    Box(Modifier.fillMaxSize().background(STColor.background)) {
        Column(Modifier.fillMaxSize()) {
            TopBar(
                title = router.destination.title,
                onMenu = { router.toggleMenu() },
                onVA = { va.toggle() },
                vaAvailable = va.isAvailable,
            )
            Box(Modifier.weight(1f).fillMaxWidth()) {
                ContentRouter()
            }
            ToolReturnBar()
        }

        // Dim + side menu overlay
        AnimatedVisibility(visible = router.isSideMenuOpen, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable { router.closeMenu() },
            )
        }
        AnimatedVisibility(
            visible = router.isSideMenuOpen,
            enter = slideInHorizontally(initialOffsetX = { -it }),
            exit = slideOutHorizontally(targetOffsetX = { -it }),
        ) {
            SideMenu()
        }

        // Floating VA button
        if (!router.isSideMenuOpen && !va.isPanelOpen) {
            VAFloatingButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(20.dp),
            ) { va.toggle() }
        }

        // VA panel
        AnimatedVisibility(
            visible = va.isPanelOpen,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
        ) {
            VAPanel(onClose = { va.close() })
        }
    }
}

@Composable
private fun TopBar(title: String, onMenu: () -> Unit, onVA: () -> Unit, vaAvailable: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(STColor.background)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onMenu) {
            Icon(stIcon("line.3.horizontal"), "Menu", tint = STColor.textPrimary)
        }
        Text(
            title,
            color = STColor.textPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f).padding(start = 4.dp),
        )
        IconButton(onClick = onVA) {
            Icon(
                stIcon("sparkles"),
                "Assistant",
                tint = if (vaAvailable) STColor.primary else STColor.textMuted,
            )
        }
    }
}
