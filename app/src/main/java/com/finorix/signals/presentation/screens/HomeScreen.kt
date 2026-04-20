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
import com.finorix.signals.util.animatedGradientBorder
import com.finorix.signals.util.neonBorder
import com.finorix.signals.util.neonGlow
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
