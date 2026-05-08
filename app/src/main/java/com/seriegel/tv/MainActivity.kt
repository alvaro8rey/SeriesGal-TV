package com.seriegel.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.seriegel.tv.ui.TvApp
import com.seriegel.tv.ui.theme.SeriesGalTvTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SeriesGalTvTheme {
                TvApp()
            }
        }
    }
}
