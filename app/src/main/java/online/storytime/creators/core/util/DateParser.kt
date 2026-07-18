package online.storytime.creators.core.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Mirrors the iOS DateParser for consistent timestamp handling. */
object DateParser {

    private val zone: ZoneId = ZoneId.systemDefault()

    fun parse(raw: String?): Instant? {
        if (raw.isNullOrEmpty()) return null
        // Try full ISO-8601 with offset / fractional seconds.
        runCatching { return OffsetDateTime.parse(raw).toInstant() }
        runCatching { return Instant.parse(raw) }
        runCatching {
            return LocalDate.parse(raw).atStartOfDay(zone).toInstant()
        }
        return null
    }

    fun display(raw: String?): String {
        val instant = parse(raw) ?: return raw ?: ""
        val fmt = DateTimeFormatter
            .ofPattern("MMM d, yyyy · HH:mm", Locale.getDefault())
            .withZone(zone)
        return fmt.format(instant)
    }

    fun displayDate(raw: String?): String {
        val instant = parse(raw) ?: return raw ?: ""
        val fmt = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()).withZone(zone)
        return fmt.format(instant)
    }

    fun monthKey(date: LocalDate): String =
        DateTimeFormatter.ofPattern("yyyy-MM").format(date)

    fun dayKey(date: LocalDate): String =
        DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date)

    fun localDate(raw: String?): LocalDate? {
        val instant = parse(raw) ?: return null
        return instant.atZone(zone).toLocalDate()
    }

    fun isoInstant(date: Instant): String = DateTimeFormatter.ISO_INSTANT.format(date)

    fun relative(raw: String?): String {
        val instant = parse(raw) ?: return raw ?: ""
        val now = Instant.now()
        val secs = (now.epochSecond - instant.epochSecond).coerceAtLeast(0)
        return when {
            secs < 60 -> "now"
            secs < 3600 -> "${secs / 60}m"
            secs < 86400 -> "${secs / 3600}h"
            secs < 604800 -> "${secs / 86400}d"
            else -> "${secs / 604800}w"
        }
    }
}

object JsonStringArray {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    fun decode(raw: String?): List<String> {
        if (raw.isNullOrEmpty()) return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(String.serializer()), raw)
        }.getOrDefault(emptyList())
    }
}

fun String.nilIfEmpty(): String? {
    val t = trim()
    return t.ifEmpty { null }
}

fun formatZAR(value: Double?): String =
    if (value == null) "R0" else "R%.2f".format(value)

fun formatZARInt(value: Double?): String =
    if (value == null) "R0" else "R%.0f".format(value)

fun formatPercent(value: Double?, decimals: Int = 1): String =
    if (value == null) "0%" else "%.${decimals}f%%".format(value)

fun formatDuration(seconds: Double?): String {
    if (seconds == null || seconds <= 0) return "0m"
    val hours = seconds.toInt() / 3600
    val mins = (seconds.toInt() % 3600) / 60
    return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
}

fun formatHours(seconds: Double?): String =
    if (seconds == null) "0h" else "%.1fh".format(seconds / 3600.0)

fun formatTrackDuration(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(minutes, secs)
}
