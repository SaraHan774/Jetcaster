package com.august.jetcaster.media

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
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
import kotlinx.coroutines.flow.first
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
        setupEventListener()
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

    private fun setupEventListener() {
        coroutineScope.launch {
            MediaBus.events.collect {
                when (it) {
                    MediaEvent.PlayPause -> playPause()
                    MediaEvent.SeekBack -> seekBack()
                    MediaEvent.SeekForward -> seekForward()
                    is MediaEvent.SeekTo -> seekTo(it.positionAsPercentage)
                    is MediaEvent.SetItem -> setAndPlayItem(it.uri)
                    else -> {
                        toBeImplemented()
                    }
                }
            }
        }
    }

    private fun playPause() {
        if (player.isPlaying) player.pause()
        else player.play()
    }

    private fun seekForward() = player.seekForward()

    private fun seekBack() = player.seekBack()

    private fun seekTo(positionAsPercentage: Float) {
        val position = (player.duration * positionAsPercentage).toLong()
        player.seekTo(position)
    }

    private fun toBeImplemented() {
        Toast.makeText(this, "To be Implemented", Toast.LENGTH_SHORT).show()
    }

    private fun setAndPlayItem(uri: String) = coroutineScope.launch {
        // Fetch info
        val episode = episodeStore.episodeWithUri(uri).first()
        val podcast = podcastStore.podcastWithUri(episode.podcastUri).first()

        if (episode.url == null) {
            // TODO: Error handling
            return@launch
        }

        // Prepare media item
        val mediaItem = MediaItem.Builder()
            .setUri(episode.url)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setMediaType(MediaMetadata.MEDIA_TYPE_PODCAST_EPISODE)
                    .setArtworkUri(Uri.parse(podcast.imageUrl))
                    .setAlbumTitle(podcast.title)
                    .setDisplayTitle(episode.title)
                    .setDescription(episode.summary)
                    .build()
            ).build()

        // Set media item
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

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