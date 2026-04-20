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
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.finorix.signals.presentation.theme.NeonGreen
import com.finorix.signals.presentation.theme.NeonGreen30


/**
 * Modifier for a subtle green glow border used on cards
 */
fun Modifier.neonBorder(color: Color = NeonGreen30): Modifier = this.border(
    width = 1.dp,
    color = color,
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

fun Modifier.animatedGradientBorder(
    colors: List<Color>,
    shape: Shape = RoundedCornerShape(16.dp),
    borderWidth: Dp = 2.dp
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "gradient")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    this.drawWithContent {
        drawContent()
        rotate(angle) {
            drawOutline(
                outline = shape.createOutline(size, layoutDirection, this),
                brush = Brush.sweepGradient(colors),
                style = Stroke(width = borderWidth.toPx())
            )
        }
    }
}
