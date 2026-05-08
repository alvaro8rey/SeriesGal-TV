package com.seriegel.tv.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.Text
import com.seriegel.tv.R
import com.seriegel.tv.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onOpenCatalog: () -> Unit,
    onOpenProfile: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val uiState = viewModel.uiState.value
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = uiState.title.ifBlank { androidx.compose.ui.res.stringResource(id = R.string.home_welcome) })
        Button(
            onClick = onOpenCatalog,
            modifier = Modifier
                .padding(top = 24.dp)
                .fillMaxWidth(0.4f),
        ) {
            Text(text = androidx.compose.ui.res.stringResource(id = R.string.home_action_explore))
        }
        Button(
            onClick = onOpenProfile,
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(0.4f),
        ) {
            Text(text = androidx.compose.ui.res.stringResource(id = R.string.home_action_profile))
        }
    }
}
