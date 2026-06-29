package com.youtubedata.app.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.youtubedata.app.data.model.ChannelData
import java.io.File

object ExportUtils {

    fun exportJson(context: Context, channel: ChannelData) {
        val json = buildJson(channel)
        shareFile(context, "youtube_${channel.handle.replace("@","")}.json", json, "application/json")
    }

    fun exportTxt(context: Context, channel: ChannelData) {
        val txt = buildTxt(channel)
        shareFile(context, "youtube_${channel.handle.replace("@","")}.txt", txt, "text/plain")
    }

    private fun shareFile(context: Context, fileName: String, content: String, mimeType: String) {
        val file = File(context.cacheDir, fileName)
        file.writeText(content)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Exportar $fileName"))
    }

    private fun buildJson(c: ChannelData): String {
        return """
{
  "channel": {
    "id": "${c.id}",
    "name": "${c.name.escapeJson()}",
    "handle": "${c.handle}",
    "url": "${c.url}",
    "avatarUrl": "${c.avatarUrl}",
    "bannerUrl": "${c.bannerUrl}",
    "country": "${c.country}",
    "createdAt": "${c.createdAt}",
    "description": "${c.description.escapeJson()}",
    "stats": {
      "subscribers": ${c.subscribersRaw},
      "videos": ${c.videosRaw},
      "totalViews": ${c.totalViewsRaw},
      "hiddenSubscribers": ${c.hiddenSubscribers}
    },
    "keywords": [${c.keywords.joinToString(", ") { "\"${it.escapeJson()}\"" }}],
    "topicCategories": [${c.topicCategories.joinToString(", ") { "\"$it\"" }}],
    "socialLinks": [
      ${c.socialLinks.joinToString(",\n      ") { 
        """{"name": "${it.name}", "href": "${it.href}", "handle": "${it.handle}"}"""
      }}
    ]
  },
  "videos": [
    ${c.videos.joinToString(",\n    ") { v ->
      """{"id": "${v.id}", "title": "${v.title.escapeJson()}", "views": ${v.viewsRaw}, "likes": ${v.likesRaw}, "duration": "${v.duration}", "publishedAt": "${v.publishedAt}"}"""
    }}
  ]
}
        """.trimIndent()
    }

    private fun buildTxt(c: ChannelData): String {
        val sb = StringBuilder()
        sb.appendLine("═══════════════════════════════════════")
        sb.appendLine("  YouTube Data — ${c.name}")
        sb.appendLine("═══════════════════════════════════════")
        sb.appendLine()
        sb.appendLine("📌 INFORMACIÓN DEL CANAL")
        sb.appendLine("  Nombre:        ${c.name}")
        sb.appendLine("  Handle:        ${c.handle}")
        sb.appendLine("  URL:           ${c.url}")
        sb.appendLine("  ID del canal:  ${c.id}")
        sb.appendLine("  País:          ${c.countryFlag} ${c.country}")
        sb.appendLine("  Creado:        ${FormatUtils.formatDate(c.createdAt)}")
        sb.appendLine()
        sb.appendLine("📊 ESTADÍSTICAS")
        sb.appendLine("  Suscriptores:  ${if (c.hiddenSubscribers) "Ocultos" else FormatUtils.formatFull(c.subscribersRaw)}")
        sb.appendLine("  Videos:        ${FormatUtils.formatFull(c.videosRaw)}")
        sb.appendLine("  Vistas totales:${FormatUtils.formatFull(c.totalViewsRaw)}")
        if (c.topicCategories.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("🏷️  CATEGORÍAS")
            c.topicCategories.forEach { sb.appendLine("  · $it") }
        }
        if (c.keywords.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("🔑 KEYWORDS")
            sb.appendLine("  ${c.keywords.joinToString(", ")}")
        }
        if (c.socialLinks.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("🌐 REDES SOCIALES")
            c.socialLinks.forEach { sb.appendLine("  ${it.icon} ${it.name}: ${it.href}") }
        }
        if (c.description.isNotBlank()) {
            sb.appendLine()
            sb.appendLine("📝 DESCRIPCIÓN")
            sb.appendLine(c.description)
        }
        if (c.videos.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("🎬 VIDEOS RECIENTES")
            c.videos.forEach { v ->
                sb.appendLine("  · ${v.title}")
                sb.appendLine("    ${FormatUtils.formatNumber(v.viewsRaw)} vistas · ${v.duration} · ${FormatUtils.timeAgo(v.publishedAt)}")
                sb.appendLine("    https://youtube.com/watch?v=${v.id}")
            }
        }
        sb.appendLine()
        sb.appendLine("Exportado con YouTube Data App")
        return sb.toString()
    }

    private fun String.escapeJson() = replace("\\", "\\\\").replace("\"", "\\\"")
        .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")
}
