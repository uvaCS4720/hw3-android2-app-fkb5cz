package edu.nd.pmcburne.hwapp.one.data.remote

data class ScoreboardResponse(
    val games: List<GameWrapper> = emptyList()
)

data class GameWrapper(
    val game: GameDto
)
data class GameDto(
    val gameID: String = "",
    val away: TeamDto = TeamDto(),
    val home: TeamDto = TeamDto(),
    val gameState: String = "",
    val startTime: String = "",
    val startTimeEpoch: String = "0",
    val currentPeriod: String = "",
    val contestClock: String = "",
    val finalMessage: String = ""
)

data class TeamDto(
    val score: String = "",
    val winner: Boolean = false,
    val names: TeamNamesDto = TeamNamesDto()
)

data class TeamNamesDto(
    val short: String = ""
)