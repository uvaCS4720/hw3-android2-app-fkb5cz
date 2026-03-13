package edu.nd.pmcburne.hwapp.one.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.nd.pmcburne.hwapp.one.data.local.ScoreEntity
import edu.nd.pmcburne.hwapp.one.data.model.Gender
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BasketballScoresScreen (viewModel: BasketballViewModel){
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it)}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("Basketball Scores")},
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon (Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        snackbarHost = {SnackbarHost(hostState = snackbarHostState)}
    ) {padding->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScoresContent(
                uiState = uiState,
                onGenderSelected = viewModel::setGender,
                onDateSelected = viewModel::setDate,
                onRefresh = viewModel::refresh
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(56.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScoresContent(
    uiState: BasketballUiState,
    onGenderSelected: (Gender) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onRefresh: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false)}
    var selectedMillis by remember {
        mutableLongStateOf(
            uiState.selectedDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Choose date and division",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.size(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.selectedGender == Gender.MEN,
                onClick = {onGenderSelected(Gender.MEN)},
                label = {Text("Men")}
            )
            FilterChip(
                selected = uiState.selectedGender == Gender.WOMEN,
                onClick = {onGenderSelected(Gender.WOMEN)},
                label = {Text("Women")}
            )
            Spacer(modifier=Modifier.weight(1f))
            AssistChip(
                onClick = {showDatePicker=true},
                label = {
                    Text(uiState.selectedDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")))
                },
                leadingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Pick date")
                }
            )
        }

        Spacer(modifier = Modifier.size(10.dp))

        if (uiState.showingOfflineData) {
            Text(
                text = "Showing saved offline results",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.size(8.dp))
        }

        if (uiState.games.isEmpty() && !uiState.isLoading) {
            Text(
                text = "No games on this date.",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyColumn (
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.games, key = {"${it.gameId}-$it.gender-${it.scoreDate}"}) { game ->
                    GameCard(game = game)

                }
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialDisplayedMonthMillis = selectedMillis
        )

        DatePickerDialog(
            onDismissRequest = {showDatePicker = false},
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = pickerState.selectedDateMillis
                        if (millis != null) {
                            selectedMillis = millis
                            val localDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            onDateSelected(localDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Ok")
                }
            },
            dismissButton = {
                TextButton(onClick = {showDatePicker=false}) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun GameCard(game: ScoreEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Away",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "Home",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(modifier = Modifier.size(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TeamColumn(
                    name = game.awayName,
                    score = game.awayScore,
                    winner = game.awayWinner
                )
                TeamColumn(
                    name = game.homeName,
                    score = game.homeScore,
                    winner = game.homeWinner,
                    alignEnd = true
                )
            }
            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = when (game.gameState.lowercase()) {
                    "pre" -> "Upcoming - ${game.startTime}"
                    "live" -> "Live - ${
                        formatPeriodAndClock(
                            game.currentPeriod,
                            game.contestClock
                        )
                    }"

                    "final" -> if (game.finalMessage.isNotBlank()) {
                        "Final - ${game.finalMessage}"
                    } else {
                        "Final"
                    }

                    else -> game.startTime.ifBlank { "Status Unavailable" }
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (game.gameState.lowercase() == "final") {
                Spacer(modifier = Modifier.size(6.dp))
                val winnerName = when {
                    game.homeWinner -> game.homeName
                    game.awayWinner -> game.awayName
                    else -> null
                }
                if (winnerName != null) {
                    Text(
                        text = "Winner: $winnerName",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
@Composable
private fun TeamColumn(
    name: String,
    score: String,
    winner: Boolean,
    alignEnd: Boolean = false
) {
    Column(horizontalAlignment =if (alignEnd) Alignment.End else Alignment.Start) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (winner) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = if (score.isBlank()) "-" else score,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = if (winner) FontWeight.Bold else FontWeight.Medium
        )
    }
}

private fun formatPeriodAndClock(period: String, clock: String): String {
    val cleanedPeriod = if (period.isBlank()) "In Progress" else period
    val cleanedClock = if (clock.isBlank() || clock == "0:00") "" else "- $clock"
    return cleanedPeriod + cleanedClock
}


@Preview(showBackground = true)
@Composable
fun BasketballScoresPreview() {
    val fakeGames = listOf(
        ScoreEntity(
            gameId = "1",
            gender = "men",
            scoreDate = "2026-02-17",
            homeName = "Virginia",
            awayName = "Duke",
            homeScore = "72",
            awayScore = "68",
            homeWinner = true,
            awayWinner = false,
            gameState = "final",
            startTime = "7:00 PM",
            startTimeEpoch = 0,
            currentPeriod = "2nd Half",
            contestClock = "0:00",
            finalMessage = "Final"
        ),
        ScoreEntity(
            gameId = "2",
            gender = "men",
            scoreDate = "2026-02-17",
            homeName = "UNC",
            awayName = "NC State",
            homeScore = "44",
            awayScore = "41",
            homeWinner = false,
            awayWinner = false,
            gameState = "live",
            startTime = "6:30 PM",
            startTimeEpoch = 0,
            currentPeriod = "2nd Half",
            contestClock = "13:47",
            finalMessage = ""
        )
    )

    val previewState = BasketballUiState(
        selectedDate = LocalDate.now(),
        selectedGender = Gender.MEN,
        games = fakeGames,
        isLoading = false
    )

    ScoresContent(
        uiState = previewState,
        onGenderSelected = {},
        onDateSelected = {},
        onRefresh = {}
    )
}
