package com.august.jetcaster.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.session.MediaSession

// NOTE: Later should be part be a Hilt Module.

object MediaModule {

    @UnstableApi
    fun provideMediaSession(
        context: Context,
//        player: ExoPlayer
    ): MediaSession = MediaSession.Builder(context, providePlayer(context)).build()

    @UnstableApi
    private fun providePlayer(
        context: Context,
//        audioAttributes: AudioAttributes
    ): ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(provideAudioAttributes(), true)
        .setHandleAudioBecomingNoisy(true)
        .setTrackSelector(DefaultTrackSelector(context))
        .build()

    private fun provideAudioAttributes(): AudioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
        .setUsage(C.USAGE_MEDIA)
        .build()
}