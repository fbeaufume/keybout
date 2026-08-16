---
name: add-game-style
description: Explains how to add a new GameStyle (e.g. regular, hidden, anagram, calculus) to the game. Use when asked to add, create, or implement a new game style, word-display effect, or word-generation mode, or when asked how styles work in this project.
---

# Adding a game style to keybout

A **GameStyle** controls how the words/values a player must type are generated and displayed
(e.g. `regular` shows the word as-is, `anagram` shuffles letters, `calculus` shows a math operation instead of a word).

## Steps for a letter style (transforms an existing dictionary word)

1. **[GameStyle.kt](keybout-backend/src/main/kotlin/com/adeliosys/keybout/model/GameStyle.kt)**
   Add a new enum constant with `@SerializedName("yourstyle")` (the wire-format string, must
   match the frontend value exactly), `isLetterStyle = true`, a `delay` (seconds of per-word
   round-expiration budget — compare to the existing styles' 5L/10L/20L to judge how hard the
   style is to read), and override `transform(value, difficulty)` with the logic that turns the
   dictionary word into the displayed string.

2. **[Difficulty.kt](keybout-backend/src/main/kotlin/com/adeliosys/keybout/model/Difficulty.kt)**
   Add an entry for your new style to the `letterRanges` map, for **each** of `EASY`, `NORMAL`,
   and `HARD`. This is easy to miss because nothing else in this task points here directly, but
   `DictionaryService.generateWords()` calls `difficulty.getLetterRange(style)` which does
   `letterRanges[style]!!` — skip this step and every round of the new style throws a
   `NullPointerException` at runtime the first time it's played.

3. **[play/model.ts](keybout-frontend/src/app/play/model.ts)**
   Add the value to the `GameStyle` enum (string must match the backend `@SerializedName`
   exactly — there is no shared schema/codegen between Kotlin and TypeScript, so a mismatch
   silently falls back to `REGULAR` via `GameStyle.getByCode`), add it to the `GameStyles` array,
   and add a `{pascal, lower}` entry to `GameStyleLabels`. The game-creation dropdown
   (`games.component.html`) and score pages iterate these generically, so no other frontend
   wiring is needed for a letter style.

4. **[documentation.component.html](keybout-frontend/src/app/documentation/documentation.component.html)**
   Add a new `<li>` under the "Style" list following the existing pattern (name in
   `<span class="text-primary">`, one-line description, an example in a
   `<button class="btn btn-primary">`). Update the "Difficulty" paragraph below it too if
   difficulty affects your style differently than "longer words at higher difficulty".

That's it for a letter style — `ScoreService`, `ScoreController`, `Score.kt`'s `GameType`/
`TopScores`, `BaseGameService`, and `DictionaryService` all key generically off the `GameStyle`
enum and `isLetterStyle` flag, so they pick up the new style automatically.

## Extra steps for a generator style (invents its own values, like calculus)

Do everything above with `isLetterStyle = false`, plus:

5. **[BaseGameService.kt](keybout-backend/src/main/kotlin/com/adeliosys/keybout/service/BaseGameService.kt)**
   `generateWords()` has a `when (style) { GameStyle.CALCULUS -> ... ; else -> dictionaryService... }`
   dispatch. Add a branch for your new style that calls your new generator service instead of
   `dictionaryService`.

6. **[CalculusService.kt](keybout-backend/src/main/kotlin/com/adeliosys/keybout/service/CalculusService.kt)**
   Use this as the template for your own `@Service`. It returns `Pair<List<Word>, Int>` (the
   generated `Word(value, display)` list — `value` is what the player types, `display` is what's
   shown — plus an attempt count), and de-duplicates via `Word.conflictsWith`. Inject your new
   service into `BaseGameService`'s constructor the same way `calculusService` is. Add whatever
   range/config fields your generator needs to `Difficulty.kt` (parallel to `additionRanges`/
   `subtractionRanges`), rather than reusing `letterRanges`.

7. **[Language.kt](keybout-backend/src/main/kotlin/com/adeliosys/keybout/model/Language.kt)**
   `getByCode(code, style)` forces `Language.NONE` when `style == GameStyle.CALCULUS`, since a
   math operation isn't in any language. If your generator style is likewise language-independent,
   add it to that check (or generalize the check to `!style.isLetterStyle` if every generator
   style should behave this way).

8. **Frontend "no language" UI checks** — several components hardcode
   `=== GameStyle.CALCULUS` / `!= 'calculus'` to hide the language field or label for
   language-less styles. If your new style is language-independent, extend (or generalize) these:
   - [scores/scores.component.ts](keybout-frontend/src/app/scores/scores.component.ts)
   - [play/games/games.component.ts](keybout-frontend/src/app/play/games/games.component.ts)
   - [play/start/start.component.html](keybout-frontend/src/app/play/start/start.component.html)
   - [play/results/results.component.html](keybout-frontend/src/app/play/results/results.component.html)

   Consider replacing the `=== CALCULUS` checks with a generic `!style.isLetterStyle`-style test
   if you're adding a second generator style — otherwise every future generator style needs the
   same four files edited again.

## Testing

- [GameStyleTest.kt](keybout-backend/src/test/kotlin/com/adeliosys/keybout/model/GameStyleTest.kt) —
  add a case for your style's `transform()` (letter styles) or generation logic (generator styles).
- [DictionaryServiceTest.kt](keybout-backend/src/test/kotlin/com/adeliosys/keybout/service/DictionaryServiceTest.kt) —
  iterates `GameStyle.letterStyles`, so a letter style is covered automatically once step 2 (the
  `letterRanges` entry) is done; nothing to add here.
- A new generator style needs its own test file, analogous to how `CalculusService` has no
  dedicated test today but arguably should (check for one before assuming there isn't).

## Quick checklist

- [ ] `GameStyle.kt`: enum constant, `@SerializedName`, `isLetterStyle`, `delay`, `transform`
- [ ] `Difficulty.kt`: `letterRanges` entry (letter style) or new range fields (generator style)
- [ ] `play/model.ts`: enum value, `GameStyles`, `GameStyleLabels`
- [ ] `documentation.component.html`: new `<li>` bullet + Difficulty paragraph if needed
- [ ] Generator style only: `BaseGameService.generateWords()` branch + new `@Service`
- [ ] Generator style only: `Language.kt` language-less check, if applicable
- [ ] Generator style only: `scores.component.ts` / `games.component.ts` / `start.component.html` /
      `results.component.html` language-less UI checks, if applicable
- [ ] Tests: `GameStyleTest.kt`, plus a new test file for a generator style
