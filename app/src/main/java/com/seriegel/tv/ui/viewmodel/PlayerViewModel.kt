package com.seriegel.tv.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.seriegel.tv.TvApplication
import com.seriegel.tv.core.config.ServerConfig
import com.seriegel.tv.domain.model.ProgressUpdate
import com.seriegel.tv.player.PlaybackRequest
import java.net.URI
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerUiState(
    val request: PlaybackRequest? = null,
    val showNextEpisodePrompt: Boolean = false,
    val nextEpisodeCountdown: Int = 10,
    val errorMessage: String? = null,
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as TvApplication).appContainer
    private val playerFactory = container.playerFactory
    private val coordinator = container.playbackCoordinator
    private val catalogRepository = container.catalogRepository

    val player: ExoPlayer = playerFactory.create()

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var progressJob: Job? = null
    private var countdownJob: Job? = null

    init {
        viewModelScope.launch {
            coordinator.currentRequest.collect { request ->
                _uiState.update { it.copy(request = request, showNextEpisodePrompt = false, nextEpisodeCountdown = 10) }
                if (request != null) {
                    startPlayback(request)
                }
            }
        }
        setupPlayerListener()
    }

    private fun startPlayback(request: PlaybackRequest) {
        viewModelScope.launch {
            val resume = catalogRepository.fetchEpisodeProgress(request.seriesId, request.episodeId).getOrNull()
            player.setMediaItem(MediaItem.fromUri(Uri.parse(request.streamUrl)))
            player.prepare()
            if ((resume?.timeSeconds ?: 0.0) > 5.0) {
                player.seekTo((resume?.timeSeconds ?: 0.0).toLong() * 1000L)
            }
            player.playWhenReady = true
            startPeriodicProgressSaves()
        }
    }

    private fun setupPlayerListener() {
        player.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        maybePromptNextEpisode()
                    }
                }
            },
        )
    }

    private fun maybePromptNextEpisode() {
        val request = _uiState.value.request ?: return
        val next = request.nextEpisode ?: return
        _uiState.update { it.copy(showNextEpisodePrompt = true, nextEpisodeCountdown = 10) }
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var count = 10
            while (isActive && count > 0) {
                delay(1000)
                count--
                _uiState.update { it.copy(nextEpisodeCountdown = count) }
            }
            if (count == 0) {
                cancelNextEpisodePrompt()
                container.playbackCoordinator.playEpisode(
                    seriesId = request.seriesId,
                    episode = next,
                    streamUrl = ServerConfig.streamUrl(next.urlPath),
                    nextEpisode = null,
                )
            }
        }
    }

    fun playNextNow() {
        val request = _uiState.value.request ?: return
        val next = request.nextEpisode ?: return
        cancelNextEpisodePrompt()
        container.playbackCoordinator.playEpisode(
            seriesId = request.seriesId,
            episode = next,
            streamUrl = ServerConfig.streamUrl(next.urlPath),
            nextEpisode = null,
        )
    }

    fun cancelNextEpisodePrompt() {
        countdownJob?.cancel()
        _uiState.update { it.copy(showNextEpisodePrompt = false, nextEpisodeCountdown = 10) }
    }

    private fun startPeriodicProgressSaves() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                delay(15000)
                saveProgressSnapshot()
            }
        }
    }

    fun saveProgressSnapshot() {
        val request = _uiState.value.request ?: return
        val position = player.currentPosition.coerceAtLeast(0L) / 1000.0
        val duration = player.duration.takeIf { it > 0 }?.div(1000.0) ?: 0.0
        viewModelScope.launch {
            catalogRepository.saveProgress(
                ProgressUpdate(
                    seriesId = request.seriesId,
                    episodeId = request.episodeId,
                    timeSeconds = position,
                    durationSeconds = duration,
                    episodeTitle = request.episodeTitle,
                    urlPath = request.streamUrl.toRelativeUrlPath(),
                ),
            )
        }
    }

    override fun onCleared() {
        saveProgressSnapshot()
        progressJob?.cancel()
        countdownJob?.cancel()
        player.release()
        super.onCleared()
    }
}

private fun String.toRelativeUrlPath(): String {
    return runCatching {
        URI(this).path ?: this
    }.getOrDefault(this)
}
