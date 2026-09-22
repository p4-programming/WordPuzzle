package com.assesment.word_puzzle.ui.pause

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.assesment.word_puzzle.ui.components.GroveBackground
import com.assesment.word_puzzle.ui.components.GroveButton
import com.assesment.word_puzzle.ui.components.GroveOutlinedButton
import com.assesment.word_puzzle.ui.theme.Ink
import com.assesment.word_puzzle.ui.theme.Parchment

@Composable
fun PauseScreen(
    levelNumber: Int,
    levelName: String,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onLevels: () -> Unit,
    onHome: () -> Unit,
) {
    GroveBackground {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = maxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    color = Parchment,
                    shape = RoundedCornerShape(32.dp),
                    shadowElevation = 12.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("Paused", color = Ink, style = MaterialTheme.typography.headlineMedium)
                        Text(
                            text = "Level $levelNumber · $levelName",
                            color = Ink.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                        GroveButton("Resume", onResume, Modifier.fillMaxWidth())
                        GroveOutlinedButton("Restart", onRestart, Modifier.fillMaxWidth(), onDark = false)
                        GroveOutlinedButton("Levels", onLevels, Modifier.fillMaxWidth(), onDark = false)
                        GroveOutlinedButton("Home", onHome, Modifier.fillMaxWidth(), onDark = false)
                    }
                }
            }
        }
    }
}
