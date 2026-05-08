package com.seriegel.tv.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.seriegel.tv.core.config.ServerConfig
import com.seriegel.tv.domain.model.DownloadState
import com.seriegel.tv.ui.viewmodel.MovieDetailViewModel

@Composable
fun MovieDetailScreen(
    movieId: String,
    onBack: () -> Unit,
    onPlay: () -> Unit,
    viewModel: MovieDetailViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(movieId) {
        viewModel.load(movieId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            AsyncImage(
                model = state.movie?.id?.let(ServerConfig::coverUrl),
                contentDescription = state.movie?.title,
                modifier = Modifier
                    .size(width = 250.dp, height = 360.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onBack) { Text("Atrás") }
                    OutlinedButton(onClick = viewModel::cycleQuality) { Text("Calidad ${state.qualitySelection}") }
                }
                Text(state.movie?.title ?: "Película", style = MaterialTheme.typography.headlineMedium)
                Text(state.movie?.description.orEmpty(), maxLines = 6)
                Text("Año: ${state.movie?.year.orEmpty()}")
                Text("Progreso ${(state.resumeProgress * 100).toInt()}%")
                state.download?.let { download ->
                    Text("Estado descarga: ${download.state} ${(download.progressPercent).toInt()}%")
                    if (download.state == DownloadState.COMPLETED) {
                        Text("Disponible offline")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { viewModel.playMovie(); onPlay() }) {
                Text(if (state.resumeProgress > 0.05f) "Reanudar" else "Reproducir")
            }
            OutlinedButton(onClick = viewModel::toggleDownload) {
                Text(if (state.download == null) "Descargar" else "Eliminar / Reintentar")
            }
        }
    }
}
