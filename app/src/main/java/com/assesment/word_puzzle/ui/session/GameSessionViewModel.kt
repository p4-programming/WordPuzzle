package com.assesment.word_puzzle.ui.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.assesment.word_puzzle.data.Level
import com.assesment.word_puzzle.domain.LevelProgress
import com.assesment.word_puzzle.domain.ProgressCodec
import com.assesment.word_puzzle.domain.ProgressStore
import com.assesment.word_puzzle.domain.openingWheel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class GameSessionViewModel(
    val levels: List<Level>,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val levelsById = levels.associateBy { it.id }
    private val store = ProgressStore(ProgressCodec.decode(savedStateHandle[PROGRESS_KEY]))

    var snapshot by mutableStateOf(store.snapshot)
        private set

    fun level(id: Int): Level? = levelsById[id]

    fun progress(levelId: Int): LevelProgress = snapshot.levels[levelId] ?: LevelProgress()

    fun letters(levelId: Int): List<Char> {
        val level = levelsById[levelId] ?: return emptyList()
        val wheel = snapshot.levels[levelId]?.wheel ?: openingWheel(level.letters, level.id)
        return wheel.toList()
    }

    fun nextIncompleteId(): Int {
        val ordered = levels.sortedBy { it.id }
        return ordered.firstOrNull { it.id !in snapshot.completed }?.id ?: ordered.first().id
    }

    fun nextLevelId(current: Int): Int? =
        levels.filter { it.id > current }.minOfOrNull { it.id }

    fun submit(levelId: Int, word: String) {
        val level = levelsById[levelId] ?: return
        mutate { store.submit(level, word) }
    }

    fun shuffle(levelId: Int) {
        val level = levelsById[levelId] ?: return
        mutate { store.shuffle(level) }
    }

    fun restart(levelId: Int) {
        val level = levelsById[levelId] ?: return
        mutate { store.restart(level) }
    }

    fun dismissBanner(nonce: Int) {
        mutate { store.dismissBanner(nonce) }
    }

    fun consumeCompletion() {
        mutate { store.consumeCompletion() }
    }

    private fun mutate(block: () -> Unit) {
        val before = store.snapshot
        block()
        if (store.snapshot == before) return
        snapshot = store.snapshot
        savedStateHandle[PROGRESS_KEY] = ProgressCodec.encode(snapshot)
    }

    companion object {
        private const val PROGRESS_KEY = "progress"

        fun factory(levels: List<Level>): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return GameSessionViewModel(levels, extras.createSavedStateHandle()) as T
            }
        }
    }
}
