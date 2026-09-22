package com.assesment.word_puzzle

import com.assesment.word_puzzle.data.LevelParser
import com.assesment.word_puzzle.data.LevelValidator
import com.assesment.word_puzzle.domain.BannerKind
import com.assesment.word_puzzle.domain.ProgressCodec
import com.assesment.word_puzzle.domain.ProgressStore
import com.assesment.word_puzzle.domain.canSpell
import com.assesment.word_puzzle.domain.openingWheel
import com.assesment.word_puzzle.domain.reshuffle
import com.assesment.word_puzzle.domain.updateSelection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.random.Random

class GameLogicTest {
    private val levels = LevelValidator.validate(LevelParser.parse(levelsJson()))

    @Test
    fun catalogHasTwentyPlayableLevels() {
        assertEquals(20, levels.size)
        assertEquals((1..20).toList(), levels.map { it.id })
    }

    @Test
    fun openingWheelIsAPermutation() {
        levels.forEach { level ->
            val wheel = openingWheel(level.letters, level.id)
            assertEquals(level.letters.toList().sorted(), wheel.toList().sorted())
            if (level.letters.length > 1) {
                assertNotEquals(level.letters, wheel)
            }
        }
    }

    @Test
    fun swipeCanUndoThePreviousLetterAndIgnoresRepeats() {
        assertEquals(listOf(0), updateSelection(emptyList(), 0))
        assertEquals(listOf(0, 1, 2), updateSelection(listOf(0, 1, 2), 0))
        assertEquals(listOf(0, 2), updateSelection(listOf(0), 2))
        assertEquals(listOf(0), updateSelection(listOf(0, 2), 0))
        var path = listOf(0, 1, 2)
        path = updateSelection(path, 1)
        assertEquals(listOf(0, 1), path)
        path = updateSelection(path, null)
        assertEquals(listOf(0, 1), path)
    }

    @Test
    fun solvingALevelMarksItCompleteOnce() {
        val store = ProgressStore()
        val level = levels.first { it.id == 1 }
        store.submit(level, "cat")
        assertEquals(setOf("CAT"), store.snapshot.levels[1]?.found)
        assertEquals(BannerKind.Grid, store.snapshot.banner?.kind)
        store.submit(level, "CAT")
        assertEquals(BannerKind.Duplicate, store.snapshot.banner?.kind)
        store.submit(level, "ACT")
        assertTrue(1 in store.snapshot.completed)
        assertEquals(1, store.snapshot.justCompletedLevelId)
        store.submit(level, "ACT")
        assertEquals(1, store.snapshot.justCompletedLevelId)
    }

    @Test
    fun bonusInvalidAndShortWords() {
        val store = ProgressStore()
        val level = levels.first { it.id == 3 }
        store.submit(level, "er")
        assertNull(store.snapshot.banner)
        store.submit(level, "era")
        assertEquals(BannerKind.Bonus, store.snapshot.banner?.kind)
        assertEquals(setOf("ERA"), store.snapshot.levels[3]?.bonus)
        assertFalse(3 in store.snapshot.completed)
        store.submit(level, "ERA")
        assertEquals(BannerKind.Duplicate, store.snapshot.banner?.kind)
        store.submit(level, "RAE")
        assertEquals(BannerKind.Invalid, store.snapshot.banner?.kind)
        assertTrue(canSpell(level.letters, "RAE"))
    }

    @Test
    fun everyLevelCanBeCleared() {
        val store = ProgressStore()
        levels.forEach { level ->
            level.placements.forEach { store.submit(level, it.word.lowercase()) }
            assertTrue(level.id in store.snapshot.completed)
            assertEquals(level.id, store.snapshot.justCompletedLevelId)
            store.consumeCompletion()
            assertNull(store.snapshot.justCompletedLevelId)
        }
    }

    @Test
    fun restartClearsAFinishedLevel() {
        val store = ProgressStore()
        val level = levels.first { it.id == 1 }
        level.placements.forEach { store.submit(level, it.word) }
        store.restart(level, Random(4))
        assertFalse(1 in store.snapshot.completed)
        assertTrue(store.snapshot.levels[1]?.found.orEmpty().isEmpty())
        assertNull(store.snapshot.justCompletedLevelId)
    }

    @Test
    fun shuffleKeepsTheSameLetters() {
        val level = levels.first { it.id == 5 }
        val store = ProgressStore()
        val before = openingWheel(level.letters, level.id)
        store.shuffle(level, Random(2))
        val after = store.snapshot.levels[5]?.wheel
        assertEquals(before.toList().sorted(), after.orEmpty().toList().sorted())
        assertNotEquals(before, after)
    }

    @Test
    fun progressSurvivesEncodeAndDecode() {
        val store = ProgressStore()
        val level = levels.first { it.id == 11 }
        store.submit(level, "HEAT")
        store.submit(level, "HARE")
        store.shuffle(level, Random(9))
        val restored = ProgressCodec.decode(ProgressCodec.encode(store.snapshot))
        assertEquals(setOf("HEAT"), restored.levels[11]?.found)
        assertEquals(setOf("HARE"), restored.levels[11]?.bonus)
        assertEquals(store.snapshot.levels[11]?.wheel, restored.levels[11]?.wheel)
        assertNull(restored.banner)
        assertTrue(restored.completed.isEmpty())
    }

    @Test
    fun reshuffleAvoidsThePreviousOrder() {
        val next = reshuffle("CAT", "CAT", Random(1))
        assertNotEquals("CAT", next)
        assertEquals(listOf('A', 'C', 'T'), next.toList().sorted())
    }

    private fun levelsJson(): String {
        val candidates = listOf(
            File("src/main/assets/levels.json"),
            File("app/src/main/assets/levels.json"),
        )
        return candidates.first { it.exists() }.readText()
    }
}
