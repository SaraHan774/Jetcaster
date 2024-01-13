package com.august.jetcaster.media

import android.content.Intent
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.august.jetcaster.Graph
import com.august.jetcaster.di.MediaModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class JetcasterMediaService : MediaSessionService() {

    private lateinit var mediaSession: MediaSession
    private lateinit var player: Player
    private lateinit var notificationManager: NotificationManager

    // NOTE: To be injected by Hilt
    private val episodeStore = Graph.episodeStore
    private val podcastStore = Graph.podcastStore
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaModule.provideMediaSession(this)
        player = mediaSession.player
        notificationManager = NotificationManager(this, mediaSession.player)
    }

    @UnstableApi
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        notificationManager.startNotificationService(
            mediaSessionService = this,
            mediaSession = mediaSession
        )
        setupEventListener()
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
            player.release()
            release()
        }
        coroutineScope.cancel()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession

    private fun setupEventListener() {
        coroutineScope.launch {
            MediaBus.events.collect {
                when (it) {
                    MediaEvent.PlayPause -> playPause()
                    MediaEvent.SeekBack -> TODO()
                    MediaEvent.SeekForward -> TODO()
                    is MediaEvent.SeekTo -> TODO()
                    is MediaEvent.SetItem -> setAndPlayItem(it.uri)
                    MediaEvent.Unit -> {}
                }
            }
        }
    }

    private fun playPause() {
        if (player.isPlaying) player.pause()
        else player.play()
    }

    private fun setAndPlayItem(uri: String) = coroutineScope.launch {
        // Fetch info
        val episode = episodeStore.episodeWithUri(uri).first()
        val podcast = podcastStore.podcastWithUri(episode.podcastUri).first()

        if (episode.url == null) {
            // TODO: Error handling
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
                    .build()
            ).build()

        // Set media item
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }
}