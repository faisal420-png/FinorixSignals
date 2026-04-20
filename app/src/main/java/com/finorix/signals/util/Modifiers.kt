package com.finorix.signals.util

import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.finorix.signals.presentation.theme.NeonGreen

fun Modifier.neonBorder(shape: Shape = RoundedCornerShape(0.dp), strokeWidth: Dp = 2.dp, color: Color = NeonGreen) = this.then(Modifier.border(strokeWidth, color, shape))

fun Modifier.neonGlow(color: Color = NeonGreen, radius: Dp = 8.dp) = this.then(Modifier.drawBehind { })

fun Modifier.animatedGradientBorder(shape: Shape = RoundedCornerShape(0.dp), borderWidth: Dp = 2.dp, colors: List<Color> = listOf(NeonGreen, Color.Cyan, NeonGreen)): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(initialValue = 0f, targetValue = 1000f, animationSpec = infiniteRepeatable(animation = tween(3000, easing = LinearEasing), repeatMode = RepeatMode.Restart))
    val brush = Brush.linearGradient(colors = colors, start = Offset(offset, offset), end = Offset(offset + 500f, offset + 500f), tileMode = TileMode.Repeated)
    this.then(Modifier.border(borderWidth, brush, shape))
}
