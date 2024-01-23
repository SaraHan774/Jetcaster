/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.august.jetcaster.ui.player

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.august.jetcaster.media.MediaBus
import com.august.jetcaster.media.MediaEvent
import com.august.jetcaster.media.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val title: String = "",
    val subTitle: String = "",
    val position: Long = 0L,
    val duration: Long = 0L,
    val podcastName: String = "",
    val author: String = "",
    val summary: String = "",
    val podcastImageUrl: String = "",
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val isLoading: Boolean = false,
)

/**
 * ViewModel that handles the business logic and screen state of the Player screen
 */
@HiltViewModel
class PlayerViewModel @Inject constructor() : ViewModel() {

    var uiState by mutableStateOf(PlayerUiState(isLoading = true))
        private set

    init {
        viewModelScope.launch {
            MediaBus.state.collect {
                uiState = PlayerUiState(
                    title = it.mediaItem.displayTitle.toString(),
                    podcastName = it.mediaItem.albumTitle.toString(),
                    podcastImageUrl = it.mediaItem.artworkUri?.toString() ?: "",
                    summary = it.mediaItem.description.toString(),
                    duration = it.duration,
                    position = it.position,
                    isPlaying = it.isPlaying,
                    isBuffering = it.playerState == PlayerState.BUFFERING
                )
            }
        }
    }

    fun onMediaEvent(event: MediaEvent) {
        MediaBus.sendEvent(event)
    }
}
