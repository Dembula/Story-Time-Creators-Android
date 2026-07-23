package com.storytime.creators.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.storytime.creators.core.Session
import com.storytime.creators.core.theme.STColor
import com.storytime.creators.core.theme.stIcon
import com.storytime.creators.ui.GradientButton
import com.storytime.creators.ui.STTextField

@Composable
fun SignInScreen() {
    val auth = Session.auth
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        Modifier
            .fillMaxSize()
            .background(STColor.background)
            .imePadding(),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(Modifier.height(40.dp))
            Box(
                Modifier.size(84.dp).clip(RoundedCornerShape(22.dp)).background(STColor.brandGradient),
                contentAlignment = Alignment.Center,
            ) {
                Icon(stIcon("sparkles"), null, tint = Color.Black, modifier = Modifier.size(44.dp))
            }
            Spacer(Modifier.height(20.dp))
            Text("Story Time Creators", color = STColor.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(
                "Sign in to your creator account",
                color = STColor.textSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))

            STTextField(email, { email = it }, "Email", keyboardType = KeyboardType.Email)
            Spacer(Modifier.height(14.dp))
            STTextField(password, { password = it }, "Password", isPassword = true)

            auth.lastError?.let { err ->
                Spacer(Modifier.height(14.dp))
                Text(err, color = STColor.danger, fontSize = 13.sp, textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(22.dp))
            GradientButton(
                label = "Sign In",
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotBlank() && password.isNotBlank(),
                busy = auth.isBusy,
            ) {
                scope.launch { auth.signIn(email, password) }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "Uses the same creator account as story-time.online",
                color = STColor.textMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
