package com.finorix.signals.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finorix.signals.R
import com.finorix.signals.presentation.theme.NeonGreen
import com.finorix.signals.presentation.theme.PureBlack
import com.finorix.signals.util.neonGlow
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onComplete: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "alpha"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2500)
        onComplete()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(PureBlack),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .alpha(alphaAnim)
                    .neonGlow(color = NeonGreen, blurRadius = 30.dp)
            ) {
                // Placeholder for Bull Logo
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.TrendingUp, 
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "FINORIX AI",
                color = NeonGreen,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.alpha(alphaAnim)
            )
            Text(
                text = "Next Gen Trading Intelligence",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.alpha(alphaAnim)
            )
        }
    }
}
