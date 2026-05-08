package com.seriegel.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import com.seriegel.tv.domain.model.DownloadState
import com.seriegel.tv.ui.viewmodel.SeriesDetailViewModel

@Composable
fun SeriesDetailScreen(
    seriesId: String,
    onPlay: () -> Unit,
    onBack: () -> Unit,
    viewModel: SeriesDetailViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(seriesId) {
        viewModel.load(seriesId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onBack) { Text("Atrás") }
            Button(onClick = viewModel::toggleFavorite) {
                Text(if (state.favorite) "Quitar favorito" else "Añadir favorito")
            }
            OutlinedButton(onClick = viewModel::cycleQuality) {
                Text("Calidad ${state.qualitySelection}")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(state.series?.title ?: "Serie")
        Text(state.series?.description.orEmpty())
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.series?.seasons?.forEachIndexed { index, season ->
                val selected = index == state.selectedSeasonIndex
                Button(
                    onClick = { viewModel.selectSeason(index) },
                ) {
                    Text(if (selected) "● ${season.title}" else season.title)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.episodes) { episodeUi ->
                Card(onClick = { viewModel.playEpisode(episodeUi.episode); onPlay() }) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(episodeUi.episode.title)
                        Text("Progreso ${(episodeUi.progressRatio * 100).toInt()}%")
                        if (episodeUi.isCompleted) {
                            Text("Completado")
                        }
                        val download = episodeUi.download
                        Text(
                            when {
                                download == null -> "Descargar (${state.qualitySelection})"
                                download.state == DownloadState.DOWNLOADING -> "Descargando ${(download.progressPercent).toInt()}%"
                                download.state == DownloadState.COMPLETED -> "Disponible offline"
                                download.state == DownloadState.FAILED -> "Error, reintentar"
                                else -> download.state.name
                            },
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .background(Color.DarkGray.copy(alpha = 0.25f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Button(onClick = { viewModel.playEpisode(episodeUi.episode); onPlay() }) {
                                Text(if (episodeUi.progressRatio > 0.05f) "Reanudar" else "Ver desde inicio")
                            }
                            OutlinedButton(onClick = { viewModel.toggleEpisodeDownload(episodeUi.episode) }) {
                                Text(if (download == null) "Descargar" else "Eliminar/Reintentar")
                            }
                        }
                    }
                }
            }
        }
    }
}
