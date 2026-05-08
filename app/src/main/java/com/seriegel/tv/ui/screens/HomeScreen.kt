package com.seriegel.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.seriegel.tv.domain.model.DownloadState
import com.seriegel.tv.ui.viewmodel.HomeCard
import com.seriegel.tv.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onOpenSeries: (String) -> Unit,
    onOpenMovie: (String) -> Unit,
    onOpenProfile: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDownloadsPanel by remember { mutableStateOf(false) }
    val activeDownloads = uiState.activeDownloads
    val aggregateProgress = if (activeDownloads.isEmpty()) 0f else {
        activeDownloads.map { it.progressPercent / 100f }.average().toFloat()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = uiState.title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { }) {
                Icon(Icons.Filled.Search, contentDescription = "Buscar")
            }
            Box(contentAlignment = Alignment.Center) {
                IconButton(onClick = { showDownloadsPanel = true }) {
                    Icon(Icons.Filled.Download, contentDescription = "Descargas activas")
                }
                if (activeDownloads.isNotEmpty()) {
                    CircularProgressIndicator(
                        progress = { aggregateProgress },
                        modifier = Modifier.size(42.dp),
                    )
                }
            }
            if (activeDownloads.isNotEmpty()) {
                Text(
                    text = activeDownloads.size.toString(),
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.Red)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onOpenProfile) {
            Text("Perfil")
        }

        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            items(uiState.sections) { section ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(section.title, style = MaterialTheme.typography.titleLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(section.items) { item ->
                            ContentCard(
                                card = item,
                                onClick = {
                                    when (item) {
                                        is HomeCard.SeriesCard -> onOpenSeries(item.id)
                                        is HomeCard.MovieCard -> onOpenMovie(item.id)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDownloadsPanel) {
        Card(
            onClick = { },
            modifier = Modifier
                .padding(90.dp)
                .fillMaxWidth(0.55f),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Descargas en curso")
                if (activeDownloads.isEmpty()) {
                    Text("No hay descargas activas.")
                } else {
                    activeDownloads.forEach { item ->
                        Text("${item.title} · ${(item.progressPercent).toInt()}% · ${item.state}")
                        if (item.state == DownloadState.DOWNLOADING || item.state == DownloadState.QUEUED) {
                            CircularProgressIndicator(progress = { (item.progressPercent / 100f).coerceIn(0f, 1f) })
                            OutlinedButton(onClick = { viewModel.cancelDownload(item.id) }) {
                                Text("Cancelar")
                            }
                        }
                    }
                }
                Button(onClick = { showDownloadsPanel = false }) { Text("Cerrar") }
            }
        }
    }
}

@Composable
private fun ContentCard(
    card: HomeCard,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .size(width = 240.dp, height = 360.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = card.imageUrl,
                contentDescription = card.title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.88f),
                            ),
                        ),
                    )
                    .padding(10.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        card.title,
                        maxLines = 2,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    when (card) {
                        is HomeCard.SeriesCard -> {
                            card.subtitle?.takeIf { it.isNotBlank() }?.let { Text(it, maxLines = 1) }
                        }
                        is HomeCard.MovieCard -> {
                            card.subtitle?.takeIf { it.isNotBlank() }?.let { Text(it, maxLines = 1) }
                        }
                    }
                }
            }
        }
    }
}
