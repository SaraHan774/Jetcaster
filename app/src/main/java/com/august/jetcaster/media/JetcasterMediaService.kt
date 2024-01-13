package com.august.jetcaster.media

import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.august.jetcaster.di.MediaModule

class JetcasterMediaService : MediaSessionService() {

    private lateinit var mediaSession: MediaSession
    private lateinit var player: Player
    private lateinit var notificationManager: NotificationManager

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
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = mediaSession
}