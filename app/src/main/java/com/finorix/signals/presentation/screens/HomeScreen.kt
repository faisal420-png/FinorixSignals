package com.finorix.signals.presentation.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.finorix.signals.presentation.theme.*

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("⚡ FINORIX SOFTWARE ⚡", color = NeonGreen)
        Spacer(modifier = Modifier.height(32.dp))
        Text("Active Signals", color = Color.White)
    }
}
