package com.seriegel.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.seriegel.tv.core.config.ServerConfig
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
    var pageIndex by remember { mutableIntStateOf(0) }
    val pagedEpisodes = state.episodes.chunked(20)
    val maxPages = pagedEpisodes.size
    val visibleEpisodes = pagedEpisodes.getOrElse(pageIndex) { emptyList() }

    LaunchedEffect(seriesId) {
        viewModel.load(seriesId)
    }
    LaunchedEffect(state.selectedSeasonIndex) {
        pageIndex = 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            AsyncImage(
                model = state.series?.id?.let(ServerConfig::coverUrl),
                contentDescription = state.series?.title,
                modifier = Modifier
                    .size(width = 250.dp, height = 360.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = state.series?.title ?: "Serie",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(state.series?.description.orEmpty(), maxLines = 5)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onBack) { Text("Atrás") }
                    Button(onClick = viewModel::toggleFavorite) {
                        Text(if (state.favorite) "Quitar favorito" else "Añadir favorito")
                    }
                    OutlinedButton(onClick = viewModel::cycleQuality) {
                        Text("Calidad ${state.qualitySelection}")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            state.series?.seasons?.forEachIndexed { index, season ->
                val selected = index == state.selectedSeasonIndex
                Button(
                    onClick = { viewModel.selectSeason(index) },
                ) {
                    Text(if (selected) "● ${season.title}" else season.title)
                }
            }
        }
        if (maxPages > 1) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { pageIndex = (pageIndex - 1).coerceAtLeast(0) },
                    enabled = pageIndex > 0,
                ) { Text("Página anterior") }
                Text("Página ${pageIndex + 1} de $maxPages")
                OutlinedButton(
                    onClick = { pageIndex = (pageIndex + 1).coerceAtMost(maxPages - 1) },
                    enabled = pageIndex < maxPages - 1,
                ) { Text("Página siguiente") }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(visibleEpisodes) { episodeUi ->
                Card(onClick = { viewModel.playEpisode(episodeUi.episode); onPlay() }) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(episodeUi.episode.title, style = MaterialTheme.typography.titleMedium)
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
                            horizontalArrangement = Arrangement.SpaceBetween,
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
