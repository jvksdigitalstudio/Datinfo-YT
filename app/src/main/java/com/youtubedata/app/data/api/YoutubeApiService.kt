package com.youtubedata.app.data.api

import com.youtubedata.app.data.model.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface YoutubeApiService {

    @GET("channels")
    suspend fun getChannel(
        @Query("part")       part: String = "snippet,statistics,brandingSettings,contentDetails,topicDetails,status",
        @Query("id")         id: String? = null,
        @Query("forHandle")  forHandle: String? = null,
        @Query("forUsername") forUsername: String? = null,
        @Query("maxResults") maxResults: Int = 1,
        @Query("key")        key: String
    ): Response<YoutubeListResponse<ChannelItem>>

    @GET("search")
    suspend fun searchChannel(
        @Query("part")       part: String = "snippet",
        @Query("type")       type: String = "channel",
        @Query("q")          query: String,
        @Query("maxResults") maxResults: Int = 1,
        @Query("key")        key: String
    ): Response<YoutubeListResponse<SearchItem>>

    @GET("videos")
    suspend fun getVideosByIds(
        @Query("part")       part: String = "snippet,statistics,contentDetails",
        @Query("id")         ids: String,
        @Query("key")        key: String
    ): Response<YoutubeListResponse<VideoItem>>

    @GET("videos")
    suspend fun getVideoByVideoId(
        @Query("part")       part: String = "snippet",
        @Query("id")         videoId: String,
        @Query("key")        key: String
    ): Response<YoutubeListResponse<VideoItem>>

    @GET("playlistItems")
    suspend fun getPlaylistItems(
        @Query("part")       part: String = "snippet,contentDetails",
        @Query("playlistId") playlistId: String,
        @Query("maxResults") maxResults: Int = 12,
        @Query("key")        key: String
    ): Response<YoutubeListResponse<PlaylistItemItem>>
}
