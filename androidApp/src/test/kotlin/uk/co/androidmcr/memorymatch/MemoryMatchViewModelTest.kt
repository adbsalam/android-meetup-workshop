package uk.co.androidmcr.memorymatch

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

class MemoryMatchViewModelTest {
    @Test
    fun completingGameRecordsTheTimeFromTheInjectedClock() {
        val startedAt = Instant.fromEpochMilliseconds(1_000)
        val finishedAt = Instant.fromEpochMilliseconds(2_000)
        val clock = mockk<Clock>()
        every { clock.now() } returnsMany listOf(startedAt, finishedAt)
        val viewModel = MemoryMatchViewModel(clock = clock, random = Random(3))

        viewModel.dispatch(MemoryMatchIntent.StartGame)
        viewModel.uiState.value.game.cards
            .groupBy { it.label }
            .values
            .forEach { pair ->
                viewModel.dispatch(MemoryMatchIntent.SelectCard(pair[0].id))
                viewModel.dispatch(MemoryMatchIntent.SelectCard(pair[1].id))
                viewModel.dispatch(MemoryMatchIntent.FeedbackElapsed)
            }

        assertEquals(GameScreen.GAME_OVER, viewModel.uiState.value.screen)
        assertEquals(finishedAt, viewModel.uiState.value.sessionFinishedAt)
        verify(exactly = 2) { clock.now() }
    }

    @Test
    fun intentsUpdateTheSharedUiState() {
        val now = Instant.fromEpochMilliseconds(1_000)
        val viewModel = MemoryMatchViewModel(
            clock = FixedClock(now),
            timeZoneProvider = { TimeZone.UTC },
            random = Random(1),
        )

        viewModel.dispatch(MemoryMatchIntent.PlayerOneNameChanged("Alex"))
        viewModel.dispatch(MemoryMatchIntent.StartPlayerSetup)
        viewModel.dispatch(MemoryMatchIntent.StartGame)

        val state = viewModel.uiState.value
        assertEquals(GameScreen.PLAYING, state.screen)
        assertEquals("Alex", state.game.players[0].name)
        assertEquals(now, state.sessionStartedAt)
        assertEquals(TimeZone.UTC, state.sessionTimeZone)
        assertEquals(20, state.game.cards.size)
    }

    @Test
    fun matchFeedbackLocksCardInputUntilTheElapsedIntent() {
        val viewModel = MemoryMatchViewModel(
            clock = FixedClock(Instant.fromEpochMilliseconds(1_000)),
            timeZoneProvider = { TimeZone.UTC },
            random = Random(2),
        )
        viewModel.dispatch(MemoryMatchIntent.StartGame)
        val game = viewModel.uiState.value.game
        val pair = game.cards.groupBy { it.label }.values.first()

        viewModel.dispatch(MemoryMatchIntent.SelectCard(pair[0].id))
        viewModel.dispatch(MemoryMatchIntent.SelectCard(pair[1].id))
        val feedbackState = viewModel.uiState.value
        val otherCardId = feedbackState.game.cards.first {
            !it.isFaceUp && !it.isMatched
        }.id
        viewModel.dispatch(MemoryMatchIntent.SelectCard(otherCardId))

        assertEquals(CardFeedback.MATCH, feedbackState.game.feedback)
        assertEquals(1, feedbackState.game.players[0].score)
        assertEquals(feedbackState, viewModel.uiState.value)

        viewModel.dispatch(MemoryMatchIntent.FeedbackElapsed)

        assertEquals(null, viewModel.uiState.value.game.feedback)
        assertEquals(0, viewModel.uiState.value.game.activePlayerIndex)
    }

    @Test
    fun backIntentShowsAndDismissesTheExitDialogDuringPlay() {
        val viewModel = MemoryMatchViewModel(
            clock = FixedClock(Instant.fromEpochMilliseconds(1_000)),
            timeZoneProvider = { TimeZone.UTC },
            random = Random(3),
        )

        viewModel.dispatch(MemoryMatchIntent.BackRequested)
        assertFalse(viewModel.uiState.value.showExitDialog)

        viewModel.dispatch(MemoryMatchIntent.StartGame)
        viewModel.dispatch(MemoryMatchIntent.BackRequested)
        assertTrue(viewModel.uiState.value.showExitDialog)

        viewModel.dispatch(MemoryMatchIntent.DismissExitDialog)
        assertFalse(viewModel.uiState.value.showExitDialog)
    }
}

private class FixedClock(private val instant: Instant) : Clock {
    override fun now(): Instant = instant
}
