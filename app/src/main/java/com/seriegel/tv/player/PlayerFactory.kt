package com.seriegel.tv.player

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.seriegel.tv.downloads.DownloadInfrastructure

class PlayerFactory(
    context: Context,
) {
    private val appContext = context.applicationContext
    private val downloads = DownloadInfrastructure.get(appContext)

    fun create(): ExoPlayer {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()
        val mediaSourceFactory = DefaultMediaSourceFactory(downloads.cacheDataSourceFactory)
        val renderersFactory = DefaultRenderersFactory(appContext)

        return ExoPlayer.Builder(appContext)
            .setMediaSourceFactory(mediaSourceFactory)
            .setRenderersFactory(renderersFactory)
            .build()
            .apply {
                setAudioAttributes(audioAttributes, true)
                playWhenReady = true
            }
    }
}
