# Word Puzzle

Word Puzzle is an Android word game in the style of Wordscapes. Each level is a small crossword. The player swipes letters on a wheel to fill the board, and extra words from the same letters count as bonus words.

The UI is Jetpack Compose. The only XML in the project is what the Android build requires: the manifest, the theme, and the launcher resources. Screens, navigation, and game state are Kotlin.

## How to run

1. Open the project in Android Studio.
2. Let Gradle sync. The wrapper is included, so a separate Gradle install is not required.
3. Run the `app` configuration on a device or emulator. Minimum SDK is 24.

Useful checks from the project root:

```bat
gradlew.bat :app:testDebugUnitTest
gradlew.bat :app:assembleDebug
```

On macOS or Linux, use `./gradlew` instead of `gradlew.bat`.

Application id: `com.assesment.word_puzzle`.

## How to play

- **Play** on the home screen opens the first level that is not finished. If every level is clear, it starts again at level 1.
- **Levels** opens all 20 levels. Every level is available from the start. A gold tile means that level is clear.
- Swipe across the letter wheel in order. The word appears above the wheel, and a line follows the finger. Lift to submit.
- A word of at least 3 letters is checked when the finger lifts.
- A grid word fills its tiles. A bonus word is acknowledged and counted, but it is not on the board. A wrong word or a repeat shakes the wheel.
- The shuffle button rearranges the same letters. It does not change the crossword.
- When every grid word is found, the level completes and the next level opens on its own. The last level returns to the level list.
- The pause button, or the system back button during a level, opens Pause. From there the player can resume, restart the level, go to the level list, or go home.

## Screens and back stack

Navigation Compose owns the stack. Routes are `home`, `levels`, `game/{levelId}`, and `pause/{levelId}`.

| Action | Result |
| --- | --- |
| Home → Play | Opens the next unfinished level |
| Home → Levels → a level | Opens that level, with Levels still underneath |
| System back, or the pause control, during a level | Opens Pause on top of that same level |
| Pause → Resume, or system back | Returns to the same level, with its words and wheel unchanged |
| Pause → Restart | Clears that level’s found words, then returns to it |
| Pause → Levels | Returns to Levels if it is already on the stack. Otherwise it opens Levels above Home |
| Pause → Home | Pops back to Home |
| Level completed, and a later level exists | Replaces the current game screen with the next level |
| Last level completed | Returns to the level list |

The stack does not keep two copies of Home, Levels, or the same Pause screen. Moving to the next level removes the finished game screen instead of stacking another one on top.

## Architecture

The app is MVVM. UI code displays state and forwards events. Rules do not live in composables.

```
MainActivity
  └── GameSessionViewModel          activity scope, SavedStateHandle
        └── ProgressStore           pure Kotlin game rules
              └── levels.json       LevelRepository → LevelParser → LevelValidator
```

| Piece | Role |
| --- | --- |
| `ui/home`, `ui/levels`, `ui/game`, `ui/pause` | Compose screens |
| `ui/navigation/WordPuzzleNavHost` | Routes and back-stack behavior |
| `ui/session/GameSessionViewModel` | Screen state. Screens call `submit`, `shuffle`, and `restart` |
| `domain/ProgressStore` | Decides grid, bonus, duplicate, or invalid, and marks a level complete |
| `domain/PuzzleRules` | Spelling, crossword cells, wheel order, and swipe path |
| `data/LevelRepository` | Loads `assets/levels.json`, parses it, then validates it before the first screen |

`GameSessionViewModel` is created once for the activity in `MainActivity`. It is not stored in a single navigation entry, so leaving Pause and coming back does not reset the level.

## State across rotation and process death

Progress is written to `SavedStateHandle` under the key `progress` whenever it changes. `ProgressCodec` stores it as plain text:

```
C=1,2
J=5
L1=CAT,ACT|ERA|TAC
```

- `C` is the set of completed level ids.
- `J` is a level that just completed, so the completion moment can be restored.
- Each `L` line is `found words | bonus words | current wheel order`.

The banner (the short “found” / “bonus” / “invalid” message) is not stored. Found words, bonus words, the shuffled wheel, completed levels, and a just-finished level are restored after rotation and after the process is killed. The opening wheel for a level is derived from the level id, so a fresh level does not reshuffle just because the screen rotated.

## Swipe handling

`LetterWheel` places 3 to 5 letters on a circle and tracks one finger with `awaitEachGesture`.

- A letter is hit when the pointer is inside a radius a little larger than the letter bubble.
- Move events can be far apart on a fast swipe. The segment between the last point and the new point is sampled, so a letter in between is still picked up.
- The selection is an ordered path, not a bag of letters. The word is the letters in swipe order.
- Touching the previous letter undoes the last pick. Touching the same letter again does nothing. A letter already in the path is ignored, so the path cannot cross itself.
- A rounded stroke is drawn through the selected letter centers and out to the finger.
- Selected letters scale up. Releasing the finger submits the word and clears the path.
- An invalid or repeated word shakes the wheel. A growing path gives a light haptic tick.

Words shorter than 3 letters are ignored. A word the wheel cannot spell, or that is neither a grid word nor a bonus word, is invalid.

## Level data

There are 20 levels in `app/src/main/assets/levels.json`. They are loaded at startup. `LevelValidator` rejects the catalog if it is outside 10–20 levels or if a puzzle is inconsistent, so a bad edit fails immediately instead of shipping a broken board.

```json
{
  "id": 1,
  "name": "First Sprout",
  "letters": "CAT",
  "bonusWords": [],
  "placements": [
    { "word": "CAT", "row": 0, "col": 0, "vertical": false },
    { "word": "ACT", "row": 0, "col": 1, "vertical": true }
  ]
}
```

| Field | Meaning |
| --- | --- |
| `id` | Unique level number, used in the route `game/{levelId}` |
| `name` | Title shown on the level and on Pause |
| `letters` | Wheel letters. 3 to 5 unique characters, `A`–`Z` |
| `placements` | Crossword words. `row` and `col` are the start cell. `vertical` is the direction |
| `bonusWords` | Extra words from the same letters. They are not drawn on the board |

Validation for every level:

- At least two grid words, each at least 3 letters, each spellable from the wheel.
- No repeated grid word. Crossing letters must be the same character.
- The crossword is connected on four sides (up, down, left, right). Diagonal-only contact does not count.
- Every bonus word is spellable, at least 3 letters, and not already on the grid.

`row` and `col` start at 0. A horizontal word grows to the right. A vertical word grows downward. Level 1 places `CAT` across the top and `ACT` down the second column, so the `A` is the shared cell.

## Project layout

```
app/src/main/assets/levels.json
app/src/main/java/com/assesment/word_puzzle/
  MainActivity.kt
  data/            models, JSON parser, validator, repository
  domain/          puzzle rules and progress
  ui/home          home screen
  ui/levels        level select
  ui/game          crossword, letter wheel, gameplay
  ui/pause         pause screen
  ui/navigation    NavHost and routes
  ui/session       GameSessionViewModel
  ui/theme         color, type, theme
  ui/components    shared buttons and background
app/src/test/java/com/assesment/word_puzzle/GameLogicTest.kt
```

## Stack

- Kotlin 2.2, Jetpack Compose, Material 3
- Navigation Compose
- Lifecycle `ViewModel` and `SavedStateHandle`
- `kotlinx.serialization` JSON parser for the level file (the levels are read as a JSON tree; there is no generated serializer)
- `minSdk` 24, `targetSdk` 37

## Tests

`GameLogicTest` runs on the JVM and loads the same `levels.json` the app ships. It covers:

- The catalog has 20 valid levels
- The opening wheel is a permutation of the level letters and is not left in the original order
- A swipe can undo the previous letter and will not repeat a letter
- Solving a level marks it complete once
- Bonus, invalid, and too-short words
- Every level can be cleared from its own grid words
- Restart clears a finished level
- Shuffle keeps the same letters
- Progress survives encode and decode
- Reshuffle avoids the previous order
