package uk.co.androidmcr.memorymatch

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MemoryMatchGameTest {
    @Test
    fun newGameCreatesTenPairs() {
        val game = MemoryMatchGame.newGame(listOf("Alex", "Sam"), Random(0))

        assertEquals(20, game.cards.size)
        assertTrue(game.cards.groupBy { it.label }.values.all { it.size == 2 })
    }

    @Test
    fun matchingCardsAwardPointAndKeepTheTurn() {
        val game = MemoryMatchGame.newGame(listOf("Alex", "Sam"), Random(1))
        val matchingCards = game.cards.groupBy { it.label }.values.first()

        val next = game
            .selectCard(matchingCards[0].id)
            .selectCard(matchingCards[1].id)

        assertEquals(1, next.players[0].score)
        assertEquals(0, next.activePlayerIndex)
        assertTrue(next.cards.filter { it.label == matchingCards[0].label }.all { it.isMatched })
        assertEquals(CardFeedback.MATCH, next.feedback)
        val otherCardId = game.cards.first {
            it.id != matchingCards[0].id && it.id != matchingCards[1].id
        }.id
        assertEquals(next, next.selectCard(otherCardId))

        val afterFeedback = next.resolveFeedback()
        assertEquals(0, afterFeedback.activePlayerIndex)
        assertEquals(null, afterFeedback.feedback)
    }

    @Test
    fun mismatchTurnsCardsDownAndPassesTheTurn() {
        val game = MemoryMatchGame.newGame(listOf("Alex", "Sam"), Random(2))
        val differentCards = game.cards.distinctBy { it.label }.take(2)
        val mismatch = game
            .selectCard(differentCards[0].id)
            .selectCard(differentCards[1].id)
        val blockedMove = mismatch.selectCard(game.cards.first {
            it.id != differentCards[0].id && it.id != differentCards[1].id
        }.id)

        val next = mismatch.resolveFeedback()

        assertEquals(mismatch, blockedMove)
        assertEquals(CardFeedback.MISMATCH, mismatch.feedback)
        assertEquals(1, next.activePlayerIndex)
        assertTrue(next.selectedCardIds.isEmpty())
        assertEquals(null, next.feedback)
        assertTrue(next.cards.filter { it.id in mismatch.feedbackCardIds }.none { it.isFaceUp })
    }

    @Test
    fun completingEveryPairFinishesTheGame() {
        var game = MemoryMatchGame.newGame(listOf("Alex", "Sam"), Random(3))

        game.cards.groupBy { it.label }.values.forEach { pair ->
            game = game
                .selectCard(pair[0].id)
                .selectCard(pair[1].id)
                .resolveFeedback()
        }

        assertEquals(GamePhase.FINISHED, game.phase)
        assertEquals(10, game.players.sumOf { it.score })
    }

    @Test
    fun blankPlayerNamesUseNumberedDefaults() {
        assertEquals("Player 1", playerNameOrDefault("  ", 1))
        assertEquals("Alex", playerNameOrDefault(" Alex ", 1))
    }
}
