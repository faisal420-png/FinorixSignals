package com.finorix.signals.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.finorix.signals.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackOrderScreen(
    onBackClick: () -> Unit,
    viewModel: TrackOrderViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Track Orders") }) }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Text("Track your orders here")
        }
    }
}
