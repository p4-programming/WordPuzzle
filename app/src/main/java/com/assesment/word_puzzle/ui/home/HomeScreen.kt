package com.assesment.word_puzzle.ui.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.assesment.word_puzzle.ui.components.GroveBackground
import com.assesment.word_puzzle.ui.components.GroveButton
import com.assesment.word_puzzle.ui.components.GroveOutlinedButton
import com.assesment.word_puzzle.ui.game.LetterWheel
import com.assesment.word_puzzle.ui.session.GameSessionViewModel
import com.assesment.word_puzzle.ui.theme.Cream
import com.assesment.word_puzzle.ui.theme.Mint

@Composable
fun HomeScreen(
    session: GameSessionViewModel,
    onPlay: (Int) -> Unit,
    onLevels: () -> Unit,
) {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val alpha by animateFloatAsState(if (appeared) 1f else 0f, tween(700), label = "homeAlpha")
    val offset by animateDpAsState(if (appeared) 0.dp else 28.dp, tween(700), label = "homeOffset")
    val completed = session.snapshot.completed.size
    val total = session.levels.size
    val playLabel = when {
        completed == 0 -> "Play"
        completed >= total -> "Play again"
        else -> "Continue"
    }

    GroveBackground {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = maxHeight)
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 28.dp)
                    .alpha(alpha)
                    .graphicsLayer { translationY = offset.toPx() },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
            Spacer(Modifier.height(36.dp))
            Text(
                text = "A WORD GAME",
                color = Mint,
                style = MaterialTheme.typography.labelLarge,
                letterSpacing = MaterialTheme.typography.labelLarge.letterSpacing,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Word Puzzle",
                color = Cream,
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Swipe letters on the wheel\nto fill the crossword.",
                color = Cream.copy(alpha = 0.82f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            LetterWheel(
                letters = listOf('W', 'O', 'R', 'D', 'S'),
                onSubmit = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(vertical = 8.dp),
                enabled = false,
                showPreview = false,
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GroveButton(
                    text = playLabel,
                    onClick = { onPlay(session.nextIncompleteId()) },
                    modifier = Modifier.fillMaxWidth(),
                )
                GroveOutlinedButton(
                    text = "Levels",
                    onClick = onLevels,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "$completed of $total levels clear",
                color = Cream.copy(alpha = 0.75f),
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(20.dp))
            }
        }
    }
}
