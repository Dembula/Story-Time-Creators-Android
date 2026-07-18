package online.storytime.creators.core.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Thin HTTP layer mirroring the iOS `APIClient`. Uses cookie-based NextAuth
 * sessions against the production Story Time backend.
 */
class ApiClient(val cookieJar: PersistentCookieJar) {

    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = false
        isLenient = true
        coerceInputValues = true
    }

    val http: OkHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .callTimeout(120, TimeUnit.SECONDS)
        .build()

    fun urlFor(path: String, query: List<Pair<String, String>> = emptyList()): String {
        val cleaned = if (path.startsWith("/")) path else "/$path"
        val base = AppConfig.API_BASE_URL + cleaned
        if (query.isEmpty()) return base
        val qs = query.joinToString("&") { (k, v) ->
            "${k.enc()}=${v.enc()}"
        }
        return "$base?$qs"
    }

    /** Executes a request and returns the response body string, throwing [ApiException] on failure. */
    suspend fun raw(
        method: String,
        path: String,
        query: List<Pair<String, String>> = emptyList(),
        jsonBody: String? = null,
    ): String {
        val url = urlFor(path, query).toHttpUrlOrNull() ?: throw ApiException.InvalidUrl
        val builder = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .header("User-Agent", "StoryTimeCreators-Android")

        if (jsonBody != null) {
            builder.method(method, jsonBody.toRequestBody(JSON_MEDIA))
        } else if (method == "GET") {
            builder.get()
        } else {
            builder.method(method, if (method == "POST" || method == "PATCH") "".toRequestBody(JSON_MEDIA) else null)
        }

        val response = try {
            execute(builder.build())
        } catch (e: IOException) {
            throw ApiException.Network(e.message ?: "Network error.")
        }

        response.use { resp ->
            val code = resp.code
            val bodyStr = resp.body?.string() ?: ""
            when {
                code == 401 -> throw ApiException.Unauthorized
                code == 403 -> throw ApiException.Forbidden
                code in 200..299 -> return bodyStr
                else -> throw ApiException.Http(code, extractError(bodyStr))
            }
        }
    }

    /** Form-encoded POST used by the NextAuth credentials flow. Returns (status, body). */
    suspend fun postForm(path: String, fields: Map<String, String>): Pair<Int, String> {
        val url = urlFor(path).toHttpUrlOrNull() ?: throw ApiException.InvalidUrl
        val body = fields.entries.joinToString("&") { (k, v) -> "${k.enc()}=${v.enc()}" }
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .post(body.toRequestBody(FORM_MEDIA))
            .build()
        val response = try {
            execute(request)
        } catch (e: IOException) {
            throw ApiException.Network(e.message ?: "Network error.")
        }
        response.use { resp ->
            return resp.code to (resp.body?.string() ?: "")
        }
    }

    /** Raw PUT used for storage uploads (S3-style presigned URLs). */
    suspend fun putBinary(rawUrl: String, bytes: ByteArray, contentType: String): Boolean {
        val url = rawUrl.toHttpUrlOrNull() ?: throw ApiException.InvalidUrl
        val request = Request.Builder()
            .url(url)
            .put(bytes.toRequestBody(contentType.toMediaType()))
            .build()
        val response = try {
            execute(request)
        } catch (e: IOException) {
            throw ApiException.Network(e.message ?: "Upload error.")
        }
        response.use { return it.code in 200..299 }
    }

    /** Streams the MODOC chat SSE / chunked response, emitting decoded text deltas. */
    fun streamChat(path: String, jsonBody: String): Flow<String> = flow {
        val url = urlFor(path).toHttpUrlOrNull() ?: throw ApiException.InvalidUrl
        val request = Request.Builder()
            .url(url)
            .header("Accept", "text/event-stream, application/json, text/plain")
            .post(jsonBody.toRequestBody(JSON_MEDIA))
            .build()

        val response = http.newCall(request).execute()
        response.use { resp ->
            if (resp.code == 401) throw ApiException.Unauthorized
            if (resp.code !in 200..299) throw ApiException.Http(resp.code, null)
            val source = resp.body?.source() ?: throw ApiException.EmptyResponse
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: break
                when {
                    line.startsWith("data:") -> {
                        val payload = line.removePrefix("data:").trim()
                        if (payload == "[DONE]") break
                        val sseText = parseSseText(payload)
                        if (sseText != null) {
                            emit(ModocStreamParser.extractText(sseText))
                        } else if (payload.isNotEmpty()) {
                            emit(ModocStreamParser.extractText(payload))
                        }
                    }
                    line.isNotEmpty() && !line.startsWith(":") && !line.startsWith("event:") -> {
                        emit(ModocStreamParser.extractText(line))
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun parseSseText(payload: String): String? {
        return try {
            val obj = json.parseToJsonElement(payload) as? JsonObject ?: return null
            obj["text"]?.jsonPrimitive?.contentOrNullSafe()
                ?: obj["delta"]?.jsonPrimitive?.contentOrNullSafe()
                ?: obj["content"]?.jsonPrimitive?.contentOrNullSafe()
        } catch (e: Exception) {
            null
        }
    }

    private fun extractError(body: String): String? {
        if (body.isBlank()) return null
        return try {
            val obj = json.parseToJsonElement(body) as? JsonObject ?: return body
            obj["error"]?.jsonPrimitive?.contentOrNullSafe()
                ?: obj["message"]?.jsonPrimitive?.contentOrNullSafe()
                ?: obj["statusText"]?.jsonPrimitive?.contentOrNullSafe()
        } catch (e: Exception) {
            body
        }
    }

    private suspend fun execute(request: Request): okhttp3.Response = suspendCoroutine { cont ->
        val call = http.newCall(request)
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                cont.resumeWithException(e)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                cont.resumeWith(Result.success(response))
            }
        })
    }

    companion object {
        private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
        private val FORM_MEDIA = "application/x-www-form-urlencoded".toMediaType()
    }
}

private fun String.enc(): String = URLEncoder.encode(this, "UTF-8")

private fun kotlinx.serialization.json.JsonPrimitive.contentOrNullSafe(): String? =
    if (this.isString) this.content else this.content.takeIf { it != "null" }

// -------- Reified JSON helpers (mirror APIClient.get/post/patch/delete) --------

suspend inline fun <reified T> ApiClient.get(
    path: String,
    query: List<Pair<String, String>> = emptyList(),
): T = decodeBody(raw("GET", path, query, null))

suspend inline fun <reified T> ApiClient.post(path: String): T =
    decodeBody(raw("POST", path, emptyList(), null))

suspend inline fun <reified T, reified B> ApiClient.post(path: String, body: B): T =
    decodeBody(raw("POST", path, emptyList(), json.encodeToString(body)))

suspend inline fun <reified T, reified B> ApiClient.patch(path: String, body: B): T =
    decodeBody(raw("PATCH", path, emptyList(), json.encodeToString(body)))

suspend inline fun <reified T> ApiClient.patchEmpty(path: String): T =
    decodeBody(raw("PATCH", path, emptyList(), null))

suspend inline fun <reified T> ApiClient.delete(path: String): T =
    decodeBody(raw("DELETE", path, emptyList(), null))

inline fun <reified T> ApiClient.decodeBody(body: String): T {
    val payload = body.ifBlank { "{}" }
    return try {
        json.decodeFromString<T>(payload)
    } catch (e: Exception) {
        throw ApiException.Decoding(e.message ?: "decode failure")
    }
}
