package com.seriegel.tv.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import com.seriegel.tv.core.util.formatBytes
import com.seriegel.tv.domain.model.DownloadState
import com.seriegel.tv.ui.viewmodel.DownloadsViewModel

@Composable
fun DownloadsScreen(
    onBack: () -> Unit,
    onPlayDownload: (String) -> Unit,
    viewModel: DownloadsViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onBack) { Text("Atrás") }
            Button(onClick = viewModel::clearAll) { Text("Borrar todo") }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("Memoria usada: ${formatBytes(state.stats.usedBytes)}")
        Text("Elementos descargados: ${state.stats.completedItems}")
        Spacer(modifier = Modifier.height(14.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.downloads) { item ->
                Card(onClick = { if (item.state == DownloadState.COMPLETED) onPlayDownload(item.id) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(item.title)
                        Text("Estado: ${item.state} - ${(item.progressPercent).toInt()}%")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (item.state == DownloadState.COMPLETED) {
                                Button(onClick = { onPlayDownload(item.id) }) { Text("Reproducir offline") }
                            }
                            OutlinedButton(onClick = { viewModel.remove(item.id) }) { Text("Eliminar") }
                        }
                    }
                }
            }
        }
    }
}
