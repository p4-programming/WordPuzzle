package com.assesment.word_puzzle.data

data class Level(
    val id: Int,
    val name: String,
    val letters: String,
    val placements: List<Placement>,
    val bonusWords: List<String>,
) {
    val gridWords: Set<String> = placements.map { it.word }.toSet()
}

data class Placement(
    val word: String,
    val row: Int,
    val col: Int,
    val vertical: Boolean,
)

data class Cell(val row: Int, val col: Int)
