package com.youtubedata.app.data.repository

import com.youtubedata.app.data.api.RetrofitClient
import com.youtubedata.app.data.model.*
import com.youtubedata.app.utils.SocialParser
import com.youtubedata.app.utils.FormatUtils
import java.util.Locale

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

class YoutubeRepository {

    private val api = RetrofitClient.youtubeApi

    // ── Resolve channel ID from any input type ─────────────────────────────

    suspend fun resolveChannelId(input: String, apiKey: String): Result<String> {
        val parsed = parseInput(input)

        return when (parsed.first) {
            "id"     -> Result.Success(parsed.second)
            "handle" -> resolveByHandle(parsed.second, apiKey)
            "video"  -> resolveByVideo(parsed.second, apiKey)
            "name"   -> resolveBySearch(parsed.second, apiKey)
            else     -> Result.Error("No se pudo interpretar el canal.")
        }
    }

    private fun parseInput(raw: String): Pair<String, String> {
        val s = raw.trim()
        if (s.isEmpty()) return "none" to ""

        // youtube.com/@Handle
        Regex("youtube\\.com/@([\\w.\\-]+)", RegexOption.IGNORE_CASE).find(s)?.let {
            return "handle" to it.groupValues[1]
        }
        // youtube.com/channel/UCxxxxx
        Regex("youtube\\.com/channel/(UC[\\w\\-]{22})", RegexOption.IGNORE_CASE).find(s)?.let {
            return "id" to it.groupValues[1]
        }
        // youtube.com/c/name or /user/name
        Regex("youtube\\.com/(?:c|user)/([\\w.\\-]+)", RegexOption.IGNORE_CASE).find(s)?.let {
            return "handle" to it.groupValues[1]
        }
        // youtu.be/VIDEO_ID
        Regex("youtu\\.be/([\\w\\-]{11})", RegexOption.IGNORE_CASE).find(s)?.let {
            return "video" to it.groupValues[1]
        }
        // ?v=VIDEO_ID
        Regex("[?&]v=([\\w\\-]{11})", RegexOption.IGNORE_CASE).find(s)?.let {
            return "video" to it.groupValues[1]
        }
        // shorts/VIDEO_ID
        Regex("youtube\\.com/shorts/([\\w\\-]{11})", RegexOption.IGNORE_CASE).find(s)?.let {
            return "video" to it.groupValues[1]
        }
        // @handle sin URL
        if (s.startsWith("@")) return "handle" to s.substring(1)
        // Channel ID directo
        if (Regex("^UC[\\w\\-]{22}$").matches(s)) return "id" to s

        return "name" to s
    }

    private suspend fun resolveByHandle(handle: String, apiKey: String): Result<String> {
        val atHandle = if (handle.startsWith("@")) handle else "@$handle"
        try {
            val res = api.getChannel(forHandle = atHandle, key = apiKey)
            if (res.isSuccessful) {
                val id = res.body()?.items?.firstOrNull()?.id
                if (!id.isNullOrEmpty()) return Result.Success(id)
            }
        } catch (_: Exception) {}

        // Fallback: forUsername
        try {
            val res = api.getChannel(forUsername = handle.removePrefix("@"), key = apiKey)
            if (res.isSuccessful) {
                val id = res.body()?.items?.firstOrNull()?.id
                if (!id.isNullOrEmpty()) return Result.Success(id)
            }
        } catch (_: Exception) {}

        // Fallback: search
        return resolveBySearch(handle, apiKey)
    }

    private suspend fun resolveByVideo(videoId: String, apiKey: String): Result<String> {
        return try {
            val res = api.getVideoByVideoId(videoId = videoId, key = apiKey)
            val channelId = res.body()?.items?.firstOrNull()?.snippet?.channelId
            if (!channelId.isNullOrEmpty()) Result.Success(channelId)
            else Result.Error("No se pudo obtener el canal de ese video.")
        } catch (e: Exception) {
            Result.Error("Error de red: ${e.message}")
        }
    }

    private suspend fun resolveBySearch(name: String, apiKey: String): Result<String> {
        return try {
            val res = api.searchChannel(query = name, key = apiKey)
            if (!res.isSuccessful) {
                val code = res.code()
                return Result.Error(mapHttpError(code, res.body()?.error))
            }
            val channelId = res.body()?.items?.firstOrNull()?.snippet?.channelId
                ?: res.body()?.items?.firstOrNull()?.id?.channelId
            if (!channelId.isNullOrEmpty()) Result.Success(channelId)
            else Result.Error("No se encontró el canal \"$name\".")
        } catch (e: Exception) {
            Result.Error("Error de red: ${e.message}")
        }
    }

    // ── Fetch full channel data ────────────────────────────────────────────

    suspend fun fetchChannel(channelId: String, apiKey: String): Result<ChannelData> {
        return try {
            val res = api.getChannel(id = channelId, key = apiKey)
            if (!res.isSuccessful) {
                return Result.Error(mapHttpError(res.code(), res.body()?.error))
            }
            val item = res.body()?.items?.firstOrNull()
                ?: return Result.Error("Canal no encontrado o es privado.")

            Result.Success(normaliseChannel(item))
        } catch (e: Exception) {
            Result.Error("Error de red: ${e.message}")
        }
    }

    private fun normaliseChannel(item: ChannelItem): ChannelData {
        val sn = item.snippet ?: ChannelSnippet()
        val st = item.statistics ?: ChannelStatistics()
        val br = item.brandingSettings ?: BrandingSettings()
        val cd = item.contentDetails ?: ContentDetails()
        val td = item.topicDetails ?: TopicDetails()

        val channelId  = item.id
        val customUrl  = sn.customUrl
        val handle     = if (customUrl.startsWith("@")) customUrl else if (customUrl.isNotEmpty()) "@$customUrl" else ""
        val channelUrl = if (handle.isNotEmpty()) "https://www.youtube.com/$handle"
                         else "https://www.youtube.com/channel/$channelId"

        val avatar = sn.thumbnails?.let {
            val base = it.maxres?.url ?: it.high?.url ?: it.medium?.url ?: it.default?.url ?: ""
            // Force max resolution for Google/YouTube avatar URLs
            if (base.contains("=s")) base.replace(Regex("=s\\d+-c"), "=s800-c")
            else base
        } ?: ""

        val banner = (br.image?.bannerExternalUrl ?: "").let {
            if (it.isNotEmpty()) "$it=w2560-nd-rj" else ""
        }

        val countryCode = sn.country
        val (flag, countryName) = formatCountry(countryCode)

        val keywords = parseKeywords(br.channel?.keywords ?: "")

        val topics = (td.topicCategories ?: emptyList()).map { url ->
            url.substringAfterLast("/").replace("_", " ")
        }

        val socialLinks = SocialParser.parse(sn.description)

        return ChannelData(
            id                  = channelId,
            name                = sn.title.ifEmpty { "Canal sin nombre" },
            handle              = handle,
            url                 = channelUrl,
            avatarUrl           = avatar,
            bannerUrl           = banner,
            country             = countryName,
            countryFlag         = flag,
            createdAt           = sn.publishedAt,
            description         = sn.description,
            uploadsPlaylistId   = cd.relatedPlaylists?.uploads ?: "",
            subscribersRaw      = st.subscriberCount.toLongOrNull() ?: 0L,
            videosRaw           = st.videoCount.toLongOrNull() ?: 0L,
            totalViewsRaw       = st.viewCount.toLongOrNull() ?: 0L,
            hiddenSubscribers   = st.hiddenSubscriberCount,
            keywords            = keywords,
            topicCategories     = topics,
            socialLinks         = socialLinks
        )
    }

    private fun parseKeywords(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        val result = mutableListOf<String>()
        val quoted = Regex("\"([^\"]+)\"")
        val remaining = StringBuilder(raw)
        quoted.findAll(raw).forEach {
            result.add(it.groupValues[1])
            remaining.replace(0, remaining.length, remaining.toString().replace(it.value, ""))
        }
        remaining.toString().trim().split("\\s+".toRegex())
            .filter { it.isNotBlank() }
            .forEach { result.add(it) }
        return result
    }

    private fun formatCountry(code: String): Pair<String, String> {
        if (code.isBlank()) return "" to "—"
        return try {
            val flag = code.uppercase().map {
                String(Character.toChars(it.code + 127397))
            }.joinToString("")
            val name = Locale("es", code.uppercase()).displayCountry
            flag to name
        } catch (e: Exception) {
            "" to code.uppercase()
        }
    }

    // ── Fetch videos ─────────────────────────────────────────────────────────

    suspend fun fetchVideos(uploadsPlaylistId: String, apiKey: String): List<VideoData> {
        if (uploadsPlaylistId.isBlank()) return emptyList()

        val playlistRes = try {
            api.getPlaylistItems(playlistId = uploadsPlaylistId, key = apiKey)
        } catch (_: Exception) { return emptyList() }

        val items = playlistRes.body()?.items ?: return emptyList()
        val videoIds = items.mapNotNull {
            it.contentDetails?.videoId ?: it.snippet?.resourceId?.videoId
        }.filter { it.isNotBlank() }

        if (videoIds.isEmpty()) return emptyList()

        val statsRes = try {
            api.getVideosByIds(ids = videoIds.joinToString(","), key = apiKey)
        } catch (_: Exception) { return emptyList() }

        val statsMap = (statsRes.body()?.items ?: emptyList()).associateBy { it.id }

        val videos = videoIds.mapIndexed { idx, videoId ->
            val enriched = statsMap[videoId]
            val sn       = enriched?.snippet ?: items.getOrNull(idx)?.snippet?.let {
                VideoSnippet(title = it.title, publishedAt = it.publishedAt, thumbnails = it.thumbnails)
            } ?: VideoSnippet()
            val stats    = enriched?.statistics ?: VideoStatistics()
            val details  = enriched?.contentDetails ?: VideoContentDetails()

            val thumb = sn.thumbnails?.let {
                it.maxres?.url ?: it.standard?.url ?: it.high?.url ?: it.medium?.url ?: it.default?.url ?: ""
            } ?: ""

            VideoData(
                id           = videoId,
                title        = sn.title,
                thumbUrl     = thumb,
                viewsRaw     = stats.viewCount.toLongOrNull() ?: 0L,
                likesRaw     = stats.likeCount.toLongOrNull() ?: 0L,
                commentsRaw  = stats.commentCount.toLongOrNull() ?: 0L,
                publishedAt  = sn.publishedAt,
                duration     = FormatUtils.parseDuration(details.duration)
            )
        }

        if (videos.isEmpty()) return videos
        val result = videos.toMutableList()
        result[0] = result[0].copy(isLatest = true)
        if (result.size > 1) {
            val maxIdx = result.drop(1).indexOfFirst { v ->
                v.viewsRaw == result.drop(1).maxOf { it.viewsRaw }
            }
            if (maxIdx >= 0) result[maxIdx + 1] = result[maxIdx + 1].copy(isPopular = true)
        }
        return result
    }

    // ── HTTP error mapping ────────────────────────────────────────────────────

    private fun mapHttpError(code: Int, error: ApiError?): String {
        val reason  = error?.errors?.firstOrNull()?.reason ?: ""
        val message = error?.message ?: ""
        return when (code) {
            400  -> "Solicitud inválida: ${message.ifEmpty { "verifica el canal ingresado." }}"
            403  -> if (reason == "quotaExceeded")
                        "Cuota diaria de la API agotada. Inténtalo mañana o usa otra API Key."
                    else
                        "API Key inválida o sin permisos. Verifica en Google Cloud Console. ($reason)"
            404  -> "Canal no encontrado (404)."
            else -> message.ifEmpty { "Error de la API ($code)." }
        }
    }
}
