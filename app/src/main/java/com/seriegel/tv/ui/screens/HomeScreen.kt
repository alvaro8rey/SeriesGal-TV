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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import com.seriegel.tv.ui.viewmodel.HomeCard
import com.seriegel.tv.ui.viewmodel.HomeSectionType
import com.seriegel.tv.ui.viewmodel.HomeViewModel
import com.seriegel.tv.ui.theme.SeriesGalColors

@Composable
fun HomeScreen(
    onOpenSeries: (String) -> Unit,
    onOpenMovie: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenProfile: () -> Unit,
    onSeeMoreSeries: () -> Unit,
    onSeeMoreMovies: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val brandName = buildAnnotatedString {
        withStyle(SpanStyle(color = SeriesGalColors.TextPrimary)) { append("Series") }
        withStyle(SpanStyle(color = SeriesGalColors.BrandBlue)) { append("Gal") }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 28.dp, vertical = 22.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = brandName,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onOpenSearch) {
                Icon(Icons.Filled.Search, contentDescription = "Buscar")
            }
            IconButton(onClick = onOpenProfile) {
                Icon(Icons.Filled.AccountCircle, contentDescription = "Perfil")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            items(uiState.sections) { section ->
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    val previewItems = section.items.take(10)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(previewItems) { item ->
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
                        if (section.items.size > 10) {
                            item {
                                SeeMoreCard(
                                    onClick = {
                                        when (section.type) {
                                            HomeSectionType.SERIES -> onSeeMoreSeries()
                                            HomeSectionType.MOVIES -> onSeeMoreMovies()
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
}

@Composable
private fun SeeMoreCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        scale = CardDefaults.scale(focusedScale = 1.015f),
        modifier = Modifier.size(width = 150.dp, height = 220.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeriesGalColors.SurfaceSoft),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Ver más",
                style = MaterialTheme.typography.titleMedium,
                color = SeriesGalColors.BrandBlue,
            )
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
        scale = CardDefaults.scale(focusedScale = 1.015f),
        modifier = Modifier
            .size(width = 124.dp, height = 180.dp),
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = SeriesGalColors.TextPrimary,
                    )
                    when (card) {
                        is HomeCard.SeriesCard -> {
                            card.subtitle?.takeIf { it.isNotBlank() }?.let {
                                Text(it, maxLines = 1, color = SeriesGalColors.TextSecondary)
                            }
                        }
                        is HomeCard.MovieCard -> {
                            card.subtitle?.takeIf { it.isNotBlank() }?.let {
                                Text(it, maxLines = 1, color = SeriesGalColors.TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
