package com.storytime.creators.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.storytime.creators.R
import com.storytime.creators.core.Session
import com.storytime.creators.core.theme.STColor
import com.storytime.creators.features.auth.SignInScreen
import com.storytime.creators.features.shell.AppShell

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
    val scale by animateFloatAsState(if (visible) 1f else 0.88f, tween(700), label = "scale")
    val progress by animateFloatAsState(if (visible) 1f else 0f, tween(1800, delayMillis = 200), label = "progress")
    LaunchedEffect(Unit) { visible = true }

    val orange = Brush.verticalGradient(
        listOf(
            Color(0xFFFF9829),
            Color(0xFFFA700F),
            Color(0xFFEE4D0A),
        )
    )

    Box(
        Modifier.fillMaxSize().background(orange),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.st_logo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(168.dp)
                    .scale(scale)
                    .clip(RoundedCornerShape(36.dp)),
            )
            Spacer(Modifier.height(22.dp))
            Text(
                "STORY TIME",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Box(Modifier.width(34.dp).height(2.dp).background(Color.White.copy(alpha = 0.5f)))
            Spacer(Modifier.height(10.dp))
            Text(
                "CREATORS",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 5.sp,
                textAlign = TextAlign.Center,
            )
        }

        Column(
            Modifier.fillMaxSize().padding(bottom = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
        ) {
            Box(
                Modifier.width(220.dp).height(3.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.2f)),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(progress.coerceIn(0.05f, 1f))
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "BUILDING STORIES TOGETHER...",
                color = Color.White.copy(alpha = 0.95f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.4.sp,
            )
        }
    }
}
