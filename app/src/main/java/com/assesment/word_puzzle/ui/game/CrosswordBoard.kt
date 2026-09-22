package com.assesment.word_puzzle.ui.game

import android.R.attr.maxHeight
import android.R.attr.maxWidth
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.assesment.word_puzzle.data.Cell
import com.assesment.word_puzzle.data.Level
import com.assesment.word_puzzle.domain.cellsOf
import com.assesment.word_puzzle.domain.solutionCells
import com.assesment.word_puzzle.ui.theme.Cream
import com.assesment.word_puzzle.ui.theme.Parchment
import com.assesment.word_puzzle.ui.theme.TileFound
import com.assesment.word_puzzle.ui.theme.TileHidden

@Composable
fun CrosswordBoard(
    level: Level,
    found: Set<String>,
    modifier: Modifier = Modifier,
) {
    val cells = remember(level) { solutionCells(level) }
    val revealed = remember(level, found) {
        buildSet {
            level.placements.forEach { placement ->
                if (placement.word in found) {
                    cellsOf(placement).forEach { (cell, _) -> add(cell) }
                }
            }
        }
    }
    val rows = cells.keys.minOf { it.row }..cells.keys.maxOf { it.row }
    val cols = cells.keys.minOf { it.col }..cells.keys.maxOf { it.col }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            color = Parchment,
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 10.dp,
        ) {
            BoxWithConstraints(Modifier.padding(18.dp)) {
                val gap = 6.dp
                val tile = tileSize(
                    maxWidth = maxWidth,
                    maxHeight = maxHeight,
                    columns = cols.count(),
                    rows = rows.count(),
                    gap = gap,
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(gap),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    for (row in rows) {
                        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                            for (col in cols) {
                                val cell = Cell(row, col)
                                val letter = cells[cell]
                                if (letter == null) {
                                    Spacer(Modifier.size(tile))
                                } else {
                                    PuzzleTile(
                                        letter = letter,
                                        revealed = cell in revealed,
                                        size = tile,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PuzzleTile(
    letter: Char,
    revealed: Boolean,
    size: Dp,
) {
    val scale = remember { Animatable(1f) }
    var seen by remember { mutableStateOf(revealed) }
    LaunchedEffect(revealed) {
        if (revealed && !seen) {
            scale.snapTo(0.45f)
            scale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 480f))
        }
        seen = revealed
    }
    val color by animateColorAsState(
        targetValue = if (revealed) TileFound else TileHidden,
        animationSpec = tween(280),
        label = "tileColor",
    )
    Box(
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .shadow(if (revealed) 2.dp else 0.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        if (revealed) {
            Text(
                text = letter.toString(),
                color = Cream,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.42f).sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun tileSize(maxWidth: Dp, maxHeight: Dp, columns: Int, rows: Int, gap: Dp): Dp {
    val width = (maxWidth - gap * (columns - 1)) / columns
    val height = (maxHeight - gap * (rows - 1)) / rows
    return minOf(width, height, 58.dp).coerceAtLeast(26.dp)
}