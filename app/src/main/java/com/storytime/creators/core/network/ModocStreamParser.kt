package com.storytime.creators.core.network

import org.json.JSONObject

/** Strips Vercel AI SDK / OpenRouter stream framing and returns human-readable text. */
object ModocStreamParser {

    fun extractText(chunk: String): String {
        val output = StringBuilder()
        var buffer = chunk
        while (true) {
            val start = buffer.indexOf('{')
            if (start < 0) break
            if (start > 0) {
                val prefix = buffer.substring(0, start)
                if (prefix.trim().isNotEmpty()) output.append(prefix)
            }
            val end = buffer.indexOf('}', start)
            if (end < 0) break
            val slice = buffer.substring(start, end + 1)
            try {
                val obj = JSONObject(slice)
                val type = obj.optString("type", "")
                when {
                    type == "text-delta" && obj.has("textDelta") -> output.append(obj.optString("textDelta"))
                    type == "text-delta" && obj.has("delta") -> output.append(obj.optString("delta"))
                    obj.has("text") -> output.append(obj.optString("text"))
                    obj.has("delta") -> output.append(obj.optString("delta"))
                    obj.has("choices") -> {
                        val choices = obj.optJSONArray("choices")
                        val delta = choices?.optJSONObject(0)?.optJSONObject("delta")
                        val content = delta?.optString("content")
                        if (!content.isNullOrEmpty()) output.append(content)
                    }
                }
            } catch (e: Exception) {
                // ignore malformed slice
            }
            buffer = if (end < buffer.length - 1) buffer.substring(end + 1) else ""
        }
        val trimmed = buffer.trim()
        if (trimmed.isNotEmpty() && !trimmed.startsWith("{")) {
            output.append(trimmed)
        }
        return output.toString()
    }

    fun cleanFullResponse(text: String): String {
        if (!text.contains("{\"type\"")) return text
        val regex = Regex("(\\{\"type\"[^}]+\\})")
        val result = StringBuilder()
        var remaining = text
        while (true) {
            val match = regex.find(remaining) ?: break
            val before = remaining.substring(0, match.range.first)
            if (before.isNotEmpty()) result.append(before)
            result.append(extractText(match.value))
            remaining = remaining.substring(match.range.last + 1)
        }
        result.append(extractText(remaining))
        return result.toString().trim()
    }
}
