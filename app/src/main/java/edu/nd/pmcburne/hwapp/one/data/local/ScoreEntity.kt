package edu.nd.pmcburne.hwapp.one.data.local
import androidx.room.Entity

@Entity(
    tableName = "scores",
    primaryKeys = ["gameId", "gender", "scoreDate"]
)

data class ScoreEntity (
    val gameId: String,
    val gender: String,
    val scoreDate: String,

    val homeName: String,
    val awayName: String,

    val homeScore: String,
    val awayScore: String,

    val homeWinner: Boolean,
    val awayWinner: Boolean,

    val gameState: String,
    val startTime: String,
    val startTimeEpoch: Long,
    val currentPeriod: String,
    val contestClock: String,
    val finalMessage: String
)