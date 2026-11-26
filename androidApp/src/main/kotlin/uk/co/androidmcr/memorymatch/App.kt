package uk.co.androidmcr.memorymatch

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

private val PlayerOneColor = Color(0xFFE7F4DF)
private val PlayerTwoColor = Color(0xFFE1EFFB)
private val MismatchColor = Color(0xFFC62828)
private val MatchColor = Color(0xFF2E7D32)

@Composable
@Preview
fun App() {
    MaterialTheme {
        val viewModel = viewModel { MemoryMatchViewModel() }
        val uiState by viewModel.uiState.collectAsState()
        val targetBackground = if (uiState.game.activePlayerIndex == 0) PlayerOneColor else PlayerTwoColor
        val backgroundColor by animateColorAsState(targetValue = targetBackground, label = "player-background")

        BackHandler(enabled = uiState.screen == GameScreen.PLAYING) {
            viewModel.dispatch(MemoryMatchIntent.BackRequested)
        }

        LaunchedEffect(uiState.game.feedback) {
            if (uiState.game.feedback != null) {
                delay(1_000)
                viewModel.dispatch(MemoryMatchIntent.FeedbackElapsed)
            }
        }

        Column(
            modifier = Modifier
                .onPreviewKeyEvent { keyEvent ->
                    if (
                        uiState.screen == GameScreen.PLAYING &&
                        keyEvent.type == KeyEventType.KeyUp &&
                        keyEvent.key == Key.Escape
                    ) {
                        viewModel.dispatch(MemoryMatchIntent.BackRequested)
                        true
                    } else {
                        false
                    }
                }
                .background(backgroundColor)
                .safeContentPadding()
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            when (uiState.screen) {
                GameScreen.WELCOME -> WelcomeScreen(
                    onStart = {
                        viewModel.dispatch(MemoryMatchIntent.StartPlayerSetup)
                    },
                )

                GameScreen.PLAYER_SETUP -> PlayerSetupScreen(
                    playerOneName = uiState.playerOneName,
                    onPlayerOneNameChange = {
                        viewModel.dispatch(MemoryMatchIntent.PlayerOneNameChanged(it))
                    },
                    playerTwoName = uiState.playerTwoName,
                    onPlayerTwoNameChange = {
                        viewModel.dispatch(MemoryMatchIntent.PlayerTwoNameChanged(it))
                    },
                    onStart = { viewModel.dispatch(MemoryMatchIntent.StartGame) },
                    onBack = { viewModel.dispatch(MemoryMatchIntent.ReturnHome) },
                )

                GameScreen.PLAYING -> GameScreenContent(
                    game = uiState.game,
                    onSelectCard = { viewModel.dispatch(MemoryMatchIntent.SelectCard(it)) },
                )

                GameScreen.GAME_OVER -> GameOverScreen(
                    game = uiState.game,
                    startedAt = uiState.sessionStartedAt,
                    timeZone = uiState.sessionTimeZone,
                    finishedAt = uiState.sessionFinishedAt,
                    onRematch = { viewModel.dispatch(MemoryMatchIntent.Rematch) },
                    onHome = { viewModel.dispatch(MemoryMatchIntent.ReturnHome) },
                )
            }
        }

        if (uiState.showExitDialog) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.dispatch(MemoryMatchIntent.DismissExitDialog)
                },
                title = { Text(stringResource(R.string.leave_game_title)) },
                text = { Text(stringResource(R.string.leave_game_confirmation)) },
                confirmButton = {
                    TextButton(onClick = { viewModel.dispatch(MemoryMatchIntent.LeaveGame) }) {
                        Text(stringResource(R.string.leave))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.dispatch(MemoryMatchIntent.DismissExitDialog)
                    }) {
                        Text(stringResource(R.string.stay))
                    }
                },
            )
        }
    }
}

@Composable
private fun WelcomeScreen(onStart: () -> Unit) {
    var showRules by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.memory_match_title), style = MaterialTheme.typography.headlineLarge)
        Text(
            stringResource(R.string.memory_match_description),
            textAlign = TextAlign.Center,
        )
        Button(onClick = onStart) {
            Text(stringResource(R.string.start_game))
        }
        Box {
            TextButton(onClick = { showRules = true }) {
                Text(stringResource(R.string.how_to_play))
            }
            DropdownMenu(
                expanded = showRules,
                onDismissRequest = { showRules = false },
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 320.dp)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(stringResource(R.string.take_turns_flipping_cards))
                    Text(stringResource(R.string.find_matching_cards))
                    Text(stringResource(R.string.most_matches_wins))
                    Text(stringResource(R.string.non_matching_cards))
                    TextButton(onClick = { showRules = false }) {
                        Text(stringResource(R.string.close))
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerSetupScreen(
    playerOneName: String,
    onPlayerOneNameChange: (String) -> Unit,
    playerTwoName: String,
    onPlayerTwoNameChange: (String) -> Unit,
    onStart: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.widthIn(max = 300.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.choose_your_players), style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = playerOneName,
            onValueChange = onPlayerOneNameChange,
            label = { Text(stringResource(R.string.player_one)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = playerTwoName,
            onValueChange = onPlayerTwoNameChange,
            label = { Text(stringResource(R.string.player_two)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.start_matching))
        }
        TextButton(onClick = onBack) {
            Text(stringResource(R.string.back))
        }
    }
}

@Composable
private fun GameScreenContent(
    game: MemoryMatchGame,
    onSelectCard: (Int) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.memory_match_title), style = MaterialTheme.typography.headlineMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlayerScoreCard(
                player = game.players[0],
                isActive = game.activePlayerIndex == 0,
                modifier = Modifier.weight(1f),
            )
            PlayerScoreCard(
                player = game.players[1],
                isActive = game.activePlayerIndex == 1,
                modifier = Modifier.weight(1f),
            )
        }
        Text(stringResource(R.string.players_turn, game.activePlayer.name))
        AdaptiveCardBoard(game = game, onSelectCard = onSelectCard)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (game.feedback) {
                CardFeedback.MATCH -> Text(stringResource(R.string.match), color = MatchColor)
                CardFeedback.MISMATCH -> Text(stringResource(R.string.not_a_match), color = MismatchColor)
                null -> Unit
            }
        }
    }
}

@Composable
private fun PlayerScoreCard(
    player: PlayerScore,
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        border = if (isActive) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(player.name, textAlign = TextAlign.Center, maxLines = 1)
            Text(stringResource(R.string.score, player.score))
        }
    }
}

@Composable
private fun AdaptiveCardBoard(
    game: MemoryMatchGame,
    onSelectCard: (Int) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .widthIn(max = 640.dp)
            .fillMaxWidth(),
    ) {
        val columnCount = ((maxWidth.value + 8f) / 72f).toInt().coerceIn(1, 6)

        LazyVerticalGrid(
            columns = GridCells.Fixed(columnCount),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(game.cards, key = { card -> card.id }) { card ->
                MemoryCardView(
                    card = card,
                    feedback = game.feedback,
                    feedbackCardIds = game.feedbackCardIds,
                    onClick = { onSelectCard(card.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                )
            }
        }
    }
}

@Composable
private fun MemoryCardView(
    card: MemoryCard,
    feedback: CardFeedback?,
    feedbackCardIds: List<Int>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isMatchingCard = feedback == CardFeedback.MATCH && card.id in feedbackCardIds
    val isMismatchedCard = feedback == CardFeedback.MISMATCH && card.id in feedbackCardIds
    val isFaceRevealed = card.isFaceUp || card.isMatched
    val imageResource = if (isFaceRevealed) {
        cardArtwork(card.label)
    } else {
        R.drawable.card_back
    }
    val painter = painterResource(imageResource)
    val rotationY by animateFloatAsState(
        targetValue = if (card.isFaceUp || card.isMatched) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "card-flip",
    )

    Card(
        onClick = onClick,
        enabled = !card.isFaceUp && !card.isMatched && feedback == null,
        modifier = modifier.graphicsLayer {
            this.rotationY = rotationY
            cameraDistance = 12f * density
        },
        shape = RoundedCornerShape(10.dp),
        border = when {
            isMatchingCard -> BorderStroke(3.dp, MatchColor)
            isMismatchedCard -> BorderStroke(3.dp, MismatchColor)
            else -> null
        },
        colors = CardDefaults.cardColors(
            containerColor = when {
                card.isMatched -> Color(0xFFD8EBD2)
                card.isFaceUp -> Color.White
                else -> MaterialTheme.colorScheme.primary
            },
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (isFaceRevealed) card.label else stringResource(R.string.face_down_card),
                modifier = Modifier.graphicsLayer {
                    if (rotationY > 90f) this.rotationY = 180f
                },
                color = if (isFaceRevealed) Color.DarkGray else Color.White,
                textAlign = TextAlign.Center,
                fontSize = 9.sp,
                lineHeight = 11.sp,
                maxLines = 3,
            )
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        if (rotationY > 90f) this.rotationY = 180f
                    },
            )
        }
    }
}

private fun cardArtwork(label: String): Int = when (label) {
    "Android MCR" -> R.drawable.android_mcr_logo
    "Bug" -> R.drawable.bug_logo
    "Compose Multiplatform" -> R.drawable.cmp_logo
    "Colour" -> R.drawable.colour_logo
    "Firebase" -> R.drawable.firebase_logo
    "GDG" -> R.drawable.gdg_logo
    "Gradle" -> R.drawable.gradle_logo
    "Kotlin" -> R.drawable.kotlin_logo
    "Kotlin Meetup" -> R.drawable.kotlin_meet_logo
    "Robot" -> R.drawable.robot_logo
    else -> error("Unknown card label: $label")
}

@Composable
private fun GameOverScreen(
    game: MemoryMatchGame,
    startedAt: Instant?,
    timeZone: TimeZone?,
    finishedAt: Instant?,
    onRematch: () -> Unit,
    onHome: () -> Unit,
) {
    val firstPlayer = game.players[0]
    val secondPlayer = game.players[1]
    val result = when {
        firstPlayer.score > secondPlayer.score -> stringResource(R.string.player_wins, firstPlayer.name)
        secondPlayer.score > firstPlayer.score -> stringResource(R.string.player_wins, secondPlayer.name)
        else -> stringResource(R.string.draw)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(stringResource(R.string.game_over), style = MaterialTheme.typography.headlineLarge)
        Text(result, style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(
                R.string.game_score,
                firstPlayer.name,
                firstPlayer.score,
                secondPlayer.name,
                secondPlayer.score,
            ),
        )
        if (startedAt != null && timeZone != null) {
            Text(stringResource(R.string.started, startedAt.toLocalDateTime(timeZone)))
            Text(stringResource(R.string.time_zone, timeZone.id))
        }
        if (startedAt != null && finishedAt != null) {
            Text(stringResource(R.string.elapsed, formatElapsedTime(startedAt, finishedAt)))
        }
        Spacer(Modifier.height(4.dp))
        Button(onClick = onRematch) {
            Text(stringResource(R.string.play_again))
        }
        TextButton(onClick = onHome) {
            Text(stringResource(R.string.home))
        }
    }
}

private fun formatElapsedTime(startedAt: Instant, finishedAt: Instant): String {
    val seconds = (finishedAt - startedAt).inWholeSeconds.coerceAtLeast(0)
    return "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
}
