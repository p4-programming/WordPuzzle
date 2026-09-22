package com.assesment.word_puzzle

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.assesment.word_puzzle.data.LevelRepository
import com.assesment.word_puzzle.ui.navigation.WordPuzzleNavHost
import com.assesment.word_puzzle.ui.session.GameSessionViewModel
import com.assesment.word_puzzle.ui.theme.Night
import com.assesment.word_puzzle.ui.theme.WordPuzzleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(Color.parseColor("#071612")))
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            WordPuzzleTheme {
                val context = LocalContext.current
                val levels = remember { LevelRepository.load(context) }
                val session: GameSessionViewModel = viewModel(
                    factory = remember(levels) { GameSessionViewModel.factory(levels) },
                )
                Surface(modifier = Modifier.fillMaxSize(), color = Night) {
                    WordPuzzleNavHost(session)
                }
            }
        }
    }
}
