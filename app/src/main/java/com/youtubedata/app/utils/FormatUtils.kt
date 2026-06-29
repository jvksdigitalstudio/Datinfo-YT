package com.youtubedata.app.utils

import java.text.SimpleDateFormat
import java.util.*

object FormatUtils {

    fun formatNumber(n: Long): String = when {
        n >= 1_000_000_000 -> "%.1fB".format(n / 1_000_000_000.0)
        n >= 1_000_000     -> "%.1fM".format(n / 1_000_000.0)
        n >= 1_000         -> "%.1fK".format(n / 1_000.0)
        else               -> "%,d".format(n)
    }

    fun formatFull(n: Long): String = "%,d".format(n)

    fun formatDate(iso: String?): String {
        if (iso.isNullOrBlank()) return "—"
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(iso) ?: return iso
            val out = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
            out.format(date)
        } catch (_: Exception) { iso }
    }

    fun parseDuration(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        val m = Regex("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?").find(iso) ?: return ""
        val h  = m.groupValues[1].toIntOrNull() ?: 0
        val mi = m.groupValues[2].toIntOrNull() ?: 0
        val s  = m.groupValues[3].toIntOrNull() ?: 0
        return if (h > 0) "%d:%02d:%02d".format(h, mi, s)
               else       "%d:%02d".format(mi, s)
    }

    fun timeAgo(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(iso) ?: return ""
            val diff = System.currentTimeMillis() - date.time
            val days = diff / (1000 * 60 * 60 * 24)
            when {
                days < 1   -> "Hoy"
                days < 2   -> "Ayer"
                days < 7   -> "Hace $days días"
                days < 30  -> "Hace ${days / 7} semanas"
                days < 365 -> "Hace ${days / 30} meses"
                else       -> "Hace ${days / 365} años"
            }
        } catch (_: Exception) { "" }
    }
}
