package com.assesment.word_puzzle.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.assesment.word_puzzle.ui.theme.Cream
import com.assesment.word_puzzle.ui.theme.Deep
import com.assesment.word_puzzle.ui.theme.Gold
import com.assesment.word_puzzle.ui.theme.Ink
import com.assesment.word_puzzle.ui.theme.Night

@Composable
fun GroveBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Night, Deep, Color(0xFF1E5C43), Color(0xFF0C241C)),
                ),
            ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                color = Gold.copy(alpha = 0.16f),
                radius = size.minDimension * 0.28f,
                center = Offset(size.width * 0.86f, size.height * 0.08f),
            )
            drawCircle(
                color = Color(0xFF0A1F18).copy(alpha = 0.55f),
                radius = size.width * 0.72f,
                center = Offset(size.width * 0.15f, size.height * 1.05f),
            )
            drawCircle(
                color = Color(0xFF145C3E).copy(alpha = 0.45f),
                radius = size.width * 0.48f,
                center = Offset(size.width * 0.92f, size.height * 0.92f),
            )
        }
        content()
    }
}

@Composable
fun GroveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(stiffness = 620f),
        label = "buttonScale",
    )
    Button(
        onClick = onClick,
        interactionSource = interaction,
        modifier = modifier
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Ink),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun GroveOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDark: Boolean = true,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(stiffness = 620f),
        label = "outlineScale",
    )
    OutlinedButton(
        onClick = onClick,
        interactionSource = interaction,
        modifier = modifier
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.5.dp, Gold),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (onDark) Cream else Ink,
        ),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun PauseIcon(
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier.size(22.dp)) {
        val barWidth = size.width * 0.24f
        val barHeight = size.height * 0.78f
        val top = (size.height - barHeight) / 2f
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.14f, top),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(6f, 6f),
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(size.width * 0.62f, top),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(6f, 6f),
        )
    }
}
