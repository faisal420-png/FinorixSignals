package com.finorix.signals.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finorix.signals.data.local.entity.SignalEntity
import com.finorix.signals.data.local.entity.SignalOutcome
import com.finorix.signals.domain.model.SignalDirection
import com.finorix.signals.presentation.theme.*
import com.finorix.signals.util.neonBorder
import com.finorix.signals.util.neonGlow
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackOrderScreen(
    viewModel: TrackOrderViewModel = hiltViewModel()
) {
    val signals by viewModel.signals.collectAsState()
    val winRate by viewModel.winRate.collectAsState()
    val currentFilter by viewModel.filter.collectAsState()
    var selectedSignal by remember { mutableStateOf<com.finorix.signals.domain.model.Signal?>(null) }
    var showDetailSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(16.dp)
    ) {
        // Accuracy Card
        AccuracyCard(winRate)
        
        Spacer(modifier = Modifier.height(24.dp))

        // Filters
        FilterRow(currentFilter) { viewModel.setFilter(it) }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Signal List
        if (signals.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                EmptyState("No signals tracked yet.\nActivate 'Auto Signals' to start monitoring.")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(signals) { signal ->
                    SignalHistoryItem(signal) {
                        selectedSignal = signal
                        showDetailSheet = true
                    }
                }
            }
        }
    }

    if (showDetailSheet && selectedSignal != null) {
        ModalBottomSheet(
            onDismissRequest = { showDetailSheet = false },
            containerColor = CardBackground,
            contentColor = TextPrimary
        ) {
            SignalDetailContent(selectedSignal!!)
        }
    }
}

@Composable
fun AccuracyCard(winRate: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .neonGlow(color = NeonGreen, blurRadius = 15.dp),
        shape = RoundedCornerShape(24.dp),
        color = CardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Your AI Accuracy", color = TextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$winRate%",
                    color = NeonGreen,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("(last 7 days)", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(bottom = 12.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterRow(selected: String, onSelect: (String) -> Unit) {
    val filters = listOf("All", "Wins", "Losses", "Today")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        filters.forEach { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelect(filter) },
                label = { Text(filter) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NeonGreen,
                    selectedLabelColor = PureBlack,
                    containerColor = Color.Transparent,
                    labelColor = NeonGreen
                ),
                border = FilterChipDefaults.filterChipBorder(borderColor = NeonGreen)
            )
        }
    }
}

@Composable
@Composable
fun SignalHistoryItem(signal: com.finorix.signals.domain.model.Signal, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("HH:mm | dd MMM", Locale.US)
    val color = when (signal.outcome) {
        "WIN" -> NeonGreen
        "LOSS" -> ErrorRed
        else -> WarningYellow
    }

    Surface(
        modifier = Modifier.fillMaxWidth().neonBorder(color = color.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(signal.pair, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(sdf.format(Date(signal.timestamp)), color = TextSecondary, fontSize = 12.sp)
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (signal.direction == SignalDirection.UP) "UP" else "DOWN",
                        color = if (signal.direction == SignalDirection.UP) NeonGreen else ErrorRed,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${signal.confidence}%", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
                
                Surface(
                    color = color.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = signal.outcome,
                        color = color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SignalDetailContent(signal: com.finorix.signals.domain.model.Signal) {
    val sdf = SimpleDateFormat("HH:mm:ss | dd MMM yyyy", Locale.US)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text("Signal Analysis History", style = MaterialTheme.typography.headlineSmall, color = NeonGreen, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        DetailRow("Asset Pair", signal.pair)
        DetailRow("Signal Time", sdf.format(Date(signal.timestamp)))
        DetailRow("Direction", signal.direction.name, if(signal.direction == SignalDirection.UP) NeonGreen else ErrorRed)
        DetailRow("Confidence", "${signal.confidence}%")
        DetailRow("Timeframe", signal.timeframe)
        DetailRow("Outcome", signal.outcome, when(signal.outcome) {
            "WIN" -> NeonGreen
            "LOSS" -> ErrorRed
            else -> WarningYellow
        })
        
        Spacer(modifier = Modifier.height(24.dp))
        // Simple indicator display
        Text(signal.indicators.toString(), color = TextPrimary, fontSize = 12.sp, lineHeight = 18.sp)
    }
}

@Composable
fun DetailRow(label: String, value: String, valueColor: Color = TextPrimary) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EmptyState(message: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome, 
            contentDescription = null,
            tint = NeonGreen.copy(alpha = 0.3f),
            modifier = Modifier.size(100.dp).neonGlow(color = NeonGreen, blurRadius = 20.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = message,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

