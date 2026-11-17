package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BlueprintCyanLight
import com.example.ui.theme.BlueprintNavy
import com.example.ui.theme.BlueprintNavyDark
import com.example.ui.theme.SpaceGroteskFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashOverlay(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val drawProgress = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleSlideY = remember { Animatable(18f) }
    val screenAlpha = remember { Animatable(1f) }
    val iconDescription = stringResource(R.string.cd_splash_icon)

    LaunchedEffect(Unit) {
        launch {
            drawProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 680, easing = FastOutSlowInEasing),
            )
        }
        delay(220L)
        launch {
            titleAlpha.animateTo(1f, tween(durationMillis = 360))
        }
        launch {
            titleSlideY.animateTo(0f, tween(durationMillis = 420, easing = FastOutSlowInEasing))
        }
        delay(680L)
        screenAlpha.animateTo(0f, tween(durationMillis = 200))
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .alpha(screenAlpha.value)
            .background(BlueprintNavyDark)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onFinished,
            )
            .testTag("splash_overlay"),
        contentAlignment = Alignment.Center,
    ) {
        // Subtle drafting grid background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 32.dp.toPx()
            val gridColor = Color(0xFF173654)
            var x = 0f
            while (x < size.width) {
                drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                y += step
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Canvas(
                modifier = Modifier
                    .size(96.dp)
                    .semantics { contentDescription = iconDescription },
            ) {
                val p = drawProgress.value
                val w = size.width
                val h = size.height

                // Outer blueprint sheet corner
                val sheetPath = Path().apply {
                    moveTo(w * 0.24f, h * 0.22f)
                    lineTo(w * 0.86f, h * 0.22f)
                    lineTo(w * 0.86f, h * 0.84f)
                    lineTo(w * 0.24f, h * 0.84f)
                    close()
                }
                val sheetMeasure = PathMeasure().apply { setPath(sheetPath, false) }
                val partialSheet = Path()
                sheetMeasure.getSegment(0f, sheetMeasure.length * p, partialSheet, true)
                drawPath(
                    path = partialSheet,
                    color = Color.White.copy(alpha = 0.88f),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )

                // L-shaped architectural drafting ruler
                val rulerProgress = ((p - 0.15f) / 0.85f).coerceIn(0f, 1f)
                val rulerPath = Path().apply {
                    moveTo(w * 0.12f, h * 0.68f)
                    lineTo(w * 0.68f, h * 0.68f)
                    lineTo(w * 0.68f, h * 0.12f)
                }
                val rulerMeasure = PathMeasure().apply { setPath(rulerPath, false) }
                val partialRuler = Path()
                rulerMeasure.getSegment(0f, rulerMeasure.length * rulerProgress, partialRuler, true)
                drawPath(
                    path = partialRuler,
                    color = BlueprintCyanLight,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
                )

                // Crisp ruler tick marks
                val tickProgress = ((p - 0.5f) / 0.5f).coerceIn(0f, 1f)
                if (tickProgress > 0f) {
                    val tickCount = 5
                    for (i in 0 until tickCount) {
                        val tx = w * (0.20f + i * 0.10f)
                        val tickH = if (i % 2 == 0) 8.dp.toPx() else 5.dp.toPx()
                        drawLine(
                            color = BlueprintNavy,
                            start = Offset(tx, h * 0.68f),
                            end = Offset(tx, h * 0.68f - tickH * tickProgress),
                            strokeWidth = 1.6.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                    }
                    // Inner right-angle dimension square
                    val sqSize = 14.dp.toPx() * tickProgress
                    drawRect(
                        color = BlueprintCyanLight.copy(alpha = 0.7f),
                        topLeft = Offset(w * 0.68f - sqSize - 8.dp.toPx(), h * 0.68f - sqSize - 8.dp.toPx()),
                        size = Size(sqSize, sqSize),
                        style = Stroke(width = 2.dp.toPx()),
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 28.sp,
                ),
                color = Color.White,
                modifier = Modifier
                    .alpha(titleAlpha.value)
                    .offset(y = titleSlideY.value.dp),
            )
        }
    }
}
