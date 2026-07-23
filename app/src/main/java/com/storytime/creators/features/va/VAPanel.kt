package com.storytime.creators.features.va

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.storytime.creators.core.Session
import com.storytime.creators.core.theme.STColor
import com.storytime.creators.core.theme.stIcon

@Composable
fun VAFloatingButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(STColor.brandGradient)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(stIcon("sparkles"), "Story Time VA", tint = Color.Black, modifier = Modifier.size(28.dp))
    }
}

@Composable
fun VAPanel(onClose: () -> Unit) {
    val va = Session.va
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(va.messages.size, va.messages.lastOrNull()?.text) {
        if (va.messages.isNotEmpty()) listState.animateScrollToItem(va.messages.size - 1)
    }

    // Full-screen right-aligned panel with a dimmed scrim (mirrors iOS VAPanelView).
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable { onClose() },
        contentAlignment = Alignment.CenterEnd,
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(STColor.surface)
                .clickable(enabled = false) {}
                .statusBarsPadding()
                .imePadding(),
        ) {
            // Header
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(STColor.background)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(STColor.primary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) { Icon(stIcon("sparkles"), null, tint = STColor.primary, modifier = Modifier.size(22.dp)) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Story Time VA", color = STColor.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        if (va.isAvailable) "Online" else "Checking availability…",
                        color = if (va.isAvailable) STColor.success else STColor.textMuted,
                        fontSize = 11.sp,
                    )
                }
                Box(
                    Modifier.size(36.dp).clip(CircleShape).background(STColor.surfaceElevated).clickable { onClose() },
                    contentAlignment = Alignment.Center,
                ) { Icon(stIcon("xmark"), "Close", tint = STColor.textSecondary, modifier = Modifier.size(14.dp)) }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(STColor.border))

            // Messages
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (va.messages.isEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Your production assistant", color = STColor.textPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Ask about scripts, schedules, casting, crew, locations, or your pipeline.",
                                color = STColor.textSecondary, fontSize = 13.sp,
                            )
                        }
                    }
                }
                items(va.messages, key = { it.id }) { msg -> MessageBubble(msg) }
                if (va.isStreaming) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(color = STColor.primary, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                            Text("Thinking…", color = STColor.textMuted, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Suggestions row
            if (va.suggestions.isNotEmpty()) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(STColor.border))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    va.suggestions.forEach { s ->
                        Box(
                            Modifier
                                .clip(CircleShape)
                                .background(STColor.primary.copy(alpha = 0.12f))
                                .clickable(enabled = !va.isStreaming) { scope.launch { va.send(s) } }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        ) { Text(s, color = STColor.accent, fontSize = 12.sp, fontWeight = FontWeight.Medium) }
                    }
                }
            }

            // Composer
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(STColor.background)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Ask anything…", color = STColor.textMuted) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = STColor.primary,
                        unfocusedBorderColor = STColor.border,
                        focusedContainerColor = STColor.surfaceElevated,
                        unfocusedContainerColor = STColor.surfaceElevated,
                        focusedTextColor = STColor.textPrimary,
                        unfocusedTextColor = STColor.textPrimary,
                        cursorColor = STColor.primary,
                    ),
                )
                Spacer(Modifier.width(10.dp))
                val canSend = input.isNotBlank() && !va.isStreaming
                IconButton(
                    onClick = {
                        val text = input
                        input = ""
                        scope.launch { va.send(text) }
                    },
                    enabled = canSend,
                ) {
                    Icon(
                        stIcon("arrow.up.circle.fill"),
                        "Send",
                        tint = if (canSend) STColor.primary else STColor.textMuted,
                        modifier = Modifier.size(34.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: VAMessage) {
    val isUser = msg.role == "user"
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            Modifier
                .widthIn(max = 320.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isUser) STColor.primary else STColor.surfaceElevated)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                msg.text.ifEmpty { "…" },
                color = if (isUser) Color.Black.copy(alpha = 0.9f) else STColor.textPrimary,
                fontSize = 14.sp,
            )
        }
    }
}
