package com.august.jetcaster.media

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object MediaBus {

    private val _events = MutableStateFlow<MediaEvent>(MediaEvent.Unit)
    val events = _events.asStateFlow()

    private val _state = MutableStateFlow<MediaState>(MediaState.Idle)
    val state = _state.asStateFlow()

    fun sendEvent(event: MediaEvent) {
        _events.update { event }
    }
}

sealed interface MediaEvent {
    data object Unit : MediaEvent

    data class SetItem(val uri: String) : MediaEvent

    data object PlayPause : MediaEvent

    data object SeekBack : MediaEvent

    data object SeekForward : MediaEvent

    data class SeekTo(val positionAsPercentage: Float) : MediaEvent
}

sealed interface MediaState {
    data object Idle : MediaState
}