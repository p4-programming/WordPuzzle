package com.assesment.word_puzzle.data

import android.content.Context

object LevelRepository {
    fun load(context: Context): List<Level> {
        val raw = context.assets.open("levels.json").bufferedReader().use { it.readText() }
        return LevelValidator.validate(LevelParser.parse(raw))
    }
}
