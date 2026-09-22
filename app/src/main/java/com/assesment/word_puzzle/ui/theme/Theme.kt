package com.assesment.word_puzzle.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PuzzleColors = lightColorScheme(
    primary = Leaf,
    onPrimary = Cream,
    secondary = Gold,
    onSecondary = Ink,
    tertiary = Mint,
    background = Night,
    onBackground = Cream,
    surface = Cream,
    onSurface = Ink,
    error = Coral,
    onError = Cream,
)

@Composable
fun WordPuzzleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PuzzleColors,
        typography = Typography,
        content = content,
    )
}
