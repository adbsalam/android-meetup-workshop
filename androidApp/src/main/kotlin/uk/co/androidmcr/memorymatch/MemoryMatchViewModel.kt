package uk.co.androidmcr.memorymatch

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.TimeZone
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Instant

internal enum class GameScreen {
    WELCOME,
    PLAYER_SETUP,
    PLAYING,
    GAME_OVER,
}

internal data class MemoryMatchUiState(
    val screen: GameScreen = GameScreen.WELCOME,
    val playerOneName: String = "Player 1",
    val playerTwoName: String = "Player 2",
    val game: MemoryMatchGame = MemoryMatchGame.newGame(listOf("Player 1", "Player 2")),
    val sessionStartedAt: Instant? = null,
    val sessionTimeZone: TimeZone? = null,
    val sessionFinishedAt: Instant? = null,
    val showExitDialog: Boolean = false,
)

internal sealed interface MemoryMatchIntent {
    data object StartPlayerSetup : MemoryMatchIntent
    data class PlayerOneNameChanged(val name: String) : MemoryMatchIntent
    data class PlayerTwoNameChanged(val name: String) : MemoryMatchIntent
    data object StartGame : MemoryMatchIntent
    data class SelectCard(val cardId: Int) : MemoryMatchIntent
    data object FeedbackElapsed : MemoryMatchIntent
    data object BackRequested : MemoryMatchIntent
    data object DismissExitDialog : MemoryMatchIntent
    data object LeaveGame : MemoryMatchIntent
    data object Rematch : MemoryMatchIntent
    data object ReturnHome : MemoryMatchIntent
}

internal class MemoryMatchViewModel(
    private val clock: Clock = Clock.System,
    private val timeZoneProvider: () -> TimeZone = { TimeZone.currentSystemDefault() },
    private val random: Random = Random.Default,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MemoryMatchUiState())
    val uiState: StateFlow<MemoryMatchUiState> = _uiState.asStateFlow()

    fun dispatch(intent: MemoryMatchIntent) {
        val state = _uiState.value
        _uiState.value = when (intent) {
            MemoryMatchIntent.StartPlayerSetup ->
                state.copy(screen = GameScreen.PLAYER_SETUP)

            is MemoryMatchIntent.PlayerOneNameChanged ->
                state.copy(playerOneName = intent.name)

            is MemoryMatchIntent.PlayerTwoNameChanged ->
                state.copy(playerTwoName = intent.name)

            MemoryMatchIntent.StartGame,
            MemoryMatchIntent.Rematch,
            -> startGame(state)

            is MemoryMatchIntent.SelectCard -> {
                val nextGame = state.game.selectCard(intent.cardId)
                if (nextGame == state.game) state else state.copy(game = nextGame)
            }

            MemoryMatchIntent.FeedbackElapsed -> resolveFeedback(state)

            MemoryMatchIntent.BackRequested ->
                if (state.screen == GameScreen.PLAYING) {
                    state.copy(showExitDialog = true)
                } else {
                    state
                }

            MemoryMatchIntent.DismissExitDialog ->
                state.copy(showExitDialog = false)

            MemoryMatchIntent.LeaveGame -> leaveGame(state)

            MemoryMatchIntent.ReturnHome ->
                state.copy(screen = GameScreen.WELCOME)
        }
    }

    private fun startGame(state: MemoryMatchUiState): MemoryMatchUiState {
        val names = listOf(
            playerNameOrDefault(state.playerOneName, 1),
            playerNameOrDefault(state.playerTwoName, 2),
        )
        return state.copy(
            screen = GameScreen.PLAYING,
            game = MemoryMatchGame.newGame(names, random),
            sessionStartedAt = clock.now(),
            sessionTimeZone = timeZoneProvider(),
            sessionFinishedAt = null,
            showExitDialog = false,
        )
    }

    private fun resolveFeedback(state: MemoryMatchUiState): MemoryMatchUiState {
        val nextGame = state.game.resolveFeedback()
        if (nextGame == state.game) return state

        val gameFinished = nextGame.phase == GamePhase.FINISHED
        return state.copy(
            screen = if (gameFinished) GameScreen.GAME_OVER else state.screen,
            game = nextGame,
            sessionFinishedAt = if (gameFinished) clock.now() else state.sessionFinishedAt,
        )
    }

    private fun leaveGame(state: MemoryMatchUiState): MemoryMatchUiState {
        val names = listOf(
            playerNameOrDefault(state.playerOneName, 1),
            playerNameOrDefault(state.playerTwoName, 2),
        )
        return state.copy(
            screen = GameScreen.WELCOME,
            game = MemoryMatchGame.newGame(names, random),
            sessionStartedAt = null,
            sessionTimeZone = null,
            sessionFinishedAt = null,
            showExitDialog = false,
        )
    }
}
