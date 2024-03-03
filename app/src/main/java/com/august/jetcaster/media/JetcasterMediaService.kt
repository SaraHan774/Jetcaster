package com.august.jetcaster.media

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.august.jetcaster.data.EpisodeStore
import com.august.jetcaster.data.PodcastStore
import com.august.jetcaster.di.modules.ServiceScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class JetcasterMediaService : MediaSessionService(), Player.Listener {

    @Inject
    lateinit var mediaSession: MediaSession

    @Inject
    lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var episodeStore: EpisodeStore

    @Inject
    lateinit var podcastStore: PodcastStore

    @ServiceScope
    @Inject
    lateinit var coroutineScope: CoroutineScope

    private lateinit var player: Player

    private var positionUpdateJob: Job? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        init()
    }

    private fun init() {
        player = mediaSession.player
        player.addListener(this)
    }

    @UnstableApi
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationManager.startNotificationService(
            mediaSessionService = this,
            mediaSession = mediaSession
        )
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (player.playWhenReady) {
            player.pause()
        }
        stopSelf()
    }

    override fun onDestroy() {
        mediaSession.run {
            player.removeListener(this@JetcasterMediaService)
            player.release()
            release()
        }
        coroutineScope.cancel()
        // Temporary: Reset media state
        MediaBus.updateState { MediaState.INITIAL }
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession


    /* Playback.Listener Callbacks */

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        if (isPlaying) startPositionUpdate()
        else stopPositionUpdate()
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        super.onMediaMetadataChanged(mediaMetadata)
        MediaBus.updateState {
            it.copy(mediaItem = mediaMetadata)
        }
    }

    override fun onEvents(player: Player, events: Player.Events) {
        super.onEvents(player, events)
        when (player.playbackState) {
            Player.STATE_IDLE -> MediaBus.updateState {
                it.copy(playerState = PlayerState.IDLE)
            }

            Player.STATE_BUFFERING -> MediaBus.updateState {
                it.copy(
                    playerState = PlayerState.BUFFERING,
                    position = player.currentPosition
                )
            }

            Player.STATE_READY -> MediaBus.updateState {
                it.copy(
                    playerState = PlayerState.READY,
                    position = player.currentPosition,
                    duration = player.duration,
                    isPlaying = player.isPlaying
                )
            }

            Player.STATE_ENDED -> {
                MediaBus.updateState {
                    it.copy(playerState = PlayerState.ENDED)
                }
                player.seekToDefaultPosition(0)
                player.pause()
            }
        }
    }


    /* Private Functions */

    private fun startPositionUpdate() {
        if (positionUpdateJob != null) return
        positionUpdateJob = coroutineScope.launch {
            while (isActive) {
                delay(500)
                MediaBus.updateState {
                    it.copy(
                        position = player.currentPosition,
                        isPlaying = true
                    )
                }
            }
        }
    }

    private fun stopPositionUpdate() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
        MediaBus.updateState {
            it.copy(
                position = player.currentPosition,
                isPlaying = false
            )
        }
    }
}