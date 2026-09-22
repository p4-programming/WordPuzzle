package com.assesment.word_puzzle.domain

import com.assesment.word_puzzle.data.Level
import kotlin.random.Random

enum class BannerKind { Grid, Bonus, Invalid, Duplicate }

data class Banner(
    val word: String,
    val kind: BannerKind,
    val nonce: Int,
)

data class LevelProgress(
    val found: Set<String> = emptySet(),
    val bonus: Set<String> = emptySet(),
    val wheel: String? = null,
)

data class ProgressSnapshot(
    val levels: Map<Int, LevelProgress> = emptyMap(),
    val completed: Set<Int> = emptySet(),
    val justCompletedLevelId: Int? = null,
    val banner: Banner? = null,
)

class ProgressStore(initial: ProgressSnapshot = ProgressSnapshot()) {
    var snapshot: ProgressSnapshot = initial
        private set

    fun submit(level: Level, raw: String) {
        val word = raw.trim().uppercase()
        if (word.length < 3) return

        val progress = snapshot.levels[level.id] ?: LevelProgress()
        val kind = when {
            !canSpell(level.letters, word) -> BannerKind.Invalid
            word in progress.found || word in progress.bonus -> BannerKind.Duplicate
            word in level.gridWords -> BannerKind.Grid
            word in level.bonusWords -> BannerKind.Bonus
            else -> BannerKind.Invalid
        }
        val updated = progress.copy(
            found = if (kind == BannerKind.Grid) progress.found + word else progress.found,
            bonus = if (kind == BannerKind.Bonus) progress.bonus + word else progress.bonus,
        )
        val complete = level.gridWords.all { it in updated.found }
        val wasComplete = level.id in snapshot.completed
        snapshot = snapshot.copy(
            levels = snapshot.levels + (level.id to updated),
            completed = if (complete) snapshot.completed + level.id else snapshot.completed,
            justCompletedLevelId = if (complete && !wasComplete) level.id else snapshot.justCompletedLevelId,
            banner = Banner(word, kind, (snapshot.banner?.nonce ?: 0) + 1),
        )
    }

    fun shuffle(level: Level, random: Random = Random) {
        val progress = snapshot.levels[level.id] ?: LevelProgress()
        val current = progress.wheel ?: openingWheel(level.letters, level.id)
        snapshot = snapshot.copy(
            levels = snapshot.levels + (level.id to progress.copy(wheel = reshuffle(level.letters, current, random))),
            banner = null,
        )
    }

    fun restart(level: Level, random: Random = Random) {
        val current = snapshot.levels[level.id]?.wheel ?: openingWheel(level.letters, level.id)
        snapshot = snapshot.copy(
            levels = snapshot.levels + (level.id to LevelProgress(wheel = reshuffle(level.letters, current, random))),
            completed = snapshot.completed - level.id,
            justCompletedLevelId = snapshot.justCompletedLevelId.takeUnless { it == level.id },
            banner = null,
        )
    }

    fun dismissBanner(nonce: Int) {
        if (snapshot.banner?.nonce == nonce) {
            snapshot = snapshot.copy(banner = null)
        }
    }

    fun consumeCompletion() {
        if (snapshot.justCompletedLevelId != null) {
            snapshot = snapshot.copy(justCompletedLevelId = null)
        }
    }
}

object ProgressCodec {
    fun encode(snapshot: ProgressSnapshot): String = buildString {
        append("C=")
        append(snapshot.completed.sorted().joinToString(","))
        append('\n')
        append("J=")
        append(snapshot.justCompletedLevelId?.toString().orEmpty())
        append('\n')
        snapshot.levels.toSortedMap().forEach { (id, progress) ->
            append('L')
            append(id)
            append('=')
            append(progress.found.sorted().joinToString(","))
            append('|')
            append(progress.bonus.sorted().joinToString(","))
            append('|')
            append(progress.wheel.orEmpty())
            append('\n')
        }
    }

    fun decode(raw: String?): ProgressSnapshot {
        if (raw.isNullOrBlank()) return ProgressSnapshot()
        var completed = emptySet<Int>()
        var justCompleted: Int? = null
        val levels = mutableMapOf<Int, LevelProgress>()
        raw.lineSequence().forEach { line ->
            when {
                line.startsWith("C=") -> {
                    completed = line.removePrefix("C=")
                        .split(',')
                        .filter { it.isNotBlank() }
                        .mapNotNull { it.toIntOrNull() }
                        .toSet()
                }
                line.startsWith("J=") -> {
                    justCompleted = line.removePrefix("J=").toIntOrNull()
                }
                line.startsWith("L") && line.contains('=') -> {
                    val id = line.substring(1, line.indexOf('=')).toIntOrNull() ?: return@forEach
                    val parts = line.substringAfter('=').split('|')
                    levels[id] = LevelProgress(
                        found = parts.getOrNull(0).toWordSet(),
                        bonus = parts.getOrNull(1).toWordSet(),
                        wheel = parts.getOrNull(2)?.takeIf { it.isNotBlank() },
                    )
                }
            }
        }
        return ProgressSnapshot(
            levels = levels,
            completed = completed,
            justCompletedLevelId = justCompleted,
        )
    }

    private fun String?.toWordSet(): Set<String> =
        orEmpty().split(',').filter { it.isNotBlank() }.toSet()
}
