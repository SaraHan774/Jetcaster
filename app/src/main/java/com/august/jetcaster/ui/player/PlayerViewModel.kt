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

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.august.jetcaster.data.EpisodeStore
import com.august.jetcaster.data.PodcastStore
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val title: String = "",
    val subTitle: String = "",
    val duration: Duration? = null,
    val podcastName: String = "",
    val author: String = "",
    val summary: String = "",
    val podcastImageUrl: String = ""
)

/**
 * ViewModel that handles the business logic and screen state of the Player screen
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    episodeStore: EpisodeStore,
    podcastStore: PodcastStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // episodeUri should always be present in the PlayerViewModel.
    // If that's not the case, fail crashing the app!

    var uiState by mutableStateOf(PlayerUiState())
        private set

    init {
        val episodeUri: String = Uri.decode(savedStateHandle.get<String>("episodeUri")!!)
        viewModelScope.launch {
            val episode = episodeStore.episodeWithUri(episodeUri).first()
            val podcast = podcastStore.podcastWithUri(episode.podcastUri).first()
            uiState = PlayerUiState(
                title = episode.title,
                duration = episode.duration,
                podcastName = podcast.title,
                summary = episode.summary ?: "",
                podcastImageUrl = podcast.imageUrl ?: ""
            )
        }
    }
}
