package com.assesment.word_puzzle.data

import com.assesment.word_puzzle.domain.canSpell
import com.assesment.word_puzzle.domain.cellsOf
import com.assesment.word_puzzle.domain.isConnected

object LevelValidator {
    fun validate(levels: List<Level>): List<Level> {
        require(levels.size in 10..20) { "Expected 10 to 20 levels, found ${levels.size}" }
        require(levels.map { it.id }.toSet().size == levels.size) { "Level ids must be unique" }
        levels.forEach(::validateLevel)
        return levels.sortedBy { it.id }
    }

    private fun validateLevel(level: Level) {
        val label = "Level ${level.id}"
        require(level.name.isNotBlank()) { "$label needs a name" }
        require(level.letters.length in 3..5) { "$label must use 3 to 5 letters" }
        require(level.letters.all { it in 'A'..'Z' }) { "$label letters must be A-Z" }
        require(level.letters.toSet().size == level.letters.length) { "$label letters must be unique" }
        require(level.placements.size >= 2) { "$label needs at least two grid words" }

        val cells = linkedMapOf<Cell, Char>()
        val seenWords = mutableSetOf<String>()
        for (placement in level.placements) {
            require(placement.word.length >= 3) { "$label word ${placement.word} is too short" }
            require(placement.word.all { it in 'A'..'Z' }) { "$label word ${placement.word} must be A-Z" }
            require(canSpell(level.letters, placement.word)) {
                "$label word ${placement.word} cannot be spelled with ${level.letters}"
            }
            require(seenWords.add(placement.word)) { "$label repeats ${placement.word}" }
            for ((cell, ch) in cellsOf(placement)) {
                val existing = cells[cell]
                require(existing == null || existing == ch) {
                    "$label clashes at $cell ($existing vs $ch)"
                }
                cells[cell] = ch
            }
        }
        require(isConnected(cells.keys)) { "$label crossword is disconnected" }

        val bonusSeen = mutableSetOf<String>()
        for (bonus in level.bonusWords) {
            require(bonus.length >= 3) { "$label bonus $bonus is too short" }
            require(canSpell(level.letters, bonus)) { "$label bonus $bonus cannot be spelled" }
            require(bonus !in level.gridWords) { "$label bonus $bonus is already on the grid" }
            require(bonusSeen.add(bonus)) { "$label repeats bonus $bonus" }
        }
    }
}
