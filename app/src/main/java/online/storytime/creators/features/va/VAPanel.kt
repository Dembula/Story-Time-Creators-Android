package online.storytime.creators.features.va

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import online.storytime.creators.core.Session
import online.storytime.creators.core.theme.STColor
import online.storytime.creators.core.theme.stIcon

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
        Icon(stIcon("sparkles"), "MODOC", tint = Color.Black, modifier = Modifier.size(28.dp))
    }
}

@Composable
fun VAPanel(onClose: () -> Unit) {
    val va = Session.va
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(va.messages.size) {
        if (va.messages.isNotEmpty()) listState.animateScrollToItem(va.messages.size - 1)
    }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { onClose() }) {
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(STColor.surface)
                .clickable(enabled = false) {}
                .statusBarsPadding()
                .imePadding(),
        ) {
            // Header
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(STColor.brandGradient),
                    contentAlignment = Alignment.Center,
                ) { Icon(stIcon("sparkles"), null, tint = Color.Black, modifier = Modifier.size(22.dp)) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("MODOC", color = STColor.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        if (va.isAvailable) "Production assistant" else "Currently unavailable",
                        color = STColor.textSecondary,
                        fontSize = 12.sp,
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(stIcon("xmark"), "Close", tint = STColor.textSecondary)
                }
            }

            // Messages
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (va.messages.isEmpty()) {
                    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Center) {
                        Text(va.greeting, color = STColor.textPrimary, fontSize = 15.sp)
                        if (va.suggestions.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            va.suggestions.forEach { s ->
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(STColor.surfaceElevated)
                                        .clickable { scope.launch { va.send(s) } }
                                        .padding(14.dp),
                                ) { Text(s, color = STColor.textSecondary, fontSize = 13.sp) }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(va.messages, key = { it.id }) { msg -> MessageBubble(msg) }
                    }
                }
            }

            // Composer
            Row(
                Modifier.fillMaxWidth().padding(12.dp).navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Ask MODOC…", color = STColor.textMuted) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = STColor.primary,
                        unfocusedBorderColor = STColor.border,
                        focusedTextColor = STColor.textPrimary,
                        unfocusedTextColor = STColor.textPrimary,
                        cursorColor = STColor.primary,
                    ),
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (input.isBlank() || va.isStreaming) STColor.textMuted else STColor.primary)
                        .clickable(enabled = input.isNotBlank() && !va.isStreaming) {
                            val text = input
                            input = ""
                            scope.launch { va.send(text) }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(stIcon("arrow.up.circle.fill"), "Send", tint = Color.Black, modifier = Modifier.size(24.dp))
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
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isUser) STColor.primary else STColor.surfaceElevated)
                .padding(12.dp),
        ) {
            Text(
                msg.text.ifEmpty { "…" },
                color = if (isUser) Color.Black else STColor.textPrimary,
                fontSize = 14.sp,
            )
        }
    }
}
