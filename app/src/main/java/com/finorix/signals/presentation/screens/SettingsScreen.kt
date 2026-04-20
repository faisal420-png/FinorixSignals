package com.finorix.signals.presentation.screens

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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen),
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Text(currentModelName)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ArrowDropDown, null)
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(CardBackground).fillMaxWidth(0.85f)
                ) {
                    freeModels.forEach { (id, name) ->
                        DropdownMenuItem(
                            text = { Text(name, color = TextPrimary) },
                            onClick = {
                                viewModel.setSelectedModel(id)
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            SettingsTextField(label = "OpenRouter API Key", value = openRouterKey) { viewModel.setOpenRouterApiKey(it) }
            
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { viewModel.testConnection() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, NeonGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Test Connection", color = NeonGreen)
            }
            
            testResult?.let {
                Text(it, color = if (it.contains("✅")) NeonGreen else TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. MARKET DATA
        SettingsSection(title = "Market Data", icon = Icons.Default.Public) {
            Surface(
                color = NeonGreen.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Using Binance (Crypto) + Yahoo Finance (Forex) — 100% Free. No keys needed.",
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. SIGNAL PREFERENCES
        SettingsSection(title = "Signal Preferences", icon = Icons.Default.Analytics) {
            Text("Min Signal Confidence: ${minConfidence}%", color = TextSecondary, fontSize = 13.sp)
            Slider(
                value = minConfidence.toFloat(),
                onValueChange = { viewModel.setMinConfidence(it.toInt()) },
                valueRange = 60f..90f,
                colors = SliderDefaults.colors(thumbColor = NeonGreen, activeTrackColor = NeonGreen)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Default Timeframe", color = TextSecondary, fontSize = 13.sp)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("1m", "3m", "5m", "15m").forEach { tf ->
                    FilterChip(
                        selected = defaultTimeframe == tf,
                        onClick = { viewModel.setDefaultTimeframe(tf) },
                        label = { Text(tf) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NeonGreen, selectedLabelColor = PureBlack)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Enabled Pairs", color = TextSecondary, fontSize = 13.sp)
            val allPairs = listOf("EUR/USD", "GBP/USD", "USD/JPY", "BTC/USDT", "ETH/USDT", "SOL/USDT")
            Column(modifier = Modifier.padding(top = 8.dp)) {
                allPairs.chunked(2).forEach { chunk ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        chunk.forEach { pair ->
                            Row(
                                modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = enabledPairs.contains(pair),
                                    onCheckedChange = { viewModel.togglePair(pair) },
                                    colors = CheckboxDefaults.colors(checkedColor = NeonGreen)
                                )
                                Text(pair, color = TextPrimary, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. NOTIFICATIONS
        SettingsSection(title = "Notifications", icon = Icons.Default.Notifications) {
            SettingsToggle(label = "Sound Effects", icon = Icons.Default.VolumeUp, checked = soundEnabled) { viewModel.setSoundEnabled(it) }
            SettingsToggle(label = "Vibration Feedback", icon = Icons.Default.Vibration, checked = vibrationEnabled) { viewModel.setVibrationEnabled(it) }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Notify only above: ${notificationConfidence}%", color = TextSecondary, fontSize = 13.sp)
            Slider(
                value = notificationConfidence.toFloat(),
                onValueChange = { viewModel.setNotificationConfidence(it.toInt()) },
                valueRange = 60f..95f,
                colors = SliderDefaults.colors(thumbColor = NeonGreen, activeTrackColor = NeonGreen)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. ABOUT
        SettingsSection(title = "About Finorix", icon = Icons.Default.Info) {
            Text("Version: 2.1.0-Flash", color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "DISCLAIMER: This software is for educational and analytical purposes only. Trading involves high risk of loss. Finorix is not a financial advisor.",
                color = ErrorRed,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Support: support@finorix.ai", color = NeonGreen, fontSize = 12.sp)
            Text("GitHub: github.com/finorix/signals", color = NeonGreen, fontSize = 12.sp)
            
            Spacer(modifier = Modifier.height(16.dp))
            SettingsToggle(
                label = "Anonymous usage data", 
                icon = Icons.Default.Shield, 
                checked = analyticsEnabled
            ) { viewModel.setAnalyticsEnabled(it) }
            Text("Help us improve Finorix by sharing anonymous interaction data.", color = TextSecondary, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 6. ACCOUNT
        SettingsSection(title = "Account", icon = Icons.Default.Person) {
            Button(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, ErrorRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Sign Out", color = ErrorRed, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
fun SettingsSection(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Surface(
        color = CardBackground,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun SettingsToggle(label: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, color = TextPrimary, fontSize = 14.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen, checkedTrackColor = NeonGreen.copy(alpha = 0.5f))
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonGreen,
            unfocusedBorderColor = Color.Gray,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        singleLine = true
    )
}
