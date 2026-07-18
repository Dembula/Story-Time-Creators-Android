package online.storytime.creators.features.network

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import online.storytime.creators.core.Session
import online.storytime.creators.core.model.ActionBody
import online.storytime.creators.core.model.ConnectBody
import online.storytime.creators.core.model.ConnectStatusResponse
import online.storytime.creators.core.model.ConnectionRequestRow
import online.storytime.creators.core.model.CreateNetworkPostBody
import online.storytime.creators.core.model.DiscoverCreator
import online.storytime.creators.core.model.EnrichedNetworkPost
import online.storytime.creators.core.model.FollowResponse
import online.storytime.creators.core.model.NetworkConnectionsResponse
import online.storytime.creators.core.model.NetworkCreatorsResponse
import online.storytime.creators.core.model.NetworkPostsResponse
import online.storytime.creators.core.model.OkResponse
import online.storytime.creators.core.network.delete
import online.storytime.creators.core.network.get
import online.storytime.creators.core.network.patch
import online.storytime.creators.core.network.post
import online.storytime.creators.core.theme.STColor
import online.storytime.creators.core.theme.stIcon
import online.storytime.creators.core.util.DateParser
import online.storytime.creators.ui.EmptyStateView
import online.storytime.creators.ui.GradientButton
import online.storytime.creators.ui.LoadingStateView
import online.storytime.creators.ui.Pill
import online.storytime.creators.ui.STTextField
import online.storytime.creators.ui.SegmentedTabs

@Composable
fun NetworkScreen() {
    val client = Session.api
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(0) }
    val tabs = listOf("Feed", "Discover", "Connections")

    var loading by remember { mutableStateOf(true) }
    var posts by remember { mutableStateOf<List<EnrichedNetworkPost>>(emptyList()) }
    var connections by remember { mutableStateOf(NetworkConnectionsResponse()) }
    var discovered by remember { mutableStateOf<List<DiscoverCreator>>(emptyList()) }
    var compose by remember { mutableStateOf("") }
    var posting by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    suspend fun loadAll() {
        loading = true
        runCatching {
            posts = client.get<NetworkPostsResponse>("/api/network/posts", listOf("mode" to "feed", "limit" to "30")).posts ?: emptyList()
        }
        runCatching { connections = client.get("/api/network/connections") }
        loading = false
    }
    suspend fun search() {
        val q = if (query.isBlank()) emptyList() else listOf("q" to query)
        runCatching { discovered = client.get<NetworkCreatorsResponse>("/api/network/creators", q).creators ?: emptyList() }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) { loadAll() }
    androidx.compose.runtime.LaunchedEffect(tab) { if (tab == 1 && discovered.isEmpty()) search() }

    Column(Modifier.fillMaxWidth()) {
        SegmentedTabs(tabs, tab) { tab = it }
        when {
            loading && posts.isEmpty() && tab == 0 -> LoadingStateView()
            else -> when (tab) {
                0 -> Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(STColor.surface).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Share an update", color = STColor.textMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        STTextField(compose, { compose = it }, "What's happening on your production?", singleLine = false)
                        GradientButton("Post", Modifier.fillMaxWidth(), enabled = compose.isNotBlank(), busy = posting) {
                            scope.launch {
                                posting = true
                                val text = compose
                                val created = runCatching {
                                    client.post<EnrichedNetworkPost, CreateNetworkPostBody>("/api/network/posts", CreateNetworkPostBody(body = text))
                                }.getOrNull()
                                if (created != null) { posts = listOf(created) + posts; compose = "" }
                                posting = false
                            }
                        }
                    }
                    if (posts.isEmpty()) EmptyStateView("person.3.fill", "Your feed is quiet", "Follow creators in Discover to see their updates here.")
                    posts.forEach { PostCard(it) }
                    Spacer(Modifier.height(30.dp))
                }
                1 -> Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f)) { STTextField(query, { query = it }, "Search creators") }
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.size(50.dp).clip(RoundedCornerShape(12.dp)).background(STColor.primary).clickable { scope.launch { search() } }, contentAlignment = Alignment.Center) {
                            Icon(stIcon("magnifyingglass"), null, tint = Color.Black)
                        }
                    }
                    if (discovered.isEmpty()) EmptyStateView("magnifyingglass", "No creators found", "Try a different search.")
                    discovered.forEachIndexed { idx, creator ->
                        DiscoverRow(
                            creator,
                            onFollow = {
                                scope.launch {
                                    val following = creator.following == true
                                    runCatching {
                                        if (following) client.delete<FollowResponse>("/api/network/follow/${creator.id}")
                                        else client.post<FollowResponse>("/api/network/follow/${creator.id}")
                                    }
                                    discovered = discovered.toMutableList().also { it[idx] = creator.copy(following = !following) }
                                }
                            },
                            onConnect = {
                                scope.launch {
                                    runCatching { client.post<ConnectStatusResponse, ConnectBody>("/api/network/connect/${creator.id}", ConnectBody()) }
                                    search()
                                }
                            },
                        )
                    }
                    Spacer(Modifier.height(30.dp))
                }
                else -> Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val received = connections.received.orEmpty()
                    val sent = connections.sent.orEmpty()
                    if (received.isEmpty() && sent.isEmpty()) EmptyStateView("person.2.fill", "No connections yet", "Connect with creators in Discover.")
                    if (received.isNotEmpty()) {
                        Text("Requests received", color = STColor.textMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        received.forEach { req ->
                            ConnectionRow(req, req.from?.label ?: "Creator", showActions = true,
                                onAccept = { respond(client, scope, req.id, true) { scope.launch { loadAll() } } },
                                onDecline = { respond(client, scope, req.id, false) { scope.launch { loadAll() } } })
                        }
                    }
                    if (sent.isNotEmpty()) {
                        Text("Requests sent", color = STColor.textMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        sent.forEach { req -> ConnectionRow(req, req.to?.label ?: "Creator", showActions = false, onAccept = {}, onDecline = {}) }
                    }
                    Spacer(Modifier.height(30.dp))
                }
            }
        }
    }
}

private fun respond(
    client: online.storytime.creators.core.network.ApiClient,
    scope: kotlinx.coroutines.CoroutineScope,
    requestId: String,
    accept: Boolean,
    reload: () -> Unit,
) {
    scope.launch {
        runCatching { client.patch<OkResponse, ActionBody>("/api/network/connections/$requestId", ActionBody(if (accept) "accept" else "decline")) }
        reload()
    }
}

@Composable
private fun PostCard(post: EnrichedNetworkPost) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(STColor.surface).padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(post.author?.label ?: "C", post.author?.image)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(post.author?.label ?: "Creator", color = STColor.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                post.author?.headline?.let { Text(it, color = STColor.textMuted, fontSize = 11.sp, maxLines = 1) }
            }
            post.createdAt?.let { Text(DateParser.relative(it), color = STColor.textMuted, fontSize = 10.sp) }
        }
        post.body?.takeIf { it.isNotEmpty() }?.let { Text(it, color = STColor.textSecondary, fontSize = 14.sp) }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MiniCount(if (post.likedByViewer == true) "heart.fill" else "heart", post.likeCount ?: 0)
            MiniCount("bubble.left", post.commentCount ?: 0)
            MiniCount(if (post.savedByViewer == true) "bookmark.fill" else "bookmark.fill", post.saveCount ?: 0)
        }
    }
}

@Composable
private fun MiniCount(icon: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(stIcon(icon), null, tint = STColor.textMuted, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text("$count", color = STColor.textMuted, fontSize = 12.sp)
    }
}

@Composable
private fun DiscoverRow(creator: DiscoverCreator, onFollow: () -> Unit, onConnect: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(STColor.surface).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(creator.label, creator.image)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(creator.label, color = STColor.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                creator.headline?.let { Text(it, color = STColor.textMuted, fontSize = 12.sp, maxLines = 1) }
            }
        }
        creator.bio?.takeIf { it.isNotEmpty() }?.let { Text(it, color = STColor.textSecondary, fontSize = 13.sp, maxLines = 2) }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionChip(if (creator.following == true) "Following" else "Follow", creator.following == true, onFollow)
            ActionChip(if (creator.connectionStatus == "PENDING_SENT") "Requested" else "Connect", false, onConnect)
        }
    }
}

@Composable
private fun ActionChip(label: String, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(20.dp)).background(if (active) STColor.primary.copy(alpha = 0.2f) else STColor.surfaceElevated).clickable { onClick() }.padding(horizontal = 16.dp, vertical = 8.dp),
    ) { Text(label, color = if (active) STColor.primary else STColor.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium) }
}

@Composable
private fun ConnectionRow(req: ConnectionRequestRow, name: String, showActions: Boolean, onAccept: () -> Unit, onDecline: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(STColor.surface).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(name, null)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(name, color = STColor.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                req.status?.let { Pill(it.replace("_", " ")) }
            }
        }
        req.message?.takeIf { it.isNotEmpty() }?.let { Text(it, color = STColor.textSecondary, fontSize = 13.sp) }
        if (showActions) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionChip("Accept", true, onAccept)
                ActionChip("Decline", false, onDecline)
            }
        }
    }
}

@Composable
fun Avatar(name: String, imageUrl: String?) {
    Box(Modifier.size(40.dp).clip(CircleShape).background(STColor.primary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
        if (!imageUrl.isNullOrEmpty()) {
            AsyncImage(model = imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(40.dp))
        } else {
            Text(name.firstOrNull()?.uppercase() ?: "C", color = STColor.primary, fontWeight = FontWeight.Bold)
        }
    }
}
