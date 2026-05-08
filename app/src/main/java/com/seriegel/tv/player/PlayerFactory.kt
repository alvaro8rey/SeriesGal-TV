package com.seriegel.tv.player

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer

class PlayerFactory(
    context: Context,
) {
    private val appContext = context.applicationContext

    fun create(): ExoPlayer {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        return ExoPlayer.Builder(appContext)
            .build()
            .apply {
                setAudioAttributes(audioAttributes, true)
            }
    }
}
