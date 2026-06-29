package com.youtubedata.app.utils

import android.content.Context
import androidx.core.content.edit

object PrefsManager {

    private const val PREFS_NAME = "youtube_data_prefs"
    private const val KEY_API_KEY = "yt_api_key"

    fun getApiKey(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_API_KEY, "") ?: ""
    }

    fun saveApiKey(context: Context, key: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(KEY_API_KEY, key.trim()) }
    }

    fun clearApiKey(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { remove(KEY_API_KEY) }
    }

    fun hasApiKey(context: Context) = getApiKey(context).length > 10
}
