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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.storytime.creators.core.Session
import com.storytime.creators.core.theme.STColor
import com.storytime.creators.core.theme.stIcon
import com.storytime.creators.features.billing.CreatorPlanStoreDialog
import com.storytime.creators.features.va.VAFloatingButton
import com.storytime.creators.features.va.VAPanel

@Composable
fun AppShell() {
    val router = Session.router
    val va = Session.va
    val auth = Session.auth

    var showPlanStore by remember { mutableStateOf(false) }
    var autoOpenedPlan by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        va.bootstrap()
        auth.refreshPackageGate()
    }
    LaunchedEffect(router.destination, router.selectedProjectId) {
        va.setContext(router.destination.title, router.selectedProjectId)
    }
    LaunchedEffect(auth.needsPlanSetup) {
        if (auth.needsPlanSetup && !autoOpenedPlan) {
            showPlanStore = true
            autoOpenedPlan = true
        }
        if (!auth.needsPlanSetup) {
            showPlanStore = false
        }
    }

    Box(Modifier.fillMaxSize().background(STColor.background)) {
        Column(Modifier.fillMaxSize()) {
            TopBar(
                title = router.destination.title,
                onMenu = { router.toggleMenu() },
                onVA = { va.toggle() },
                vaAvailable = va.isAvailable,
            )
            if (auth.needsPlanSetup) {
                PlanBanner { showPlanStore = true }
            }
            Box(Modifier.weight(1f).fillMaxWidth()) {
                ContentRouter()
            }
            ToolReturnBar()
        }

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

        if (!router.isSideMenuOpen && !va.isPanelOpen) {
            VAFloatingButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .windowInsetsPadding(WindowInsets.systemBars)
                    .padding(20.dp),
            ) { va.toggle() }
        }

        AnimatedVisibility(
            visible = va.isPanelOpen,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
        ) {
            VAPanel(onClose = { va.close() })
        }

        if (showPlanStore) {
            CreatorPlanStoreDialog(
                title = "Finish your creator plan",
                onDismiss = { showPlanStore = false },
                onCompleted = { showPlanStore = false },
            )
        }
    }
}

@Composable
private fun PlanBanner(onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(STColor.primary.copy(alpha = 0.18f))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(stIcon("exclamationmark.triangle.fill"), null, tint = STColor.primary, modifier = Modifier.size(18.dp))
        Column(Modifier.weight(1f)) {
            Text("Finish your creator plan", color = STColor.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text("Choose pay-per-film, catalogue unlimited, or full pipeline.", color = STColor.textSecondary, fontSize = 11.sp)
        }
        Icon(stIcon("chevron.right"), null, tint = STColor.primary, modifier = Modifier.size(16.dp))
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
