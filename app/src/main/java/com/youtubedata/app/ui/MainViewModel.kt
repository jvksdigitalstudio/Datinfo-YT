package com.youtubedata.app.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youtubedata.app.data.model.ChannelData
import com.youtubedata.app.data.repository.Result
import com.youtubedata.app.data.repository.YoutubeRepository
import kotlinx.coroutines.launch

sealed class UiState {
    object Idle    : UiState()
    object Loading : UiState()
    data class Success(val channel: ChannelData) : UiState()
    data class Error(val message: String) : UiState()
}

class MainViewModel : ViewModel() {

    private val repository = YoutubeRepository()

    private val _uiState = MutableLiveData<UiState>(UiState.Idle)
    val uiState: LiveData<UiState> = _uiState

    fun analyzeChannel(input: String, apiKey: String) {
        if (input.isBlank()) {
            _uiState.value = UiState.Error("Ingresa una URL de YouTube, @usuario, o ID de canal.")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            // Step 1: resolve channel ID
            when (val idResult = repository.resolveChannelId(input, apiKey)) {
                is Result.Error   -> { _uiState.value = UiState.Error(idResult.message); return@launch }
                is Result.Success -> {
                    val channelId = idResult.data

                    // Step 2: fetch channel data
                    when (val chanResult = repository.fetchChannel(channelId, apiKey)) {
                        is Result.Error   -> { _uiState.value = UiState.Error(chanResult.message); return@launch }
                        is Result.Success -> {
                            var channel = chanResult.data

                            // Step 3: fetch videos
                            val videos = if (channel.uploadsPlaylistId.isNotBlank()) {
                                repository.fetchVideos(channel.uploadsPlaylistId, apiKey)
                            } else emptyList()

                            channel = channel.copy(videos = videos)
                            _uiState.value = UiState.Success(channel)
                        }
                    }
                }
            }
        }
    }

    fun reset() {
        _uiState.value = UiState.Idle
    }
}
