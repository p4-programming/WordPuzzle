package com.assesment.word_puzzle.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.assesment.word_puzzle.ui.game.GameScreen
import com.assesment.word_puzzle.ui.home.HomeScreen
import com.assesment.word_puzzle.ui.levels.LevelSelectScreen
import com.assesment.word_puzzle.ui.pause.PauseScreen
import com.assesment.word_puzzle.ui.session.GameSessionViewModel

object Routes {
    const val Home = "home"
    const val Levels = "levels"
    const val Game = "game/{levelId}"
    const val Pause = "pause/{levelId}"

    fun game(id: Int) = "game/$id"
    fun pause(id: Int) = "pause/$id"
}

@Composable
fun WordPuzzleNavHost(session: GameSessionViewModel) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.Home,
        enterTransition = {
            if (targetState.destination.route == Routes.Pause) {
                fadeIn(tween(220)) + scaleIn(initialScale = 0.96f, animationSpec = tween(220))
            } else {
                fadeIn(tween(280)) + slideInHorizontally(tween(380)) { it / 5 }
            }
        },
        exitTransition = { fadeOut(tween(180)) },
        popEnterTransition = { fadeIn(tween(220)) },
        popExitTransition = {
            fadeOut(tween(200)) + slideOutHorizontally(tween(320)) { it / 5 }
        },
    ) {
        composable(Routes.Home) {
            HomeScreen(
                session = session,
                onPlay = { levelId ->
                    navController.navigate(Routes.game(levelId)) { launchSingleTop = true }
                },
                onLevels = {
                    navController.navigate(Routes.Levels) { launchSingleTop = true }
                },
            )
        }
        composable(Routes.Levels) {
            LevelSelectScreen(
                session = session,
                onBack = { navController.popBackStack() },
                onLevel = { levelId ->
                    navController.navigate(Routes.game(levelId)) { launchSingleTop = true }
                },
            )
        }
        composable(
            route = Routes.Game,
            arguments = listOf(navArgument("levelId") { type = NavType.IntType }),
        ) { entry ->
            val levelId = entry.arguments?.getInt("levelId") ?: return@composable
            GameScreen(
                levelId = levelId,
                session = session,
                onPause = {
                    navController.navigate(Routes.pause(levelId)) { launchSingleTop = true }
                },
                onNextLevel = { nextId ->
                    navController.navigate(Routes.game(nextId)) {
                        popUpTo(Routes.game(levelId)) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onAllClear = {
                    val returned = navController.popBackStack(Routes.Levels, inclusive = false)
                    if (!returned) {
                        navController.navigate(Routes.Levels) {
                            popUpTo(Routes.Home) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
            )
        }
        composable(
            route = Routes.Pause,
            arguments = listOf(navArgument("levelId") { type = NavType.IntType }),
        ) { entry ->
            val levelId = entry.arguments?.getInt("levelId") ?: return@composable
            val level = session.level(levelId)
            PauseScreen(
                levelNumber = levelId,
                levelName = level?.name ?: "Puzzle",
                onResume = { navController.popBackStack() },
                onRestart = {
                    session.restart(levelId)
                    navController.popBackStack()
                },
                onLevels = {
                    val returned = navController.popBackStack(Routes.Levels, inclusive = false)
                    if (!returned) {
                        navController.navigate(Routes.Levels) {
                            popUpTo(Routes.Home) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
                onHome = { navController.popBackStack(Routes.Home, inclusive = false) },
            )
        }
    }
}
