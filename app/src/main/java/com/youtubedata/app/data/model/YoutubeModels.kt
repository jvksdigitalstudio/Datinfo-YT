package com.youtubedata.app.data.model

import com.google.gson.annotations.SerializedName

// ── API Response wrappers ─────────────────────────────────────────────────────

data class YoutubeListResponse<T>(
    val items: List<T>? = null,
    val pageInfo: PageInfo? = null,
    val nextPageToken: String? = null,
    val error: ApiError? = null
)

data class PageInfo(
    val totalResults: Int = 0,
    val resultsPerPage: Int = 0
)

data class ApiError(
    val code: Int = 0,
    val message: String = "",
    val errors: List<ApiErrorDetail>? = null
)

data class ApiErrorDetail(
    val reason: String = "",
    val domain: String = "",
    val message: String = ""
)

// ── Channel ───────────────────────────────────────────────────────────────────

data class ChannelItem(
    val id: String = "",
    val snippet: ChannelSnippet? = null,
    val statistics: ChannelStatistics? = null,
    val brandingSettings: BrandingSettings? = null,
    val contentDetails: ContentDetails? = null,
    val topicDetails: TopicDetails? = null,
    val status: ChannelStatus? = null
)

data class ChannelSnippet(
    val title: String = "",
    val description: String = "",
    val customUrl: String = "",
    val publishedAt: String = "",
    val country: String = "",
    val thumbnails: Thumbnails? = null
)

data class ChannelStatistics(
    val subscriberCount: String = "0",
    val videoCount: String = "0",
    val viewCount: String = "0",
    val hiddenSubscriberCount: Boolean = false
)

data class BrandingSettings(
    val image: BrandingImage? = null,
    val channel: BrandingChannel? = null
)

data class BrandingImage(
    val bannerExternalUrl: String = ""
)

data class BrandingChannel(
    val keywords: String = "",
    val featuredChannelsUrls: List<String>? = null
)

data class ContentDetails(
    val relatedPlaylists: RelatedPlaylists? = null
)

data class RelatedPlaylists(
    val uploads: String = ""
)

data class TopicDetails(
    val topicCategories: List<String>? = null
)

data class ChannelStatus(
    val madeForKids: Boolean = false,
    val selfDeclaredMadeForKids: Boolean = false
)

data class Thumbnails(
    val default: Thumbnail? = null,
    val medium: Thumbnail? = null,
    val high: Thumbnail? = null,
    val standard: Thumbnail? = null,
    val maxres: Thumbnail? = null
)

data class Thumbnail(
    val url: String = "",
    val width: Int = 0,
    val height: Int = 0
)

// ── Search ────────────────────────────────────────────────────────────────────

data class SearchItem(
    val id: SearchId? = null,
    val snippet: SearchSnippet? = null
)

data class SearchId(
    val kind: String = "",
    val channelId: String = "",
    val videoId: String = ""
)

data class SearchSnippet(
    val channelId: String = "",
    val title: String = "",
    val description: String = "",
    val thumbnails: Thumbnails? = null
)

// ── Video ─────────────────────────────────────────────────────────────────────

data class VideoItem(
    val id: String = "",
    val snippet: VideoSnippet? = null,
    val statistics: VideoStatistics? = null,
    val contentDetails: VideoContentDetails? = null
)

data class VideoSnippet(
    val title: String = "",
    val description: String = "",
    val publishedAt: String = "",
    val channelId: String = "",
    val thumbnails: Thumbnails? = null,
    val tags: List<String>? = null
)

data class VideoStatistics(
    val viewCount: String = "0",
    val likeCount: String = "0",
    val commentCount: String = "0"
)

data class VideoContentDetails(
    val duration: String = ""
)

// ── PlaylistItem ──────────────────────────────────────────────────────────────

data class PlaylistItemItem(
    val snippet: PlaylistItemSnippet? = null,
    val contentDetails: PlaylistItemContentDetails? = null
)

data class PlaylistItemSnippet(
    val title: String = "",
    val publishedAt: String = "",
    val thumbnails: Thumbnails? = null,
    val resourceId: ResourceId? = null
)

data class ResourceId(
    val kind: String = "",
    val videoId: String = ""
)

data class PlaylistItemContentDetails(
    val videoId: String = ""
)
