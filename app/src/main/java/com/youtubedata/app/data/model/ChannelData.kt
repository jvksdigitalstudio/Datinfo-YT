package com.youtubedata.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChannelData(
    val id: String,
    val name: String,
    val handle: String,
    val url: String,
    val avatarUrl: String,
    val bannerUrl: String,
    val country: String,
    val countryFlag: String,
    val createdAt: String,
    val description: String,
    val uploadsPlaylistId: String,
    val subscribersRaw: Long,
    val videosRaw: Long,
    val totalViewsRaw: Long,
    val hiddenSubscribers: Boolean,
    val keywords: List<String>,
    val topicCategories: List<String>,
    val socialLinks: List<SocialLink>,
    val videos: List<VideoData> = emptyList()
) : Parcelable

@Parcelize
data class SocialLink(
    val name: String,
    val icon: String,
    val href: String,
    val handle: String,
    val colorHex: String
) : Parcelable

@Parcelize
data class VideoData(
    val id: String,
    val title: String,
    val thumbUrl: String,
    val viewsRaw: Long,
    val likesRaw: Long,
    val commentsRaw: Long,
    val publishedAt: String,
    val duration: String,
    val isLatest: Boolean = false,
    val isPopular: Boolean = false
) : Parcelable
