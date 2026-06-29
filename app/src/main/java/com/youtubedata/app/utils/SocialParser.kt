package com.youtubedata.app.utils

import com.youtubedata.app.data.model.SocialLink

object SocialParser {

    data class Platform(
        val name: String,
        val icon: String,
        val colorHex: String,
        val regex: Regex,
        val buildHref: (String, String) -> String,
        val buildHandle: (String) -> String = { g1 -> g1 }
    )

    private val PLATFORMS = listOf(
        Platform("Instagram",  "📸", "#E1306C",
            Regex("(?:https?://)?(?:www\\.)?instagram\\.com/([\\w.]+)", RegexOption.IGNORE_CASE),
            { _, g1 -> "https://instagram.com/$g1" }
        ),
        Platform("TikTok",     "🎵", "#ff0050",
            Regex("(?:https?://)?(?:www\\.)?tiktok\\.com/@([\\w.]+)", RegexOption.IGNORE_CASE),
            { _, g1 -> "https://tiktok.com/@$g1" }
        ),
        Platform("X / Twitter","🐦", "#1DA1F2",
            Regex("(?:https?://)?(?:www\\.)?(?:twitter|x)\\.com/([\\w]+)", RegexOption.IGNORE_CASE),
            { _, g1 -> "https://x.com/$g1" }
        ),
        Platform("Facebook",   "👥", "#1877F2",
            Regex("(?:https?://)?(?:www\\.)?facebook\\.com/([\\w.]+)", RegexOption.IGNORE_CASE),
            { _, g1 -> "https://facebook.com/$g1" }
        ),
        Platform("Discord",    "💬", "#5865F2",
            Regex("(?:https?://)?(?:www\\.)?discord\\.(?:gg|com/invite)/([\\w-]+)", RegexOption.IGNORE_CASE),
            { _, g1 -> "https://discord.gg/$g1" }
        ),
        Platform("Telegram",   "✈️", "#229ED9",
            Regex("(?:https?://)?(?:www\\.)?t\\.me/([\\w]+)", RegexOption.IGNORE_CASE),
            { _, g1 -> "https://t.me/$g1" }
        ),
        Platform("Twitch",     "🎮", "#9147FF",
            Regex("(?:https?://)?(?:www\\.)?twitch\\.tv/([\\w]+)", RegexOption.IGNORE_CASE),
            { _, g1 -> "https://twitch.tv/$g1" }
        ),
        Platform("Kick",       "🟢", "#53FC18",
            Regex("(?:https?://)?(?:www\\.)?kick\\.com/([\\w]+)", RegexOption.IGNORE_CASE),
            { _, g1 -> "https://kick.com/$g1" }
        ),
        Platform("Patreon",    "🎁", "#FF424D",
            Regex("(?:https?://)?(?:www\\.)?patreon\\.com/([\\w]+)", RegexOption.IGNORE_CASE),
            { _, g1 -> "https://patreon.com/$g1" }
        ),
        Platform("Ko-fi",      "☕", "#FF5E5B",
            Regex("(?:https?://)?(?:www\\.)?ko-fi\\.com/([\\w]+)", RegexOption.IGNORE_CASE),
            { _, g1 -> "https://ko-fi.com/$g1" }
        ),
        Platform("GitHub",     "💻", "#f0f0f0",
            Regex("(?:https?://)?(?:www\\.)?github\\.com/([\\w-]+)", RegexOption.IGNORE_CASE),
            { _, g1 -> "https://github.com/$g1" }
        ),
        Platform("Spotify",    "🎧", "#1DB954",
            Regex("(?:https?://)?(?:open\\.)?spotify\\.com/(artist|user|show)/([\\w]+)", RegexOption.IGNORE_CASE),
            { raw, _ -> if (raw.startsWith("http")) raw else "https://$raw" }
        ),
        Platform("Gmail",      "✉️", "#EA4335",
            Regex("[\\w.%+\\-]+@gmail\\.com", RegexOption.IGNORE_CASE),
            { raw, _ -> "mailto:$raw" },
            { raw -> raw }
        ),
        Platform("Email",      "📩", "#aaaaaa",
            Regex("[\\w.%+\\-]+@[\\w.\\-]+\\.[a-z]{2,}", RegexOption.IGNORE_CASE),
            { raw, _ -> "mailto:$raw" },
            { raw -> raw }
        ),
        Platform("Sitio web",  "🌐", "#22d98b",
            Regex("https?://(?!(?:www\\.)?(?:youtube|instagram|facebook|twitter|x\\.com|tiktok|discord|t\\.me|wa\\.me|whatsapp|github|patreon|ko-fi|twitch|kick|linkedin|spotify|open\\.spotify))[\\w./\\-?=&%#+~@!:]+", RegexOption.IGNORE_CASE),
            { raw, _ -> raw },
            { raw -> raw.removePrefix("https://").removePrefix("http://").removePrefix("www.").take(32) }
        ),
    )

    fun parse(description: String): List<SocialLink> {
        if (description.isBlank()) return emptyList()
        val found     = mutableListOf<SocialLink>()
        val seenHrefs = mutableSetOf<String>()

        for (platform in PLATFORMS) {
            platform.regex.findAll(description).forEach { match ->
                val raw = match.value
                val g1  = if (match.groupValues.size > 1) match.groupValues[1] else raw
                val href = platform.buildHref(raw, g1)
                if (href.isBlank() || seenHrefs.contains(href.lowercase())) return@forEach
                seenHrefs.add(href.lowercase())
                val handle = platform.buildHandle(g1).take(32)
                found.add(SocialLink(
                    name     = platform.name,
                    icon     = platform.icon,
                    href     = href,
                    handle   = handle,
                    colorHex = platform.colorHex
                ))
            }
        }
        return found
    }
}
