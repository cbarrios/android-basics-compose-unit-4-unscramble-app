package com.example.android.unscramble.ui

import com.example.android.unscramble.data.MAX_NO_OF_WORDS
import com.example.android.unscramble.data.SCORE_INCREASE
import com.example.android.unscramble.data.getUnscrambledWord
import org.junit.Assert.*
import org.junit.Test

// By default, before each test method is executed, JUnit creates a new instance of this test class.
// Test methods are executed in isolation to avoid unexpected side effects from mutable test instance state.
// Dependencies are created each time a test runs (like our view model)
class GameViewModelTest {

    // This view model instance will get data from the unit testing environment automatically since
    // "allWords" set is imported through the same package name: "com.example.android.unscramble.data"
    // In the view model we have this line: import com.example.android.unscramble.data.allWords
    // Because of that, we get "allWords" from the data package of this testing environment and not from the app environment
    // since the package name in which "allWords" is present is also "com.example.android.unscramble.data"
    // Quick conclusion: Naming conventions are KEY!
    private val viewModel = GameViewModel()

    companion object {
        private const val SCORE_AFTER_FIRST_CORRECT_ANSWER = SCORE_INCREASE
    }

    // The code below uses the thingUnderTest_TriggerOfTest_ResultOfTest
    // format (naming convention) to name the test functions.

    // Success path
    @Test
    fun gameViewModel_CorrectWordGuessed_ScoreUpdatedAndErrorFlagUnset() {
        var currentGameUiState = viewModel.uiState.value
        val correctPlayerWord = getUnscrambledWord(currentGameUiState.currentScrambledWord)
        viewModel.updateUserGuess(correctPlayerWord)
        viewModel.checkUserGuess()

        currentGameUiState = viewModel.uiState.value
        // Assert that checkUserGuess() method updates isGuessedWordWrong is updated correctly.
        assertFalse(currentGameUiState.isGuessedWordWrong)
        // Assert that score is updated correctly.
        assertEquals(SCORE_AFTER_FIRST_CORRECT_ANSWER, currentGameUiState.score)
    }

    // Error path
    @Test
    fun gameViewModel_IncorrectGuess_ErrorFlagSet() {
        // Given an incorrect word as input (this word cannot be in "allWords" from the testing data package)
        // Otherwise, the test is considered "not deterministic", making it an invalid unit test!
        val incorrectPlayerWord = "and"
        viewModel.updateUserGuess(incorrectPlayerWord)
        viewModel.checkUserGuess()

        val currentGameUiState = viewModel.uiState.value
        // Assert that score is unchanged
        assertEquals(0, currentGameUiState.score)
        // Assert that checkUserGuess() method updates isGuessedWordWrong correctly
        assertTrue(currentGameUiState.isGuessedWordWrong)
    }

    // Boundary path (new game)
    @Test
    fun gameViewModel_Initialization_FirstWordLoaded() {
        val currentGameUiState = viewModel.uiState.value
        val scrambledWord = currentGameUiState.currentScrambledWord
        val unScrambledWord = getUnscrambledWord(currentGameUiState.currentScrambledWord)
        val isScrambled =
            scrambledWord.toSortedSet() == unScrambledWord.toSortedSet() && unScrambledWord != scrambledWord

        // Assert that current word is scrambled.
        assertTrue(isScrambled)
        // Assert that current word count is set to 1.
        assertTrue(currentGameUiState.currentWordCount == 1)
        // Assert that initially the score is 0.
        assertTrue(currentGameUiState.score == 0)
        // Assert that the wrong word guessed is false.
        assertFalse(currentGameUiState.isGuessedWordWrong)
        // Assert that game is not over.
        assertFalse(currentGameUiState.isGameOver)
    }

    // Boundary path (game over)
    @Test
    fun gameViewModel_AllWordsGuessed_UiStateUpdatedCorrectly() {
        var expectedScore = 0
        var currentGameUiState = viewModel.uiState.value
        var correctPlayerWord = getUnscrambledWord(currentGameUiState.currentScrambledWord)
        repeat(MAX_NO_OF_WORDS) {
            expectedScore += SCORE_INCREASE
            viewModel.updateUserGuess(correctPlayerWord)
            viewModel.checkUserGuess()
            currentGameUiState = viewModel.uiState.value
            correctPlayerWord = getUnscrambledWord(currentGameUiState.currentScrambledWord)
            // Assert that after each correct answer, score is updated correctly.
            assertEquals(expectedScore, currentGameUiState.score)
        }

        // Assert that after all questions are answered, the current word count is up-to-date.
        assertEquals(MAX_NO_OF_WORDS, currentGameUiState.currentWordCount)
        // Assert that after 10 questions are answered, the game is over.
        assertTrue(currentGameUiState.isGameOver)
    }

    // Boundary path (skip word)
    @Test
    fun gameViewModel_WordSkipped_ScoreUnchangedAndWordCountIncreased() {
        var currentGameUiState = viewModel.uiState.value
        val correctPlayerWord = getUnscrambledWord(currentGameUiState.currentScrambledWord)
        viewModel.updateUserGuess(correctPlayerWord)
        viewModel.checkUserGuess()

        currentGameUiState = viewModel.uiState.value
        val lastWordCount = currentGameUiState.currentWordCount
        viewModel.skipWord()
        currentGameUiState = viewModel.uiState.value
        // Assert that score remains unchanged after word is skipped.
        assertEquals(SCORE_AFTER_FIRST_CORRECT_ANSWER, currentGameUiState.score)
        // Assert that word count is increased by 1 after word is skipped.
        assertEquals(lastWordCount + 1, currentGameUiState.currentWordCount)
    }
}