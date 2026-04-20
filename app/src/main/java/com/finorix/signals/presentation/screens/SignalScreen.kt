package com.finorix.signals.presentation.screens

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finorix.signals.domain.model.Candle
import com.finorix.signals.domain.model.SignalDirection
import com.finorix.signals.presentation.theme.*
import com.finorix.signals.util.*
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignalScreen(
    viewModel: SignalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val countdown by viewModel.countdown.collectAsState()
    val isAutoSignalsEnabled by viewModel.isAutoSignalsEnabled.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
    val selectedPair by viewModel.selectedPair.collectAsState()
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsState()
    
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is SignalUiState.Success && state.signal != null) {
            if (vibrationEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            if (soundEnabled) {
                try {
                    val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP: Selectors
        PairSelector(selectedPair) { viewModel.selectPair(it) }
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeframeSelector(selectedTimeframe) { viewModel.selectTimeframe(it) }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Auto Signals", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isAutoSignalsEnabled,
                    onCheckedChange = { viewModel.toggleAutoSignals(context) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonGreen,
                        checkedTrackColor = NeonGreen.copy(alpha = 0.5f),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Live Price
        val price = (uiState as? SignalUiState.Success)?.lastPrice ?: 0.0
        LivePriceTicker(price)

        Spacer(modifier = Modifier.height(24.dp))

        // MIDDLE: Chart with Shimmer Loading
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .neonBorder(),
        ) {
            when (val state = uiState) {
                is SignalUiState.Loading -> Box(Modifier.fillMaxSize().shimmer())
                is SignalUiState.Success -> CandlestickChartWithEMA(state.candles)
                is SignalUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.message, color = ErrorRed, modifier = Modifier.padding(16.dp)) }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SIGNAL CARD
        if (uiState is SignalUiState.Success) {
            val signal = (uiState as SignalUiState.Success).signal
            if (signal != null) {
                SignalDetailsCard(signal, countdown)
            } else {
                Text("Analyzing market for high-confidence signals...", color = TextSecondary, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        var showAiSheet by remember { mutableStateOf(false) }

        // BOTTOM: AI Button
        Button(
            onClick = { 
                val state = uiState
                if (state is SignalUiState.Success && state.signal != null) {
                    viewModel.explainSignal(state.signal, state.candles)
                    showAiSheet = true 
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .neonGlow(color = SoftPurple, blurRadius = 15.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SoftPurple),
            shape = RoundedCornerShape(28.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Explain with AI", fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showAiSheet) {
        val aiText by viewModel.aiExplanation.collectAsState()
        val isAiLoading by viewModel.isAiLoading.collectAsState()

        ModalBottomSheet(
            onDismissRequest = { showAiSheet = false },
            containerColor = CardBackground,
            contentColor = TextPrimary
        ) {
            AiExplanationContent(aiText, isAiLoading)
        }
    }
}

@Composable
fun PairSelector(selected: String, onSelect: (String) -> Unit) {
    val pairs = listOf("EUR/USD", "GBP/USD", "USD/JPY", "BTC/USDT", "ETH/USDT", "SOL/USDT")
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, NeonGreen),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen)
        ) {
            Text(selected, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(CardBackground).fillMaxWidth(0.9f)
        ) {
            pairs.forEach { pair ->
                DropdownMenuItem(
                    text = { Text(pair, color = TextPrimary) },
                    onClick = {
                        onSelect(pair)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeframeSelector(selected: String, onSelect: (String) -> Unit) {
    val timeframes = listOf("1m", "3m", "5m", "15m")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        timeframes.forEach { tf ->
            FilterChip(
                selected = selected == tf,
                onClick = { onSelect(tf) },
                label = { Text(tf) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NeonGreen,
                    selectedLabelColor = PureBlack,
                    labelColor = NeonGreen,
                    containerColor = Color.Transparent
                ),
                border = FilterChipDefaults.filterChipBorder(borderColor = NeonGreen)
            )
        }
    }
}

@Composable
fun LivePriceTicker(price: Double) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (price > 0) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
            contentDescription = null,
            tint = if (price > 0) NeonGreen else ErrorRed,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = String.format(Locale.US, "%.5f", price),
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CandlestickChartWithEMA(candles: List<Candle>) {
    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        val width = size.width
        val height = size.height
        val candleWidth = width / (candles.size * 2f)
        val spacing = width / candles.size
        
        val maxPrice = candles.maxByOrNull { it.high }?.high?.toFloat() ?: 100f
        val minPrice = candles.minByOrNull { it.low }?.low?.toFloat() ?: 0f
        val range = (maxPrice - minPrice).coerceAtLeast(0.0001f)

        fun normalize(price: Double) = height - ((price.toFloat() - minPrice) / range * height)

        // Draw Candlesticks
        candles.forEachIndexed { index, candle ->
            val x = (index * spacing) + (spacing / 2f)
            val color = if (candle.isBullish) NeonGreen else ErrorRed
            
            drawLine(color, androidx.compose.ui.geometry.Offset(x, normalize(candle.high)), androidx.compose.ui.geometry.Offset(x, normalize(candle.low)), 2f)
            val top = normalize(maxOf(candle.open, candle.close))
            val bottom = normalize(minOf(candle.open, candle.close))
            drawRect(color, androidx.compose.ui.geometry.Offset(x - candleWidth / 2f, top), androidx.compose.ui.geometry.Size(candleWidth, abs(bottom - top).coerceAtLeast(1f)))
        }

        // EMA Overlays
        drawEMA(candles, 9, Color.Cyan, this, minPrice, range)
        drawEMA(candles, 21, SoftPurple, this, minPrice, range)
    }
}

private fun drawEMA(candles: List<Candle>, period: Int, color: Color, scope: androidx.compose.ui.graphics.drawscope.DrawScope, minPrice: Float, range: Float) {
    if (candles.size < period) return
    val multiplier = 2.0 / (period + 1)
    val points = mutableListOf<androidx.compose.ui.geometry.Offset>()
    val spacing = scope.size.width / candles.size
    
    var ema = candles.take(period).map { it.close }.average()
    for (i in period until candles.size) {
        ema = (candles[i].close - ema) * multiplier + ema
        val x = (i * spacing) + (spacing / 2f)
        val y = scope.size.height - ((ema.toFloat() - minPrice) / range * scope.size.height)
        points.add(androidx.compose.ui.geometry.Offset(x, y))
    }

    val path = Path().apply {
        points.forEachIndexed { index, offset ->
            if (index == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
        }
    }
    scope.drawPath(path, color, style = Stroke(width = 2.dp.toPx()))
}

@Composable
fun SignalDetailsCard(signal: com.finorix.signals.domain.model.Signal, countdown: Int) {
    var expanded by remember { mutableStateOf(false) }
    
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse)
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animatedGradientBorder(colors = listOf(if (signal.direction == SignalDirection.UP) NeonGreen else ErrorRed, Color.Transparent))
            .neonGlow(if (signal.direction == SignalDirection.UP) NeonGreen else ErrorRed),
        shape = RoundedCornerShape(24.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (signal.direction == SignalDirection.UP) NeonGreen else ErrorRed)
                        .pulse(if (signal.direction == SignalDirection.UP) NeonGreen else ErrorRed)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (signal.direction == SignalDirection.UP) "BUY SIGNAL" else "SELL SIGNAL",
                    color = if (signal.direction == SignalDirection.UP) NeonGreen else ErrorRed,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Confidence", color = TextSecondary, fontSize = 12.sp)
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = signal.confidence / 100f,
                            color = if (signal.direction == SignalDirection.UP) NeonGreen else ErrorRed,
                            trackColor = Color.DarkGray,
                            strokeWidth = 6.dp,
                            modifier = Modifier.size(60.dp)
                        )
                        Text("${signal.confidence}%", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Expires In", color = TextSecondary, fontSize = 12.sp)
                    Text(String.format("00:%02d", countdown), color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "Hide Analysis" else "Why this signal?", color = NeonGreen)
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = NeonGreen)
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    signal.indicators.forEach { (name, value) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(name, color = TextSecondary)
                            Text(value, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiExplanationContent(text: String, isLoading: Boolean) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, null, tint = SoftPurple)
            Spacer(modifier = Modifier.width(12.dp))
            Text("AI Technical Analysis", style = MaterialTheme.typography.headlineSmall, color = SoftPurple, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(modifier = Modifier.fillMaxWidth().minHeight(100.dp)) {
            if (text.isEmpty() && isLoading) {
                CircularProgressIndicator(color = SoftPurple, modifier = Modifier.align(Alignment.Center))
            } else {
                Column {
                    Text(
                        text = buildAnnotatedString {
                            append(text)
                            if (isLoading) {
                                withStyle(SpanStyle(color = SoftPurple, fontWeight = FontWeight.Bold)) {
                                    append(" █") // Typing cursor
                                }
                            }
                        },
                        color = TextPrimary,
                        lineHeight = 24.sp
                    )
                    
                    if (!isLoading && text.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "⚠️ Educational use only. Not financial advice.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        
        if (isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(color = SoftPurple, modifier = Modifier.fillMaxWidth())
        }
    }
}

private fun Modifier.minHeight(height: androidx.compose.ui.unit.Dp) = this.defaultMinSize(minHeight = height)
