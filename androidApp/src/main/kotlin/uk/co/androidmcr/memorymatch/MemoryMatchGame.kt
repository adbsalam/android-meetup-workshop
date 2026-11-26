package uk.co.androidmcr.memorymatch

import kotlin.random.Random

internal enum class GamePhase {
    PLAYING,
    FINISHED,
}

internal enum class CardFeedback {
    MATCH,
    MISMATCH,
}

internal data class PlayerScore(
    val name: String,
    val score: Int = 0,
)

internal data class MemoryCard(
    val id: Int,
    val label: String,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false,
)

internal data class MemoryMatchGame(
    val players: List<PlayerScore>,
    val activePlayerIndex: Int,
    val cards: List<MemoryCard>,
    val selectedCardIds: List<Int> = emptyList(),
    val feedbackCardIds: List<Int> = emptyList(),
    val phase: GamePhase = GamePhase.PLAYING,
    val feedback: CardFeedback? = null,
) {
    val activePlayer: PlayerScore
        get() = players[activePlayerIndex]

    fun selectCard(cardId: Int): MemoryMatchGame {
        if (phase != GamePhase.PLAYING || feedback != null || selectedCardIds.size == 2) {
            return this
        }

        val card = cards.firstOrNull { it.id == cardId } ?: return this
        if (card.isFaceUp || card.isMatched) return this

        val nextSelectedIds = selectedCardIds + cardId
        val nextCards = cards.map { current ->
            if (current.id == cardId) current.copy(isFaceUp = true) else current
        }

        if (nextSelectedIds.size == 1) {
            return copy(cards = nextCards, selectedCardIds = nextSelectedIds)
        }

        val firstCard = nextCards.first { it.id == nextSelectedIds[0] }
        val secondCard = nextCards.first { it.id == nextSelectedIds[1] }
        if (firstCard.label != secondCard.label) {
            return copy(
                cards = nextCards,
                selectedCardIds = emptyList(),
                feedbackCardIds = nextSelectedIds,
                feedback = CardFeedback.MISMATCH,
            )
        }

        val matchedCards = nextCards.map { current ->
            if (current.id in nextSelectedIds) {
                current.copy(isFaceUp = true, isMatched = true)
            } else {
                current
            }
        }
        val nextPlayers = players.mapIndexed { index, player ->
            if (index == activePlayerIndex) player.copy(score = player.score + 1) else player
        }
        return copy(
            players = nextPlayers,
            cards = matchedCards,
            selectedCardIds = emptyList(),
            feedbackCardIds = nextSelectedIds,
            feedback = CardFeedback.MATCH,
        )
    }

    fun resolveFeedback(): MemoryMatchGame {
        val currentFeedback = feedback ?: return this

        val nextCards = cards.map { card ->
            if (currentFeedback == CardFeedback.MISMATCH && card.id in feedbackCardIds) {
                card.copy(isFaceUp = false)
            } else {
                card
            }
        }
        val nextActivePlayerIndex = if (currentFeedback == CardFeedback.MISMATCH) {
            (activePlayerIndex + 1) % players.size
        } else {
            activePlayerIndex
        }
        val isFinished = currentFeedback == CardFeedback.MATCH && nextCards.all { it.isMatched }

        return copy(
            activePlayerIndex = nextActivePlayerIndex,
            cards = nextCards,
            selectedCardIds = emptyList(),
            feedbackCardIds = emptyList(),
            phase = if (isFinished) GamePhase.FINISHED else phase,
            feedback = null,
        )
    }

    companion object {
        private val cardLabels = listOf(
            "Android MCR",
            "Bug",
            "Compose Multiplatform",
            "Colour",
            "Firebase",
            "GDG",
            "Gradle",
            "Kotlin",
            "Kotlin Meetup",
            "Robot",
        )

        fun newGame(
            playerNames: List<String>,
            random: Random = Random.Default,
        ): MemoryMatchGame {
            require(playerNames.size == 2) { "Memory Match requires exactly two players." }

            val cards = cardLabels
                .flatMap { label -> listOf(label, label) }
                .shuffled(random)
                .mapIndexed { index, label -> MemoryCard(id = index, label = label) }

            return MemoryMatchGame(
                players = playerNames.map(::PlayerScore),
                activePlayerIndex = 0,
                cards = cards,
            )
        }
    }
}

internal fun playerNameOrDefault(name: String, playerNumber: Int): String =
    name.trim().ifEmpty { "Player $playerNumber" }
