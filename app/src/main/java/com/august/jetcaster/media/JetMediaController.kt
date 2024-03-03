package com.august.jetcaster.media

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.august.jetcaster.data.EpisodeStore
import com.august.jetcaster.data.PodcastStore
import com.august.jetcaster.di.modules.IODispatcher
import com.august.jetcaster.di.modules.MainDispatcher
import com.august.jetcaster.di.modules.MediaControllerScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.guava.asDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class JetMediaController @Inject constructor(
    @ApplicationContext private val context: Context,
    @MediaControllerScope private val coroutineScope: CoroutineScope,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher,
    private val episodeStore: EpisodeStore,
    private val podcastStore: PodcastStore
) {

    private var mediaController: MediaController? = null

    init {
        buildMediaController()
    }

    fun onMediaEvent(event: MediaEvent) {
        when (event) {
            MediaEvent.PlayPause -> playPause()
            MediaEvent.SeekBack -> seekBack()
            MediaEvent.SeekForward -> seekForward()
            is MediaEvent.SeekTo -> seekTo(event.positionAsPercentage)
            is MediaEvent.SetItem -> setAndPlayItem(event.uri)
            else -> {
                toBeImplemented()
            }
        }
    }

    fun cleanup() {
        mediaController?.release()
        coroutineScope.cancel()
    }

    private fun buildMediaController() {
        val componentName = ComponentName(context, JetcasterMediaService::class.java)
        val sessionToken = SessionToken(context, componentName)
        val asyncMc = MediaController.Builder(context, sessionToken)
            .buildAsync()
            .asDeferred()

        coroutineScope.launch {
            mediaController = asyncMc.await()
        }
    }

    private fun playPause() = executeAfterCheck { player ->
        if (player.isPlaying) player.pause()
        else player.play()
    }

    private fun seekForward() = executeAfterCheck { player -> player.seekForward() }

    private fun seekBack() = executeAfterCheck { player -> player.seekBack() }

    private fun seekTo(positionAsPercentage: Float) = executeAfterCheck { player ->
        val position = (player.duration * positionAsPercentage).toLong()
        player.seekTo(position)
    }

    private fun toBeImplemented() {
        Toast.makeText(context, "To be Implemented", Toast.LENGTH_SHORT).show()
    }

    private fun setAndPlayItem(uri: String) = executeAfterCheck { player ->
        coroutineScope.launch(ioDispatcher) {
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

            withContext(mainDispatcher) {
                // Set media item
                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()
            }
        }
    }

    private inline fun executeAfterCheck(crossinline action: (MediaController) -> Unit) {
        if (mediaController == null) {
            Timber.w("MediaController is not initialized")
            return
        }

        val controller = mediaController ?: return

        if (!controller.isConnected) {
            Timber.w("MediaController is not connected")
            return
        }

        action(controller)
    }
}