package com.seriegel.tv.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

private val SeriesGalColorScheme = darkColorScheme(
    primary = SeriesGalColors.BrandBlue,
    onPrimary = Color(0xFF062033),
    secondary = Color(0xFF9FB6CC),
    onSecondary = Color(0xFF0F1925),
    background = SeriesGalColors.Background,
    onBackground = SeriesGalColors.TextPrimary,
    surface = SeriesGalColors.Surface,
    onSurface = SeriesGalColors.TextPrimary,
    surfaceVariant = SeriesGalColors.SurfaceSoft,
    onSurfaceVariant = SeriesGalColors.TextSecondary,
    border = Color(0xFF3A475E),
)

@Composable
fun SeriesGalTvTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SeriesGalColorScheme,
        content = content,
    )
}
