package com.seriegel.tv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.seriegel.tv.ui.theme.SeriesGalColors
import com.seriegel.tv.ui.viewmodel.BrowseCatalogItem
import com.seriegel.tv.ui.viewmodel.BrowseCatalogViewModel
import com.seriegel.tv.ui.viewmodel.BrowseViewMode

@Composable
fun BrowseCatalogScreen(
    contentType: String,
    onBack: () -> Unit,
    onOpenSeries: (String) -> Unit,
    onOpenMovie: (String) -> Unit,
    viewModel: BrowseCatalogViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(contentType) {
        viewModel.load(contentType)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onBack) { Text("Atrás") }
                Text(
                    text = "Todas las ${state.title.lowercase()}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SeriesGalColors.TextPrimary,
                )
            }
            Button(onClick = viewModel::toggleViewMode) {
                Text(if (state.viewMode == BrowseViewMode.GRID) "Vista: Cuadrícula" else "Vista: Lista")
            }
        }

        when {
            state.isLoading -> {
                Text("Cargando...", color = SeriesGalColors.TextSecondary)
            }
            state.errorMessage != null -> {
                Text(state.errorMessage.orEmpty(), color = SeriesGalColors.TextSecondary)
            }
            state.viewMode == BrowseViewMode.GRID -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    gridItems(state.items) { item ->
                        BrowseGridCard(
                            item = item,
                            onClick = {
                                if (item.isSeries) onOpenSeries(item.id) else onOpenMovie(item.id)
                            },
                        )
                    }
                }
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.items) { item ->
                        BrowseListCard(
                            item = item,
                            onClick = {
                                if (item.isSeries) onOpenSeries(item.id) else onOpenMovie(item.id)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BrowseGridCard(item: BrowseCatalogItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        scale = CardDefaults.scale(focusedScale = 1.015f),
        modifier = Modifier.size(width = 124.dp, height = 180.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.84f)),
                        ),
                    )
                    .padding(8.dp),
            ) {
                Text(item.title, color = SeriesGalColors.TextPrimary, maxLines = 2)
            }
        }
    }
}

@Composable
private fun BrowseListCard(item: BrowseCatalogItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        scale = CardDefaults.scale(focusedScale = 1.01f),
        modifier = Modifier
            .fillMaxWidth()
            .height(94.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(SeriesGalColors.SurfaceSoft)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .size(width = 52.dp, height = 74.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
            Column(modifier = Modifier.fillMaxWidth(0.78f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.title, color = SeriesGalColors.TextPrimary, maxLines = 1)
                if (item.subtitle.isNotBlank()) {
                    Text(item.subtitle, color = SeriesGalColors.TextSecondary, maxLines = 1)
                }
            }
            Button(onClick = onClick) { Text("Abrir") }
        }
    }
}
