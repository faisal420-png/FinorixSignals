package com.finorix.signals.util

import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.finorix.signals.presentation.theme.NeonGreen
import com.finorix.signals.presentation.theme.NeonGreen30

/**
 * Modifier for a subtle green glow border used on cards
 */
fun Modifier.neonBorder(): Modifier = this.border(
    width = 1.dp,
    color = NeonGreen30,
    shape = RoundedCornerShape(20.dp)
)

/**
 * Modifier for a neon shadow/glow, ideal for primary buttons.
 */
fun Modifier.neonGlow(
    color: Color = NeonGreen,
    borderRadius: Dp = 20.dp,
    blurRadius: Dp = 10.dp,
    offsetY: Dp = 0.dp,
    offsetX: Dp = 0.dp
): Modifier = this.drawBehind {
    val paint = Paint().apply {
        val frameworkPaint = asFrameworkPaint()
        if (blurRadius != 0.dp) {
            frameworkPaint.maskFilter = android.graphics.BlurMaskFilter(
                blurRadius.toPx(),
                android.graphics.BlurMaskFilter.Blur.NORMAL
            )
        }
        frameworkPaint.color = color.copy(alpha = 0.5f).toArgb()
    }

    drawIntoCanvas { canvas ->
        val left = offsetX.toPx()
        val top = offsetY.toPx()
        val right = size.width + left
        val bottom = size.height + top

        canvas.drawRoundRect(
            left = left,
            top = top,
            right = right,
            bottom = bottom,
            radiusX = borderRadius.toPx(),
            radiusY = borderRadius.toPx(),
            paint = paint
        )
    }
}

/**
 * Modifier for a shimmer loading effect
 */
fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = listOf(
        Color.Transparent,
        NeonGreen.copy(alpha = 0.1f),
        NeonGreen.copy(alpha = 0.3f),
        NeonGreen.copy(alpha = 0.1f),
        Color.Transparent,
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 500f, translateAnim + 500f)
    )
    this.drawBehind { drawRect(brush) }
}

/**
 * Modifier for a pulsing glow effect
 */
fun Modifier.pulse(color: Color = NeonGreen): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )
    val alpha by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    this.drawBehind {
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = size.minDimension / 2 * scale
        )
    }
}

/**
 * Modifier for an animated moving gradient border
 */
fun Modifier.animatedGradientBorder(
    colors: List<Color> = listOf(NeonGreen, Color.Transparent, NeonGreen, Color.Transparent),
    borderWidth: Dp = 1.5.dp,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp)
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "border")
    val rotate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "borderRotate"
    )

    val brush = Brush.sweepGradient(colors)
    // Draw rotating border behind content
    this.drawBehind {
        val paint = Paint().apply {
            this.asFrameworkPaint().isAntiAlias = true
            this.shader = brush.createShader(size)
        }
        
        drawIntoCanvas { canvas ->
            canvas.save()
            canvas.rotate(rotate, size.width / 2, size.height / 2)
            canvas.drawRoundRect(
                0f, 0f, size.width, size.height,
                shape.topStart.toPx(size, this),
                shape.topStart.toPx(size, this),
                paint
            )
            canvas.restore()
        }
    }
    // Add the static border for crispness
    .border(borderWidth, NeonGreen30, shape)
}
