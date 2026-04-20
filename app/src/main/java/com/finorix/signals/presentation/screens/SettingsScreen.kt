package com.finorix.signals.presentation.screens


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finorix.signals.presentation.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onSignOut: () -> Unit
) {
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
    val minConfidence by viewModel.minConfidence.collectAsState()
    val defaultTimeframe by viewModel.defaultTimeframe.collectAsState()
    val openRouterKey by viewModel.openRouterApiKey.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val notificationConfidence by viewModel.notificationConfidence.collectAsState()
    val enabledPairs by viewModel.enabledPairs.collectAsState()
    val testResult by viewModel.testConnectionResult.collectAsState()
    val analyticsEnabled by viewModel.analyticsEnabled.collectAsState()


    val freeModels = listOf(
        "deepseek/deepseek-chat:free" to "DeepSeek V3 (Recommended)",
        "meta-llama/llama-3.3-70b-instruct:free" to "Llama 3.3 70B",
        "google/gemini-2.0-flash-exp:free" to "Gemini Flash 2.0",
        "mistralai/mistral-small-3.2-24b-instruct:free" to "Mistral Small 3.2"
    )


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge, color = NeonGreen, fontWeight = FontWeight.Bold)
        Text("Free Analytics Stack", color = TextSecondary, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(24.dp))


        // 1. AI MODEL SELECTION
        SettingsSection(title = "AI Model Selection", icon = Icons.Default.AutoAwesome) {
            var expanded by remember { mutableStateOf(false) }
            val currentModelName = freeModels.find { it.first == selectedModel }?.second ?: "Select Model"


            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { expanded = true },
