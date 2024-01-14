package com.august.jetcaster.media

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object MediaBus {

    private val _events = MutableSharedFlow<MediaEvent>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events = _events.asSharedFlow()

    private val _state = MutableStateFlow(MediaState.INITIAL)
    val state = _state.asStateFlow()

    fun sendEvent(event: MediaEvent) {
        _events.tryEmit(event)
    }

    fun updateState(newState: MediaState) {
        _state.update { newState }
    }

    fun updateState(function: (MediaState) -> MediaState) {
        _state.update(function)
    }
}

sealed interface MediaEvent {
    data class SetItem(val uri: String) : MediaEvent

    data class AddItem(val uri: String) : MediaEvent

    data object PlayPause : MediaEvent

    data object SeekBack : MediaEvent

    data object SeekForward : MediaEvent

    data class SeekTo(val positionAsPercentage: Float) : MediaEvent

    data object SkipPrev : MediaEvent

    data object SkipNext : MediaEvent

}

data class MediaState(
    val playerState: PlayerState,
    val mediaItem: MediaMetadata,
    val duration: Long,
    val position: Long,
    val isPlaying: Boolean
) {

    companion object {
        val INITIAL = MediaState(
            playerState = PlayerState.IDLE,
            mediaItem = MediaItem.EMPTY.mediaMetadata,
            duration = 0L,
            position = 0L,
            isPlaying = false
        )
    }
}

enum class PlayerState {
    IDLE, BUFFERING, READY, ENDED
}