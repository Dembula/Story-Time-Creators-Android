package online.storytime.creators.features.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import online.storytime.creators.core.model.MarketplaceMessage
import online.storytime.creators.core.model.NetworkChatMessage
import online.storytime.creators.core.model.NetworkChatThreadResponse
import online.storytime.creators.core.model.NetworkChatsResponse
import online.storytime.creators.core.model.SendMarketplaceMessageBody
import online.storytime.creators.core.network.get
import online.storytime.creators.core.network.post
import online.storytime.creators.core.theme.STColor
import online.storytime.creators.core.theme.stIcon
import online.storytime.creators.ui.EmptyStateView
import online.storytime.creators.ui.SegmentedTabs
import online.storytime.creators.features.network.Avatar

private data class ThreadRef(val isNetwork: Boolean, val peerId: String, val title: String)
private data class InboxThread(val peerId: String, val title: String, val preview: String)

@kotlinx.serialization.Serializable
private data class NetworkSendBody(val body: String)

@Composable
fun MessagesScreen() {
    val client = Session.api
    val myId = Session.auth.currentUser?.id
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(0) }

    var conversations by remember { mutableStateOf<List<online.storytime.creators.core.model.NetworkConversation>>(emptyList()) }
    var inbox by remember { mutableStateOf<List<InboxThread>>(emptyList()) }
    var openThread by remember { mutableStateOf<ThreadRef?>(null) }

    suspend fun loadAll() {
        runCatching { conversations = client.get<NetworkChatsResponse>("/api/network/chats").conversations ?: emptyList() }
        runCatching {
            val msgs = client.get<List<MarketplaceMessage>>("/api/messages")
            inbox = buildInbox(msgs, myId)
        }
    }
    LaunchedEffect(Unit) { loadAll() }

    val thread = openThread
    if (thread != null) {
        ConversationView(thread) { openThread = null }
        return
    }

    Column(Modifier.fillMaxWidth()) {
        SegmentedTabs(listOf("Network", "Inbox"), tab) { tab = it }
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (tab == 0) {
                if (conversations.isEmpty()) EmptyStateView("bubble.left.and.bubble.right.fill", "No conversations yet", "Connect with creators, then message them.")
                conversations.forEach { conv ->
                    val peer = conv.participants?.firstOrNull()
                    ThreadRow(peer?.label ?: "Creator", conv.lastMessage?.body ?: "No messages yet") {
                        openThread = ThreadRef(true, peer?.id ?: "", peer?.label ?: "Creator")
                    }
                }
            } else {
                if (inbox.isEmpty()) EmptyStateView("tray", "Marketplace inbox is empty", "Booking threads from cast, crew, locations, and catering show up here.")
                inbox.forEach { t ->
                    ThreadRow(t.title, t.preview) { openThread = ThreadRef(false, t.peerId, t.title) }
                }
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

private fun buildInbox(messages: List<MarketplaceMessage>, myId: String?): List<InboxThread> {
    if (myId == null) return emptyList()
    val map = linkedMapOf<String, InboxThread>()
    for (msg in messages) {
        val peer = if (msg.senderId == myId) msg.receiverId else msg.senderId
        val name = if (msg.senderId == myId) msg.receiver?.name else msg.sender?.name
        val context = msg.locationBooking?.location?.name
            ?: msg.crewTeamRequest?.crewTeam?.companyName
            ?: msg.castingInquiry?.agency?.agencyName
            ?: msg.request?.equipment?.companyName
        map[peer] = InboxThread(peer, name ?: context ?: "Conversation", msg.body)
    }
    return map.values.sortedBy { it.title }
}

@Composable
private fun ThreadRow(title: String, preview: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(STColor.surface).clickable { onClick() }.padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(title, null)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = STColor.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(preview, color = STColor.textMuted, fontSize = 12.sp, maxLines = 1)
        }
        Icon(stIcon("chevron.right"), null, tint = STColor.textMuted, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun ConversationView(thread: ThreadRef, onBack: () -> Unit) {
    val client = Session.api
    val myId = Session.auth.currentUser?.id
    val scope = rememberCoroutineScope()
    var input by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    // pair: (id, text, mine)
    var messages by remember { mutableStateOf<List<Triple<String, String, Boolean>>>(emptyList()) }

    suspend fun load() {
        if (thread.isNetwork) {
            runCatching {
                val t = client.get<NetworkChatThreadResponse>("/api/network/chats/${thread.peerId}")
                messages = (t.messages ?: emptyList()).map { Triple(it.id, it.body ?: "", it.sender?.id == myId) }
            }.onFailure { error = it.message }
        } else {
            runCatching {
                val msgs = client.get<List<MarketplaceMessage>>("/api/messages", listOf("peerId" to thread.peerId))
                messages = msgs.map { Triple(it.id, it.body, it.senderId == myId) }
            }.onFailure { error = it.message }
        }
    }
    LaunchedEffect(thread.peerId) { load() }

    Column(Modifier.fillMaxSize().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.clickable { onBack() }, verticalAlignment = Alignment.CenterVertically) {
                Icon(stIcon("chevron.left"), null, tint = STColor.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(thread.title, color = STColor.textPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        error?.let { Text(it, color = STColor.danger, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp)) }
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(messages) { (id, text, mine) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
                    Box(
                        Modifier.fillMaxWidth(0.8f).clip(RoundedCornerShape(16.dp)).background(if (mine) STColor.primary else STColor.surfaceElevated).padding(12.dp),
                    ) { Text(text, color = if (mine) Color.Black else STColor.textPrimary, fontSize = 14.sp) }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input, onValueChange = { input = it },
                placeholder = { Text("Message…", color = STColor.textMuted) },
                modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp), maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = STColor.primary, unfocusedBorderColor = STColor.border,
                    focusedTextColor = STColor.textPrimary, unfocusedTextColor = STColor.textPrimary, cursorColor = STColor.primary,
                ),
            )
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier.size(46.dp).clip(CircleShape).background(if (input.isBlank() || sending) STColor.textMuted else STColor.primary)
                    .clickable(enabled = input.isNotBlank() && !sending) {
                        val text = input.trim()
                        input = ""
                        scope.launch {
                            sending = true
                            error = null
                            runCatching {
                                if (thread.isNetwork) {
                                    val msg = client.post<NetworkChatMessage, NetworkSendBody>("/api/network/chats/${thread.peerId}", NetworkSendBody(text))
                                    messages = messages + Triple(msg.id, msg.body ?: text, true)
                                } else {
                                    val msg = client.post<MarketplaceMessage, SendMarketplaceMessageBody>("/api/messages", SendMarketplaceMessageBody(body = text, receiverId = thread.peerId))
                                    messages = messages + Triple(msg.id, msg.body, true)
                                }
                            }.onFailure { error = it.message }
                            sending = false
                        }
                    },
                contentAlignment = Alignment.Center,
            ) { Icon(stIcon("arrow.up.circle.fill"), "Send", tint = Color.Black, modifier = Modifier.size(24.dp)) }
        }
    }
}
