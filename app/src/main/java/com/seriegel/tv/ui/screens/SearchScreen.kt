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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
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
import com.seriegel.tv.ui.viewmodel.SearchResultItem
import com.seriegel.tv.ui.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenSeries: (String) -> Unit,
    onOpenMovie: (String) -> Unit,
    viewModel: SearchViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(26.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(onClick = onBack) { Text("Atrás") }
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChanged,
                modifier = Modifier.fillMaxWidth(0.82f),
                singleLine = true,
                label = { androidx.compose.material3.Text("Buscar series o películas") },
            )
        }

        if (state.isLoading) {
            Text("Cargando catálogo...", color = SeriesGalColors.TextSecondary)
        } else if (state.errorMessage != null) {
            Text(state.errorMessage.orEmpty(), color = SeriesGalColors.TextSecondary)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                item {
                    Text("Series", style = MaterialTheme.typography.titleLarge, color = SeriesGalColors.TextPrimary)
                }
                items(state.seriesResults) { item ->
                    SearchResultCard(
                        item = item,
                        onClick = { onOpenSeries(item.id) },
                    )
                }
                item {
                    Text("Películas", style = MaterialTheme.typography.titleLarge, color = SeriesGalColors.TextPrimary)
                }
                items(state.movieResults) { item ->
                    SearchResultCard(
                        item = item,
                        onClick = { onOpenMovie(item.id) },
                    )
                }
                if (state.seriesResults.isEmpty() && state.movieResults.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                        ) {
                            Text(
                                text = "Sin resultados",
                                modifier = Modifier.align(Alignment.Center),
                                color = SeriesGalColors.TextSecondary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    item: SearchResultItem,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        scale = CardDefaults.scale(focusedScale = 1.01f),
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(SeriesGalColors.SurfaceSoft)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(width = 58.dp, height = 76.dp)
                    .clip(RoundedCornerShape(8.dp)),
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.25f)))),
                )
            }
            Column(modifier = Modifier.fillMaxWidth(0.67f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.title, color = SeriesGalColors.TextPrimary, maxLines = 1)
                if (item.subtitle.isNotBlank()) {
                    Text(item.subtitle, color = SeriesGalColors.TextSecondary, maxLines = 1)
                }
            }
            Button(onClick = onClick) { Text("Abrir") }
        }
    }
}
