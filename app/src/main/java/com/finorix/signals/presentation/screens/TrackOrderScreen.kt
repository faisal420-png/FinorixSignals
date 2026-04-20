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
import androidx.compose.material.icons.Icons
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finorix.signals.data.local.entity.SignalEntity
import com.finorix.signals.data.local.entity.SignalOutcome
import com.finorix.signals.domain.model.SignalDirection
import com.finorix.signals.presentation.screens.TrackOrderViewModel
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
