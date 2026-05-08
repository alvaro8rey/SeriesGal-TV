package com.seriegel.tv.ui.screens

import android.app.Activity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Button
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import com.seriegel.tv.ui.viewmodel.PlayerViewModel

@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    viewModel: PlayerViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val continueFocusRequester = remember { FocusRequester() }
    val activity = LocalContext.current as? Activity
    var shouldKeepScreenOn by remember {
        mutableStateOf(
            viewModel.player.playWhenReady &&
                viewModel.player.playbackState != Player.STATE_IDLE &&
                viewModel.player.playbackState != Player.STATE_ENDED,
        )
    }

    LaunchedEffect(state.showNextEpisodePrompt) {
        if (state.showNextEpisodePrompt) {
            continueFocusRequester.requestFocus()
        }
    }

    BackHandler {
        viewModel.saveProgressSnapshot()
        onBack()
    }

    DisposableEffect(viewModel.player) {
        val listener =
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    shouldKeepScreenOn =
                        viewModel.player.playWhenReady &&
                            playbackState != Player.STATE_IDLE &&
                            playbackState != Player.STATE_ENDED
                }

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    shouldKeepScreenOn =
                        playWhenReady &&
                            viewModel.player.playbackState != Player.STATE_IDLE &&
                            viewModel.player.playbackState != Player.STATE_ENDED
                }
            }
        viewModel.player.addListener(listener)
        onDispose {
            viewModel.player.removeListener(listener)
        }
    }

    DisposableEffect(activity, shouldKeepScreenOn) {
        if (shouldKeepScreenOn) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.saveProgressSnapshot()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = viewModel.player
                    useController = true
                    layoutParams = android.view.ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { playerView ->
                playerView.player = viewModel.player
                playerView.useController = !state.showNextEpisodePrompt
                if (state.showNextEpisodePrompt) {
                    playerView.hideController()
                }
            },
        )

        if (state.showNextEpisodePrompt) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f)),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(androidx.compose.ui.Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.78f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Siguiente episodio en ${state.nextEpisodeCountdown}s",
                    modifier = Modifier.fillMaxWidth(0.48f),
                    color = Color.White,
                )
                Button(
                    onClick = viewModel::playNextNow,
                    modifier = Modifier.focusRequester(continueFocusRequester),
                ) {
                    Text("Continuar viendo")
                }
                OutlinedButton(
                    onClick = {
                        viewModel.cancelNextEpisodePrompt()
                        viewModel.saveProgressSnapshot()
                        onBack()
                    },
                ) {
                    Text("Cancelar")
                }
            }
        }
    }
}
