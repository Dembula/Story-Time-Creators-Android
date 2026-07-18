package online.storytime.creators.core.network

import android.content.Context
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

/**
 * Persists session cookies (NextAuth) to SharedPreferences so the creator stays
 * signed in across launches. Mirrors iOS `HTTPCookieStorage.shared` behaviour.
 */
class PersistentCookieJar(context: Context) : CookieJar {

    private val prefs = context.applicationContext
        .getSharedPreferences("st_cookies", Context.MODE_PRIVATE)

    // host -> (cookie name -> serialized cookie)
    private val cache = ConcurrentHashMap<String, MutableMap<String, Cookie>>()

    init {
        loadFromDisk()
    }

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        val map = cache.getOrPut(host) { mutableMapOf() }
        for (cookie in cookies) {
            if (cookie.expiresAt < System.currentTimeMillis() && !cookie.persistent) {
                map.remove(cookie.name)
            } else if (cookie.expiresAt < System.currentTimeMillis()) {
                map.remove(cookie.name)
            } else {
                map[cookie.name] = cookie
            }
        }
        persist()
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        val result = mutableListOf<Cookie>()
        val map = cache[url.host] ?: return emptyList()
        val expired = mutableListOf<String>()
        for ((name, cookie) in map) {
            if (cookie.expiresAt < now) {
                expired.add(name)
            } else if (cookie.matches(url)) {
                result.add(cookie)
            }
        }
        expired.forEach { map.remove(it) }
        if (expired.isNotEmpty()) persist()
        return result
    }

    @Synchronized
    fun clear() {
        cache.clear()
        prefs.edit().clear().apply()
    }

    private fun persist() {
        val serialized = mutableSetOf<String>()
        for ((host, map) in cache) {
            for (cookie in map.values) {
                serialized.add("$host|" + cookie.toString())
            }
        }
        prefs.edit().putStringSet(KEY, serialized).apply()
    }

    private fun loadFromDisk() {
        val stored = prefs.getStringSet(KEY, emptySet()) ?: emptySet()
        for (entry in stored) {
            val sep = entry.indexOf('|')
            if (sep <= 0) continue
            val host = entry.substring(0, sep)
            val raw = entry.substring(sep + 1)
            val url = HttpUrl.Builder().scheme("https").host(host).build()
            val cookie = Cookie.parse(url, raw) ?: continue
            if (cookie.expiresAt < System.currentTimeMillis()) continue
            cache.getOrPut(host) { mutableMapOf() }[cookie.name] = cookie
        }
    }

    companion object {
        private const val KEY = "cookies_v1"
    }
}
