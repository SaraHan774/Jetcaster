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
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PodcastCategoryViewModel @AssistedInject constructor(
    @Assisted val categoryId: Long,
    categoryStore: CategoryStore,
    val podcastStore: PodcastStore,
) : ViewModel() {
    private val _state = MutableStateFlow(PodcastCategoryViewState())

    val state: StateFlow<PodcastCategoryViewState>
        get() = _state


    private val _selectedEpisode = MutableSharedFlow<SelectedEpisode>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val selectedEpisode = _selectedEpisode


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
            combine(recentPodcastsFlow, episodesFlow) { topPodcasts, episodes ->
                PodcastCategoryViewState(
                    topPodcasts = topPodcasts,
                    episodes = episodes
                )
            }.collect { _state.value = it }
        }

        viewModelScope.launch {
            selectedEpisode.map { it.episode }.collect { episode ->
                // FIXME : 이렇게 비교하는건 불안정해 보이는데 다른 방법 찾아야 한다
                if (episode?.title != MediaBus.state.value.mediaItem.displayTitle) {
                    if (episode != null) onMediaEvent(MediaEvent.SetItem(uri = episode.uri))
                } else {
                    onMediaEvent(MediaEvent.PlayPause)
                }
            }
        }
    }

    fun onPlayEpisode(selectedEpisode: SelectedEpisode) {
        _selectedEpisode.tryEmit(selectedEpisode)
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
    val episodes: List<EpisodeToPodcast> = emptyList(),
)

data class SelectedEpisode(
    val episode: Episode? = null,
    val isPlaying: Boolean = false,
) {
    companion object {
        val NONE = SelectedEpisode()
    }
}

fun SelectedEpisode?.isPlayingItem(item: Episode) = this?.episode?.uri == item.uri && this.isPlaying
