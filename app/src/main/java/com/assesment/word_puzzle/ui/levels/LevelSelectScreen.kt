package com.assesment.word_puzzle.ui.levels

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.assesment.word_puzzle.data.Level
import com.assesment.word_puzzle.ui.components.GroveBackground
import com.assesment.word_puzzle.ui.session.GameSessionViewModel
import com.assesment.word_puzzle.ui.theme.Cream
import com.assesment.word_puzzle.ui.theme.Gold
import com.assesment.word_puzzle.ui.theme.Ink
import com.assesment.word_puzzle.ui.theme.Leaf
import com.assesment.word_puzzle.ui.theme.Mint

@Composable
fun LevelSelectScreen(
    session: GameSessionViewModel,
    onBack: () -> Unit,
    onLevel: (Int) -> Unit,
) {
    val nextId = session.nextIncompleteId()
    GroveBackground {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Cream)
                }
                Text(
                    text = "Levels",
                    color = Cream,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            Text(
                text = "Every level is open. Cleared puzzles keep a gold mark.",
                color = Cream.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 8.dp),
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(96.dp),
                contentPadding = PaddingValues(20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(session.levels, key = { it.id }) { level ->
                    LevelBadge(
                        level = level,
                        cleared = level.id in session.snapshot.completed,
                        current = level.id == nextId && level.id !in session.snapshot.completed,
                        onClick = { onLevel(level.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelBadge(
    level: Level,
    cleared: Boolean,
    current: Boolean,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, spring(stiffness = 600f), label = "badge")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            ),
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(if (cleared) Gold else Cream.copy(alpha = 0.92f))
                .border(
                    width = if (current) 3.dp else 0.dp,
                    color = if (current) Mint else Cream,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = level.id.toString(),
                color = if (cleared) Ink else Leaf,
                style = MaterialTheme.typography.titleLarge,
            )
            if (cleared) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Cleared",
                    tint = Leaf,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(16.dp),
                )
            }
        }
        Text(
            text = level.name,
            color = Cream,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
