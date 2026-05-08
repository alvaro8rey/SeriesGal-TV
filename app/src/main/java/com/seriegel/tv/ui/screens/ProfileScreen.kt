package com.seriegel.tv.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import com.seriegel.tv.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onOpenDownloads: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(36.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Perfil")
        Text(text = "Token: ${state.tokenPreview.ifBlank { "N/A" }}")
        Text(text = "Calidad preferida: ${state.preferredQuality}")
        OutlinedButton(onClick = viewModel::cyclePreferredQuality, modifier = Modifier.padding(top = 16.dp)) {
            Text("Cambiar calidad")
        }
        Button(onClick = onOpenDownloads, modifier = Modifier.padding(top = 16.dp)) {
            Text("Panel de descargas")
        }
        Button(onClick = onLogout, modifier = Modifier.padding(top = 16.dp)) {
            Text("Cerrar sesión")
        }
    }
}
