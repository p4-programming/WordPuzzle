package com.assesment.word_puzzle

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.assesment.word_puzzle.data.LevelRepository
import com.assesment.word_puzzle.ui.navigation.WordPuzzleNavHost
import com.assesment.word_puzzle.ui.session.GameSessionViewModel
import com.assesment.word_puzzle.ui.theme.Night
import com.assesment.word_puzzle.ui.theme.WordPuzzleTheme

class MainActivity : ComponentActivity() {
    private val session: GameSessionViewModel by viewModels {
        GameSessionViewModel.factory(LevelRepository.load(applicationContext))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(Color.parseColor("#071612")))
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            WordPuzzleTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Night) {
                    WordPuzzleNavHost(session)
                }
            }
        }
    }
}
