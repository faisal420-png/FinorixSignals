package com.finorix.signals.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finorix.signals.R
import com.finorix.signals.presentation.theme.*
import com.finorix.signals.util.neonBorder
import com.finorix.signals.util.neonGlow
import com.finorix.signals.util.animatedGradientBorder
import com.finorix.signals.domain.model.Candle

@Composable
fun HomeScreen() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // LEFT SIDE (Top on mobile)
        BullLogo()
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "⚡ FINORIX SOFTWARE ⚡",
            color = NeonGreen,
            style = Typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "🔥 Premium Features:",
            color = TextPrimary,
            style = Typography.titleLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        Spacer(modifier = Modifier.height(16.dp))
        FeaturesList()
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // RIGHT SIDE (Bottom on mobile)
        TradingTerminalCard()
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // BELOW the card
        GlowingActiveButton()
        Spacer(modifier = Modifier.height(16.dp))
        FreePricePill()
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun BullLogo() {
    Surface(
        modifier = Modifier.size(80.dp),
        shape = androidx.compose.foundation.shape.CircleShape,
        color = CardBackground,
        border = BorderStroke(2.dp, SoftPurple)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_bull_head),
            contentDescription = "Bull Logo",
            tint = SoftPurple,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun FeaturesList() {
    val features = listOf(
        "See Next 2/3 Candles Advance",
        "Future Candle Predictor",
        "Works on All Real Markets",
        "24/7 Admin Support",
        "Lifetime Free Updates",
        "No Lag, Instant Execute"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        features.forEach { feature ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = feature, color = TextPrimary, style = Typography.bodyLarge)
            }
        }
    }
}

@Composable
fun TradingTerminalCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animatedGradientBorder()
            .neonGlow(borderRadius = 20.dp, blurRadius = 15.dp),
        shape = RoundedCornerShape(20.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Activated", color = NeonGreen, fontWeight = FontWeight.Bold)
                Text("Stopped", color = TextSecondary)
                Text("Featured", color = TextSecondary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Pair selector & toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("EUR/USD (1m)", color = TextPrimary, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = TextPrimary)
                }
                Switch(
                    checked = true, 
                    onCheckedChange = {},
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PureBlack,
                        checkedTrackColor = NeonGreen
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Candlestick Chart (Canvas)
            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                MockCandlestickChart()
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bottom icons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Icon(Icons.Default.Code, contentDescription = null, tint = TextSecondary)
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = NeonGreen)
                Icon(painterResource(id = android.R.drawable.stat_sys_download), contentDescription = null, tint = TextSecondary)
                Icon(Icons.Default.Settings, contentDescription = null, tint = TextSecondary)
            }
        }
    }
}

@Composable
fun GlowingActiveButton() {
    Button(
        onClick = { /*TODO*/ },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .neonGlow(color = NeonGreen, blurRadius = 15.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
        shape = RoundedCornerShape(28.dp)
    ) {
        Icon(painterResource(id = android.R.drawable.ic_menu_share), contentDescription = null, tint = PureBlack)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Active Finorix Software", color = PureBlack, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
fun FreePricePill() {
    Surface(
        modifier = Modifier.wrapContentSize(),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, WarningYellow)
    ) {
        Text(
            text = "Price: FREE",
            color = WarningYellow,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun MockCandlestickChart() {
    val candles = remember {
        listOf(
            Candle(0L, open = 20.0, high = 40.0, low = 10.0, close = 30.0, volume = 0.0),
            Candle(0L, open = 25.0, high = 60.0, low = 30.0, close = 50.0, volume = 0.0),
            Candle(0L, open = 40.0, high = 55.0, low = 50.0, close = 45.0, volume = 0.0),
            Candle(0L, open = 40.0, high = 70.0, low = 45.0, close = 65.0, volume = 0.0),
            Candle(0L, open = 50.0, high = 80.0, low = 65.0, close = 60.0, volume = 0.0),
            Candle(0L, open = 30.0, high = 65.0, low = 60.0, close = 35.0, volume = 0.0),
            Candle(0L, open = 20.0, high = 50.0, low = 35.0, close = 45.0, volume = 0.0),
            Candle(0L, open = 40.0, high = 90.0, low = 45.0, close = 85.0, volume = 0.0),
            Candle(0L, open = 70.0, high = 95.0, low = 85.0, close = 75.0, volume = 0.0),
            Candle(0L, open = 70.0, high = 100.0, low = 75.0, close = 95.0, volume = 0.0)
        )
    }
    
    val maxPrice = 100f
    val minPrice = 0f
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val candleWidth = width / (candles.size * 2f)
        val spacing = width / candles.size
        
        candles.forEachIndexed { index, candle ->
            val xCenter = (index * spacing) + (spacing / 2f)
            val yHigh = height - ((candle.high.toFloat() - minPrice) / (maxPrice - minPrice) * height)
            val yLow = height - ((candle.low.toFloat() - minPrice) / (maxPrice - minPrice) * height)
            val yOpen = height - ((candle.open.toFloat() - minPrice) / (maxPrice - minPrice) * height)
            val yClose = height - ((candle.close.toFloat() - minPrice) / (maxPrice - minPrice) * height)
            
            val color = if (candle.isBullish) NeonGreen else ErrorRed
            
            // Draw wick
            drawLine(
                color = color,
                start = androidx.compose.ui.geometry.Offset(xCenter, yHigh),
                end = androidx.compose.ui.geometry.Offset(xCenter, yLow),
                strokeWidth = 2.dp.toPx()
            )
            
            // Draw body
            val top = minOf(yOpen, yClose)
            val bottom = maxOf(yOpen, yClose)
            drawRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(xCenter - candleWidth / 2f, top),
                size = androidx.compose.ui.geometry.Size(candleWidth, maxOf(bottom - top, 2.dp.toPx()))
            )
        }
    }
}
