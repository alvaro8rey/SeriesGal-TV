package com.seriegel.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.seriegel.tv.core.config.ServerConfig
import com.seriegel.tv.ui.theme.SeriesGalColors
import com.seriegel.tv.ui.viewmodel.SeriesDetailViewModel

@Composable
fun SeriesDetailScreen(
    seriesId: String,
    onPlay: () -> Unit,
    onBack: () -> Unit,
    viewModel: SeriesDetailViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(seriesId) {
        viewModel.load(seriesId)
    }
    LaunchedEffect(state.navigateToPlayer) {
        if (state.navigateToPlayer) {
            onPlay()
            viewModel.consumeNavigateToPlayer()
        }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshContinueWatching()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(30.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                AsyncImage(
                    model = state.series?.id?.let(ServerConfig::coverUrl),
                    contentDescription = state.series?.title,
                    modifier = Modifier
                        .size(width = 200.dp, height = 290.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = state.series?.title ?: "Serie",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SeriesGalColors.TextPrimary,
                    )
                    Text(
                        text = state.series?.description.orEmpty(),
                        maxLines = 6,
                        color = SeriesGalColors.TextSecondary,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onBack) { Text("Atrás") }
                        Button(onClick = viewModel::toggleFavorite) {
                            Text(if (state.favorite) "Quitar favorito" else "Añadir favorito")
                        }
                    }
                }
            }
        }

        state.continueWatching?.let { continueWatching ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    scale = CardDefaults.scale(focusedScale = 1.01f),
                    onClick = {
                        viewModel.resumeContinueWatching()
                    },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SeriesGalColors.SurfaceSoft)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text("Seguir viendo", color = SeriesGalColors.TextSecondary)
                            Text(
                                text = "${continueWatching.episodeTitle} · ${continueWatching.progressPercent}%",
                                color = SeriesGalColors.TextPrimary,
                            )
                        }
                        Button(onClick = viewModel::resumeContinueWatching) {
                            Text("Reanudar")
                        }
                    }
                }
            }
        }

        state.nextEpisodeCountdown?.let { prompt ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    scale = CardDefaults.scale(focusedScale = 1.01f),
                    onClick = {},
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SeriesGalColors.SurfaceSoft)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Siguiente episodio", color = SeriesGalColors.TextSecondary)
                            Text(
                                text = "${prompt.episodeTitle} en ${prompt.secondsRemaining}s",
                                color = SeriesGalColors.TextPrimary,
                                maxLines = 1,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = viewModel::playNextEpisodeNow) { Text("Reproducir") }
                            OutlinedButton(onClick = viewModel::cancelNextEpisodeCountdown) { Text("Cancelar") }
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                state.series?.seasons?.forEachIndexed { index, season ->
                    val selected = index == state.selectedSeasonIndex
                    Button(onClick = { viewModel.selectSeason(index) }) {
                        Text(if (selected) "● ${season.title}" else season.title)
                    }
                }
            }
        }

        if (state.totalPages > 1) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = viewModel::previousPage,
                        enabled = state.currentPage > 0,
                    ) { Text("Página anterior") }
                    Text(
                        text = "Página ${state.currentPage + 1} de ${state.totalPages}",
                        color = SeriesGalColors.TextSecondary,
                    )
                    OutlinedButton(
                        onClick = viewModel::nextPage,
                        enabled = state.currentPage < state.totalPages - 1,
                    ) { Text("Página siguiente") }
                }
            }
        }

        items(state.episodes) { episodeUi ->
            Card(
                onClick = { viewModel.playEpisode(episodeUi.episode) },
                scale = CardDefaults.scale(focusedScale = 1.015f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp),
            ) {
                Row(
                    modifier = Modifier
                        .background(SeriesGalColors.SurfaceSoft)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = episodeUi.episode.title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        color = SeriesGalColors.TextPrimary,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (episodeUi.isCompleted) {
                            Text("Completado", color = SeriesGalColors.TextSecondary)
                        }
                        Button(onClick = { viewModel.playEpisode(episodeUi.episode) }) {
                            Text(if (episodeUi.progressRatio > 0.05f) "Reanudar" else "Ver")
                        }
                    }
                }
            }
        }
    }
}
