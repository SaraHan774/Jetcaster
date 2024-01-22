/*
 * Copyright 2020 The Android Open Source Project
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

package com.august.jetcaster.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.august.jetcaster.data.PodcastStore
import com.august.jetcaster.data.PodcastWithExtraInfo
import com.august.jetcaster.data.PodcastsRepository
import com.august.jetcaster.media.MediaBus
import com.august.jetcaster.media.MediaEvent
import com.august.jetcaster.media.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val podcastsRepository: PodcastsRepository,
    private val podcastStore: PodcastStore,
) : ViewModel() {
    // Holds our currently selected home category
    private val selectedCategory = MutableStateFlow(HomeCategory.Discover)

    // Holds the currently available home categories
    private val categories = MutableStateFlow(HomeCategory.entries)

    // Holds our view state which the UI collects via [state]
    private val _state = MutableStateFlow(HomeViewState())

    private val refreshing = MutableStateFlow(false)

    val state: StateFlow<HomeViewState>
        get() = _state

    private val _playerBarUiState = MutableStateFlow(PlayerBarUiState.IDLE)
    val playerBarState = _playerBarUiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Combines the latest value from each of the flows, allowing us to generate a
            // view state instance which only contains the latest values.
            combine(
                categories,
                selectedCategory,
                podcastStore.followedPodcastsSortedByLastEpisode(limit = 20),
                refreshing
            ) { categories, selectedCategory, podcasts, refreshing ->
                HomeViewState(
                    homeCategories = categories,
                    selectedHomeCategory = selectedCategory,
                    featuredPodcasts = podcasts.toPersistentList(),
                    refreshing = refreshing,
                    errorMessage = null /* TODO */
                )
            }.catch { throwable ->
                // TODO: emit a UI error here. For now we'll just rethrow
                throw throwable
            }.collect {
                _state.value = it
            }
        }

        collectMediaState()

        refresh(force = false)
    }

    private fun refresh(force: Boolean) {
        viewModelScope.launch {
            runCatching {
                refreshing.value = true
                podcastsRepository.updatePodcasts(force)
            }
            // TODO: look at result of runCatching and show any errors

            refreshing.value = false
        }
    }

    fun onHomeCategorySelected(category: HomeCategory) {
        selectedCategory.value = category
    }

    fun onPodcastUnfollowed(podcastUri: String) {
        viewModelScope.launch {
            podcastStore.unfollowPodcast(podcastUri)
        }
    }

    fun onMediaEvent(event: MediaEvent) {
        MediaBus.sendEvent(event)
    }

    private fun collectMediaState() {
        viewModelScope.launch {
            MediaBus.state.collect { mediaState ->
                _playerBarUiState.update {
                    it.copy(
                        title = mediaState.mediaItem.displayTitle.toString(),
                        podcastImageUrl = mediaState.mediaItem.artworkUri?.toString() ?: "",
                        isPlaying = mediaState.isPlaying,
                        isBuffering = mediaState.playerState == PlayerState.BUFFERING,
                        isIdle = mediaState.playerState == PlayerState.IDLE
                    )
                }
            }
        }
    }
}

enum class HomeCategory {
    Library, Discover
}

data class HomeViewState(
    val featuredPodcasts: PersistentList<PodcastWithExtraInfo> = persistentListOf(),
    val refreshing: Boolean = false,
    val selectedHomeCategory: HomeCategory = HomeCategory.Discover,
    val homeCategories: List<HomeCategory> = emptyList(),
    val errorMessage: String? = null
)

data class PlayerBarUiState(
    val title: String,
    val podcastImageUrl: String,
    val isPlaying: Boolean,
    val isBuffering: Boolean,
    val isIdle: Boolean,
) {
    companion object {
        val IDLE = PlayerBarUiState(
            title = "",
            podcastImageUrl = "",
            isPlaying = false,
            isBuffering = false,
            isIdle = true
        )
    }
}
