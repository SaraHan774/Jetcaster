package com.august.jetcaster.media

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object MediaBus {

    private val _events = MutableSharedFlow<MediaEvent>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events = _events.asSharedFlow()

    private val _state = MutableSharedFlow<MediaState>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val state = _state.asSharedFlow()

    fun sendEvent(event: MediaEvent) {
        _events.tryEmit(event)
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

sealed interface MediaState {
    data object Idle : MediaState
}