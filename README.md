# Word Puzzle

A Wordscapes-style crossword for Android. The board is a crossword, the letters sit on a wheel, and a continuous swipe spells a word. The UI is Jetpack Compose. Manifest, theme, and launcher icon XML stay only where the Android build system requires them.

## Architecture

The game uses MVVM.

- `GameSessionViewModel` is the single source of UI state. Screens read `snapshot` and send events (`submit`, `shuffle`, `restart`).
- `ProgressStore` is plain Kotlin. It decides whether a swipe is a grid word, a bonus word, a duplicate, or invalid, and it marks a level complete when every grid word is found.
- Navigation Compose owns the back stack: **Home → Levels → Game → Pause**. The system back button on the game screen opens Pause. Back from Pause returns to the same game. Pause actions pop to Levels or Home without pushing a second copy of those screens.
- The view model is stored on the activity and writes progress into `SavedStateHandle`. Rotation, backgrounding, and process death restore found words, bonus words, the shuffled wheel, and a level that was just cleared.

## Swipe handling

`LetterWheel` lays letters on a circle and tracks the pointer with `awaitEachGesture`.

- Hit testing uses a radius slightly larger than each letter.
- Fast swipes are sampled along the segment between pointer events, so a letter is not skipped when move events are sparse.
- Touching the previous letter removes the last pick. A letter already used in the current word is ignored.
- A rounded stroke follows the selected letter centers and the finger.
- Selected letters scale up. An invalid or repeated word shakes the wheel. Releasing the finger submits the word.

## Level data

Twenty levels live in `app/src/main/assets/levels.json` and are parsed at startup.

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

`letters` is the wheel (3–5 unique letters). `placements` are the crossword words, with `row`, `col`, and `vertical`. `bonusWords` are real words from those letters that are not on the board. `LevelValidator` checks intersections, connectivity, and that every word can be spelled from the wheel. `GameLogicTest` loads this file on the JVM.

Clearing the crossword opens the next level on its own. The last level returns to the level list.
