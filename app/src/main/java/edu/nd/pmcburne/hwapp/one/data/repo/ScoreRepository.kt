package edu.nd.pmcburne.hwapp.one.data.repo

import edu.nd.pmcburne.hwapp.one.data.local.ScoreDao
import edu.nd.pmcburne.hwapp.one.data.local.ScoreEntity
import edu.nd.pmcburne.hwapp.one.data.model.Gender
import edu.nd.pmcburne.hwapp.one.data.remote.ScoreApiService
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class ScoreRepository (private val dao:ScoreDao, private val api:ScoreApiService) {
    fun observeScores(date: LocalDate, gender: Gender): Flow<List<ScoreEntity>> {
        return dao.observeScores(scoreDate = date.toString(), gender=gender.apiValue)
    }

    suspend fun refreshScores(date: LocalDate, gender: Gender): Result<Unit> {
        return runCatching {
            val response = api.getScores(
                gender = gender.apiValue,
                year = date.year,
                month = "%02d".format(date.monthValue),
                day = "%02d".format(date.dayOfMonth)
            )

            val entities = response.games.map { wrapper ->
                val game = wrapper.game
                ScoreEntity(
                    gameId = game.gameID,
                    gender = gender.apiValue,
                    scoreDate = date.toString(),
                    homeName = game.home.names.short.ifBlank {"Home Team"},
                    awayName = game.away.names.short.ifBlank {"Away Team"},
                    homeScore = game.home.score,
                    awayScore = game.away.score,
                    homeWinner = game.home.winner,
                    awayWinner = game.away.winner,
                    gameState = game.gameState,
                    startTime = game.startTime,
                    startTimeEpoch = game.startTimeEpoch.toLongOrNull() ?: 0L,
                    currentPeriod = game.currentPeriod,
                    contestClock = game.contestClock,
                    finalMessage = game.finalMessage
                )
            }
            dao.upsertAll(entities)
        }
    }
}