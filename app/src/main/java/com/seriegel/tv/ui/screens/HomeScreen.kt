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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 22.dp),
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
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onOpenProfile) {
            Text("Perfil")
        }

        Spacer(modifier = Modifier.height(10.dp))
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
}

@Composable
private fun ContentCard(
    card: HomeCard,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .size(width = 180.dp, height = 270.dp),
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
                        style = MaterialTheme.typography.titleSmall,
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
