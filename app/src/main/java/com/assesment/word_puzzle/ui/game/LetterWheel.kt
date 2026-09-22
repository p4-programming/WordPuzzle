package com.assesment.word_puzzle.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.assesment.word_puzzle.domain.updateSelection
import com.assesment.word_puzzle.ui.theme.Cream
import com.assesment.word_puzzle.ui.theme.Gold
import com.assesment.word_puzzle.ui.theme.GoldDeep
import com.assesment.word_puzzle.ui.theme.Ink
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun LetterWheel(
    letters: List<Char>,
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showPreview: Boolean = true,
    shakeKey: Int = 0,
) {
    var selected by remember { mutableStateOf(emptyList<Int>()) }
    var finger by remember { mutableStateOf<Offset?>(null) }
    val haptics = LocalHapticFeedback.current
    val textMeasurer = rememberTextMeasurer()
    val shake = remember { Animatable(0f) }
    val scales = letters.indices.map { index ->
        animateFloatAsState(
            targetValue = if (index in selected) 1.16f else 1f,
            animationSpec = spring(dampingRatio = 0.62f, stiffness = 520f),
            label = "letterScale$index",
        )
    }

    LaunchedEffect(shakeKey) {
        if (shakeKey == 0) return@LaunchedEffect
        repeat(3) {
            shake.animateTo(14f, tween(40))
            shake.animateTo(-14f, tween(40))
        }
        shake.animateTo(0f, tween(40))
    }

    Column(
        modifier = modifier.offset { IntOffset(shake.value.roundToInt(), 0) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (showPreview) {
            Box(
                modifier = Modifier.height(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                val word = selected.joinToString("") { letters[it].toString() }
                androidx.compose.material3.Text(
                    text = word,
                    color = if (word.length >= 3) Gold else Cream.copy(alpha = 0.75f),
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    letterSpacing = 6.sp,
                )
            }
        }
        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .semantics { contentDescription = "Letter wheel. Swipe across letters to spell a word." }
                .pointerInput(letters, enabled) {
                    if (!enabled || letters.isEmpty()) return@pointerInput
                    try {
                        awaitEachGesture {
                            var current = emptyList<Int>()
                            val down = awaitFirstDown()
                            down.consume()
                            var last = down.position

                            val wheelSize = Size(size.width.toFloat(), size.height.toFloat())
                            fun apply(to: Offset) {
                                var next = current
                                for (hit in sampleHits(last, to, wheelSize, letters.size)) {
                                    next = updateSelection(next, hit)
                                }
                                val grew = next.size > current.size
                                current = next
                                selected = next
                                finger = to
                                last = to
                                if (grew) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }

                            apply(down.position)
                            drag(down.id) { change ->
                                if (change.positionChange() != Offset.Zero) change.consume()
                                apply(change.position)
                            }
                            val word = current.joinToString("") { letters[it].toString() }
                            current = emptyList()
                            selected = emptyList()
                            finger = null
                            onSubmit(word)
                        }
                    } finally {
                        selected = emptyList()
                        finger = null
                    }
                },
        ) {
            if (letters.isEmpty()) return@Canvas
            val orbit = size.minDimension * 0.30f
            val bubble = size.minDimension * 0.105f
            val plate = orbit + bubble + 10.dp.toPx()
            drawCircle(Color.White.copy(alpha = 0.07f), plate)
            drawCircle(Color(0xFF0E2A22).copy(alpha = 0.42f), plate * 0.9f)
            drawCircle(
                color = Color.White.copy(alpha = 0.18f),
                radius = plate,
                style = Stroke(width = 2.dp.toPx()),
            )

            val centers = List(letters.size) { letterCenter(it, letters.size, size) }
            if (selected.isNotEmpty()) {
                val path = Path()
                selected.forEachIndexed { index, letterIndex ->
                    val center = centers[letterIndex]
                    if (index == 0) path.moveTo(center.x, center.y) else path.lineTo(center.x, center.y)
                }
                val lastCenter = centers[selected.last()]
                val tip = finger
                if (tip != null && (tip - lastCenter).getDistance() > bubble * 0.45f) {
                    path.lineTo(tip.x, tip.y)
                }
                val stroke = size.minDimension * 0.045f
                drawPath(
                    path = path,
                    color = Gold.copy(alpha = 0.35f),
                    style = Stroke(width = stroke * 2.1f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
                drawPath(
                    path = path,
                    color = GoldDeep,
                    style = Stroke(width = stroke * 1.25f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
                drawPath(
                    path = path,
                    color = Gold,
                    style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
            }

            letters.forEachIndexed { index, letter ->
                val center = centers[index]
                val picked = index in selected
                scale(scales[index].value, scales[index].value, pivot = center) {
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.16f),
                        radius = bubble,
                        center = center + Offset(0f, 5f),
                    )
                    drawCircle(
                        color = if (picked) Gold else Cream,
                        radius = bubble,
                        center = center,
                    )
                    val layout = textMeasurer.measure(
                        text = letter.toString(),
                        style = TextStyle(
                            color = Ink,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                        ),
                    )
                    drawText(
                        textLayoutResult = layout,
                        topLeft = Offset(
                            center.x - layout.size.width / 2f,
                            center.y - layout.size.height / 2f,
                        ),
                    )
                }
            }
        }
    }
}

internal fun letterCenter(index: Int, count: Int, size: Size): Offset {
    val angle = -PI / 2.0 + (2.0 * PI * index / count)
    val radius = size.minDimension * 0.30f
    return Offset(
        x = size.width / 2f + cos(angle).toFloat() * radius,
        y = size.height / 2f + sin(angle).toFloat() * radius,
    )
}

internal fun hitIndex(position: Offset, size: Size, count: Int): Int? {
    if (count == 0) return null
    val hitRadius = size.minDimension * 0.15f
    var best = -1
    var bestDistance = Float.MAX_VALUE
    for (index in 0 until count) {
        val distance = (position - letterCenter(index, count, size)).getDistance()
        if (distance <= hitRadius && distance < bestDistance) {
            best = index
            bestDistance = distance
        }
    }
    return best.takeIf { it >= 0 }
}

internal fun sampleHits(from: Offset, to: Offset, size: Size, count: Int): List<Int> {
    val distance = (to - from).getDistance()
    val steps = (distance / 8f).toInt().coerceIn(1, 48)
    val hits = mutableListOf<Int>()
    for (step in 1..steps) {
        val t = step / steps.toFloat()
        val point = Offset(from.x + (to.x - from.x) * t, from.y + (to.y - from.y) * t)
        val hit = hitIndex(point, size, count) ?: continue
        if (hits.lastOrNull() != hit) hits += hit
    }
    return hits
}
