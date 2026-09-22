package com.assesment.word_puzzle.ui.game

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.assesment.word_puzzle.domain.BannerKind
import com.assesment.word_puzzle.ui.components.GroveBackground
import com.assesment.word_puzzle.ui.components.GroveButton
import com.assesment.word_puzzle.ui.components.PauseIcon
import com.assesment.word_puzzle.ui.session.GameSessionViewModel
import com.assesment.word_puzzle.ui.theme.Coral
import com.assesment.word_puzzle.ui.theme.Cream
import com.assesment.word_puzzle.ui.theme.Gold
import com.assesment.word_puzzle.ui.theme.Ink
import com.assesment.word_puzzle.ui.theme.Mint
import com.assesment.word_puzzle.ui.theme.Parchment
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun GameScreen(
    levelId: Int,
    session: GameSessionViewModel,
    onPause: () -> Unit,
    onNextLevel: (Int) -> Unit,
    onAllClear: () -> Unit,
) {
    val level = session.level(levelId)
    if (level == null) {
        GroveBackground {
            Text(
                text = "That level is missing.",
                color = Cream,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        return
    }

    val progress = session.progress(level.id)
    val showComplete = session.snapshot.justCompletedLevelId == level.id
    val banner = session.snapshot.banner
    var showBonus by remember { mutableStateOf(false) }
    var advanced by remember(level.id) { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    fun advance() {
        if (advanced) return
        advanced = true
        session.consumeCompletion()
        val next = session.nextLevelId(level.id)
        if (next == null) onAllClear() else onNextLevel(next)
    }

    BackHandler {
        if (showBonus) showBonus = false else onPause()
    }

    LaunchedEffect(banner?.nonce) {
        val nonce = banner?.nonce ?: return@LaunchedEffect
        when (banner.kind) {
            BannerKind.Grid, BannerKind.Bonus -> haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            BannerKind.Invalid, BannerKind.Duplicate -> haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        delay(if (banner.kind == BannerKind.Invalid) 900 else 1200)
        session.dismissBanner(nonce)
    }

    LaunchedEffect(showComplete) {
        if (showComplete && session.nextLevelId(level.id) != null) {
            delay(1700)
            advance()
        }
    }

    Box(Modifier.fillMaxSize()) {
        GroveBackground {
            BoxWithConstraints(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
            ) {
                val landscape = maxWidth > maxHeight
                val wheelLetters = session.letters(level.id)
                val shakeKey = if (banner?.kind == BannerKind.Invalid || banner?.kind == BannerKind.Duplicate) {
                    banner.nonce
                } else {
                    0
                }
                Column(Modifier.fillMaxSize()) {
                    GameTopBar(
                        title = level.name,
                        found = progress.found.size,
                        total = level.gridWords.size,
                        onPause = onPause,
                    )
                    if (landscape) {
                        Row(Modifier.weight(1f).fillMaxWidth()) {
                            CrosswordBoard(
                                level = level,
                                found = progress.found,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .padding(start = 16.dp, end = 8.dp, bottom = 12.dp),
                            )
                            Column(Modifier.weight(1f).fillMaxSize()) {
                                LetterWheel(
                                    letters = wheelLetters,
                                    onSubmit = { session.submit(level.id, it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    enabled = !showComplete,
                                    shakeKey = shakeKey,
                                )
                                GameActions(
                                    bonusCount = progress.bonus.size,
                                    onBonus = { showBonus = true },
                                    onShuffle = { session.shuffle(level.id) },
                                )
                            }
                        }
                    } else {
                        CrosswordBoard(
                            level = level,
                            found = progress.found,
                            modifier = Modifier
                                .weight(1.05f)
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                        )
                        LetterWheel(
                            letters = wheelLetters,
                            onSubmit = { session.submit(level.id, it) },
                            modifier = Modifier
                                .weight(0.95f)
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            enabled = !showComplete,
                            shakeKey = shakeKey,
                        )
                        GameActions(
                            bonusCount = progress.bonus.size,
                            onBonus = { showBonus = true },
                            onShuffle = { session.shuffle(level.id) },
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = banner != null && !showComplete,
            enter = fadeIn() + slideInVertically { -it / 2 },
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 64.dp),
        ) {
            banner?.let { FeedbackChip(it.word, it.kind) }
        }

        AnimatedVisibility(
            visible = showComplete,
            enter = fadeIn(tween(280)) + scaleIn(initialScale = 0.94f, animationSpec = tween(320)),
            exit = fadeOut(),
        ) {
            LevelCompleteOverlay(
                levelNumber = level.id,
                levelName = level.name,
                bonusCount = progress.bonus.size,
                hasNext = session.nextLevelId(level.id) != null,
                onNext = { advance() },
            )
        }
    }

    if (showBonus) {
        AlertDialog(
            onDismissRequest = { showBonus = false },
            containerColor = Parchment,
            title = {
                Text("Bonus words", color = Ink, style = MaterialTheme.typography.titleLarge)
            },
            text = {
                if (progress.bonus.isEmpty()) {
                    Text(
                        "Words that are not on the board still count. Keep swiping.",
                        color = Ink,
                    )
                } else {
                    Text(
                        progress.bonus.sorted().joinToString("\n"),
                        color = Ink,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showBonus = false }) {
                    Text("Close", color = Ink)
                }
            },
        )
    }
}

@Composable
private fun GameActions(
    bonusCount: Int,
    onBonus: () -> Unit,
    onShuffle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onBonus) {
            Text(
                text = "Bonus $bonusCount",
                color = Mint,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        IconButton(onClick = onShuffle) {
            Icon(Icons.Filled.Refresh, contentDescription = "Shuffle letters", tint = Cream)
        }
    }
}

@Composable
private fun GameTopBar(
    title: String,
    found: Int,
    total: Int,
    onPause: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(start = 16.dp)) {
            Text(title, color = Cream, style = MaterialTheme.typography.titleLarge, maxLines = 1)
            Text(
                text = "$found / $total words",
                color = Mint,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        IconButton(onClick = onPause) {
            PauseIcon(tint = Cream)
        }
    }
}

@Composable
private fun FeedbackChip(word: String, kind: BannerKind) {
    val (label, color) = when (kind) {
        BannerKind.Grid -> "Nice!" to Gold
        BannerKind.Bonus -> "Bonus word" to Mint
        BannerKind.Invalid -> "Not in this puzzle" to Coral
        BannerKind.Duplicate -> "Already found" to Cream
    }
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF10281F).copy(alpha = 0.92f))
            .padding(horizontal = 22.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(word, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        Text(label, color = Cream.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun LevelCompleteOverlay(
    levelNumber: Int,
    levelName: String,
    bonusCount: Int,
    hasNext: Boolean,
    onNext: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
        contentAlignment = Alignment.Center,
    ) {
        Celebration()
        Column(
            modifier = Modifier
                .padding(28.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Parchment)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (hasNext) "Level clear" else "All clear",
                color = Ink,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Level $levelNumber · $levelName",
                color = Ink.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 8.dp),
            )
            if (bonusCount > 0) {
                Text(
                    text = "$bonusCount bonus ${if (bonusCount == 1) "word" else "words"}",
                    color = Ink,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            GroveButton(
                text = if (hasNext) "Next level" else "Back to levels",
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
            )
            if (hasNext) {
                Text(
                    text = "Moving on…",
                    color = Ink.copy(alpha = 0.55f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun Celebration() {
    val progress = remember { Animatable(0f) }
    val specks = remember {
        List(18) {
            Speck(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.45f + 0.2f,
                radius = Random.nextInt(4, 10).toFloat(),
                color = listOf(Gold, Mint, Cream, Coral).random(),
            )
        }
    }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(1400)) }
    Canvas(Modifier.fillMaxSize()) {
        specks.forEach { speck ->
            drawCircle(
                color = speck.color.copy(alpha = 1f - progress.value),
                radius = speck.radius,
                center = Offset(
                    speck.x * size.width,
                    speck.y * size.height - progress.value * size.height * 0.25f,
                ),
            )
        }
    }
}

private data class Speck(val x: Float, val y: Float, val radius: Float, val color: Color)
