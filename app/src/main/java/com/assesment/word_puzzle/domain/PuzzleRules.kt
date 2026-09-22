package com.assesment.word_puzzle.domain

import com.assesment.word_puzzle.data.Cell
import com.assesment.word_puzzle.data.Level
import com.assesment.word_puzzle.data.Placement
import kotlin.random.Random

fun canSpell(letters: String, word: String): Boolean {
    if (word.isEmpty()) return false
    val available = letters.groupingBy { it }.eachCount().toMutableMap()
    for (ch in word) {
        val left = available[ch] ?: return false
        if (left == 0) return false
        available[ch] = left - 1
    }
    return true
}

fun cellsOf(placement: Placement): List<Pair<Cell, Char>> =
    placement.word.mapIndexed { index, ch ->
        Cell(
            row = placement.row + if (placement.vertical) index else 0,
            col = placement.col + if (placement.vertical) 0 else index,
        ) to ch
    }

fun solutionCells(level: Level): Map<Cell, Char> {
    val map = linkedMapOf<Cell, Char>()
    for (placement in level.placements) {
        for ((cell, ch) in cellsOf(placement)) {
            val existing = map[cell]
            check(existing == null || existing == ch) {
                "Level ${level.id} clashes at $cell ($existing vs $ch)"
            }
            map[cell] = ch
        }
    }
    return map
}

fun isConnected(cells: Set<Cell>): Boolean {
    if (cells.isEmpty()) return false
    val remaining = cells.toMutableSet()
    val queue = ArrayDeque<Cell>()
    val start = remaining.first()
    queue.add(start)
    remaining.remove(start)
    while (queue.isNotEmpty()) {
        val cell = queue.removeFirst()
        val neighbors = listOf(
            cell.copy(row = cell.row - 1),
            cell.copy(row = cell.row + 1),
            cell.copy(col = cell.col - 1),
            cell.copy(col = cell.col + 1),
        )
        for (neighbor in neighbors) {
            if (remaining.remove(neighbor)) queue.add(neighbor)
        }
    }
    return remaining.isEmpty()
}

fun openingWheel(letters: String, levelId: Int): String {
    if (letters.length <= 1) return letters
    val shuffled = letters.toList().shuffled(Random(levelId * 7919L)).joinToString("")
    return if (shuffled != letters) shuffled else letters.drop(1) + letters.first()
}

fun reshuffle(letters: String, previous: String, random: Random = Random): String {
    if (letters.length <= 1) return letters
    repeat(6) {
        val next = letters.toList().shuffled(random).joinToString("")
        if (next != previous) return next
    }
    val rotated = previous.drop(1) + previous.first()
    return if (rotated != previous) rotated else previous
}

/**
 * Updates the swipe path.
 * Returning to the previous letter undoes the last pick, which keeps fast
 * swipes correctable. A letter already in the path is ignored.
 */
fun updateSelection(current: List<Int>, hit: Int?): List<Int> {
    if (hit == null) return current
    if (current.isEmpty()) return listOf(hit)
    if (hit == current.last()) return current
    if (current.size >= 2 && hit == current[current.size - 2]) return current.dropLast(1)
    if (hit !in current) return current + hit
    return current
}
