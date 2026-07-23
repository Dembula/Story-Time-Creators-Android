package com.storytime.creators.features.va

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.catch
import com.storytime.creators.core.network.ApiClient
import com.storytime.creators.core.network.ModocStreamParser
import com.storytime.creators.core.network.get
import com.storytime.creators.core.model.ChatPostBody
import com.storytime.creators.core.model.ChatUIMessage
import com.storytime.creators.core.model.ModocContext
import com.storytime.creators.core.model.ModocStatus

data class VAMessage(val id: String, val role: String, val text: String)

/** Mirrors the iOS VAController — MODOC chat state + streaming. */
class VAController(private val client: ApiClient) {

    var isPanelOpen by mutableStateOf(false)
    var isAvailable by mutableStateOf(false)
    var greeting by mutableStateOf("Hi, I'm MODOC — your production assistant.")
    var suggestions by mutableStateOf<List<String>>(emptyList())
    var isStreaming by mutableStateOf(false)
    val messages = mutableStateListOf<VAMessage>()

    private var pageContext: Map<String, String> = emptyMap()

    fun setContext(destinationTitle: String, projectId: String?) {
        pageContext = buildMap {
            put("page", destinationTitle)
            projectId?.let { put("projectId", it) }
        }
    }

    suspend fun bootstrap() {
        runCatching {
            val status: ModocStatus = client.get("/api/modoc/status")
            isAvailable = status.available ?: false
        }
        runCatching {
            val ctx: ModocContext = client.get("/api/modoc/context")
            ctx.greeting?.let { greeting = it }
            ctx.suggestions?.let { suggestions = it }
        }
    }

    fun toggle() { if (isPanelOpen) close() else open() }

    fun open() {
        isPanelOpen = true
        if (messages.isEmpty() && greeting.isNotEmpty()) {
            messages.add(VAMessage(System.nanoTime().toString(), "assistant", greeting))
        }
    }

    fun close() { isPanelOpen = false }

    suspend fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || isStreaming) return
        messages.add(VAMessage(System.nanoTime().toString(), "user", trimmed))
        val assistantId = (System.nanoTime() + 1).toString()
        messages.add(VAMessage(assistantId, "assistant", ""))
        isStreaming = true

        val history = messages
            .filter { it.text.isNotEmpty() }
            .map { ChatUIMessage(role = it.role, content = it.text) }
        val body = ChatPostBody(
            messages = history,
            scope = "creator",
            pageContext = pageContext.ifEmpty { null },
        )

        val builder = StringBuilder()
        runCatching {
            client.streamChat("/api/modoc/chat", client.json.encodeToString(body))
                .catch { }
                .collect { delta ->
                    builder.append(delta)
                    val idx = messages.indexOfFirst { it.id == assistantId }
                    if (idx >= 0) {
                        messages[idx] = messages[idx].copy(text = builder.toString())
                    }
                }
        }
        val finalText = ModocStreamParser.cleanFullResponse(builder.toString())
        val idx = messages.indexOfFirst { it.id == assistantId }
        if (idx >= 0) {
            messages[idx] = messages[idx].copy(
                text = finalText.ifEmpty { "Sorry, I couldn't respond right now." },
            )
        }
        isStreaming = false
    }
}
