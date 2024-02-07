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

package com.august.jetcaster.ui.home.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.august.jetcaster.data.CategoryStore
import com.august.jetcaster.data.Episode
import com.august.jetcaster.data.EpisodeToPodcast
import com.august.jetcaster.data.PodcastStore
import com.august.jetcaster.data.PodcastWithExtraInfo
import com.august.jetcaster.media.MediaBus
import com.august.jetcaster.media.MediaEvent
import com.august.jetcaster.media.PlayerState
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class PodcastCategoryViewModel @AssistedInject constructor(
    @Assisted val categoryId: Long,
    categoryStore: CategoryStore,
    val podcastStore: PodcastStore,
) : ViewModel() {
    private val _state = MutableStateFlow(PodcastCategoryViewState())

    val state: StateFlow<PodcastCategoryViewState>
        get() = _state

    init {
        viewModelScope.launch {
            val recentPodcastsFlow = categoryStore.podcastsInCategorySortedByPodcastCount(
                categoryId,
                limit = 10
            )

            val episodesFlow = categoryStore.episodesFromPodcastsInCategory(
                categoryId,
                limit = 20
            )
            // Combine our flows and collect them into the view state StateFlow
            combine(recentPodcastsFlow, episodesFlow, MediaBus.state) { topPodcasts, episodes, mediaState ->
                PodcastCategoryViewState(
                    topPodcasts = topPodcasts,
                    episodes = episodes.map {
                        val isCurrentItem = mediaState.mediaItem.displayTitle == it.episode.title
                        EpisodeListItemState(
                            episodeToPodcast = it,
                            isPlaying = isCurrentItem && mediaState.isPlaying,
                            isLoading = isCurrentItem && mediaState.playerState == PlayerState.BUFFERING,
                        )
                    }
                )
            }.collect { _state.value = it }
        }
    }

    fun onPlayEpisode(episode: Episode) {
        if (state.value.isSelectedItem(episode.title)) {
            onMediaEvent(MediaEvent.SetItem(episode.uri))
        } else {
            onMediaEvent(MediaEvent.PlayPause)
        }
    }

    fun onTogglePodcastFollowed(podcastUri: String) {
        viewModelScope.launch {
            podcastStore.togglePodcastFollowed(podcastUri)
        }
    }

    private fun onMediaEvent(mediaEvent: MediaEvent) {
        MediaBus.sendEvent(mediaEvent)
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun provideFactory(
            assistedFactory: PodcastCategoryViewModelFactory,
            categoryId: Long
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return assistedFactory.create(categoryId) as T
                }
            }
        }
    }
}

data class PodcastCategoryViewState(
    val topPodcasts: List<PodcastWithExtraInfo> = emptyList(),
    val episodes: List<EpisodeListItemState> = emptyList(),
)

private fun PodcastCategoryViewState.isSelectedItem(title: String) = episodes.any { it.episodeToPodcast.episode.title == title }

data class EpisodeListItemState(
    val episodeToPodcast: EpisodeToPodcast,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
)
