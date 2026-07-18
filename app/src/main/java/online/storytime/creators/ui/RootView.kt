package online.storytime.creators.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import online.storytime.creators.core.Session
import online.storytime.creators.core.theme.STColor
import online.storytime.creators.core.theme.stIcon
import online.storytime.creators.features.auth.SignInScreen
import online.storytime.creators.features.shell.AppShell

private enum class RootPhase { Splash, SignIn, Shell }

@Composable
fun RootView() {
    val auth = Session.auth
    var phase by remember { mutableStateOf(RootPhase.Splash) }

    LaunchedEffect(Unit) {
        auth.restoreSession()
        delay(1400)
        phase = if (auth.isAuthenticated) RootPhase.Shell else RootPhase.SignIn
    }

    LaunchedEffect(auth.isAuthenticated) {
        if (phase != RootPhase.Splash) {
            phase = if (auth.isAuthenticated) RootPhase.Shell else RootPhase.SignIn
        }
    }

    when (phase) {
        RootPhase.Splash -> SplashView()
        RootPhase.SignIn -> SignInScreen()
        RootPhase.Shell -> AppShell()
    }
}

@Composable
private fun SplashView() {
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (visible) 1f else 0.7f, tween(700), label = "scale")
    LaunchedEffect(Unit) { visible = true }

    Box(
        Modifier.fillMaxSize().background(STColor.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(
                Modifier
                    .size(120.dp)
                    .scale(scale)
                    .clip(RoundedCornerShape(28.dp))
                    .background(STColor.brandGradient),
                contentAlignment = Alignment.Center,
            ) {
                Icon(stIcon("sparkles"), null, tint = Color.Black, modifier = Modifier.size(64.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text("Story Time", color = STColor.textPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("CREATORS", color = STColor.primary, fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 6.sp)
        }
    }
}
