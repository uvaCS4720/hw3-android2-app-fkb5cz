package edu.nd.pmcburne.hwapp.one

import edu.nd.pmcburne.hwapp.one.data.local.ScoreDao
import edu.nd.pmcburne.hwapp.one.data.local.ScoreEntity
import edu.nd.pmcburne.hwapp.one.data.model.Gender
import edu.nd.pmcburne.hwapp.one.data.remote.GameDto
import edu.nd.pmcburne.hwapp.one.data.remote.GameWrapper
import edu.nd.pmcburne.hwapp.one.data.remote.ScoreApiService
import edu.nd.pmcburne.hwapp.one.data.remote.ScoreboardResponse
import edu.nd.pmcburne.hwapp.one.data.remote.TeamDto
import edu.nd.pmcburne.hwapp.one.data.remote.TeamNamesDto
import edu.nd.pmcburne.hwapp.one.data.repo.ScoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate


class ScoreRepositoryTest {

    @Test
    fun refreshScores() = runBlocking {
        val fakeDao = FakeScoreDao()
        val fakeApi = FakeScoreApiService()

        val repository = ScoreRepository(
            dao = fakeDao,
            api = fakeApi
        )

        val date = LocalDate.of(2026, 2, 17)
        val result = repository.refreshScores(date, Gender.MEN)

        assertTrue(result.isSuccess)
        val saved = fakeDao.observeScores(date.toString(), "men").first()

        assertEquals(1, saved.size)

        val game = saved.first()
        assertEquals("game123", game.gameId)
        assertEquals("Virginia", game.homeName)
        assertEquals("Duke", game.awayName)
        assertEquals("72", game.homeScore)
        assertEquals("68", game.awayScore)
        assertEquals(true, game.homeWinner)
        assertEquals(false, game.awayWinner)
        assertEquals("final", game.gameState)
        assertEquals("Final", game.finalMessage)
    }

    @Test
    fun refreshScores_Offline() = runBlocking {
        val fakeDao = FakeScoreDao()
        fakeDao.upsertAll(
            listOf(
                ScoreEntity(
                    gameId = "saved1",
                    gender = "men",
                    scoreDate = "2026-02-17",
                    homeName = "Virginia",
                    awayName = "Duke",
                    homeScore = "45",
                    awayScore = "44",
                    homeWinner = false,
                    awayWinner = false,
                    gameState = "live",
                    startTime = "7:00 PM",
                    startTimeEpoch = 0L,
                    currentPeriod = "2nd Half",
                    contestClock = "13:47",
                    finalMessage = ""
                )
            )
        )

        val repository = ScoreRepository(
            dao = fakeDao,
            api = object : ScoreApiService {
                override suspend fun getScores(
                    gender: String,
                    year: Int,
                    month: String,
                    day: String
                ): ScoreboardResponse {
                    error("No internet")
                }
            }
        )

        val result = repository.refreshScores(LocalDate.of(2026, 2, 17), Gender.MEN)
        assertTrue(result.isFailure)

        val cached = fakeDao.observeScores("2026-02-17", "men").first()
        assertEquals(1, cached.size)
        assertEquals("Virginia", cached.first().homeName)
    }
}

private class FakeScoreApiService: ScoreApiService {
    override suspend fun getScores(
        gender: String,
        year: Int,
        month: String,
        day: String
    ) : ScoreboardResponse {
        return ScoreboardResponse(
            games = listOf(
                GameWrapper(
                    game = GameDto(
                        gameID = "game123",
                        away = TeamDto(
                            score = "68",
                            winner = false,
                            names = TeamNamesDto(short = "Duke")
                        ),
                        home = TeamDto(
                            score = "72",
                            winner = true,
                            names = TeamNamesDto(short = "Virginia")
                        ),
                        gameState = "final",
                        startTime = "7:00 PM",
                        startTimeEpoch = "1739833200",
                        currentPeriod = "2nd Half",
                        contestClock = "0:00",
                        finalMessage = "Final"
                    )
                )
            )
        )
    }
}

private class FakeScoreDao: ScoreDao {
    private val scoresFlow = MutableStateFlow<List<ScoreEntity>>(emptyList())

    override fun observeScores(scoreDate: String, gender: String): Flow<List<ScoreEntity>> {
        return MutableStateFlow(
            scoresFlow.value.filter {
                it.scoreDate == scoreDate && it.gender == gender
            }
        )
    }

    override suspend fun upsertAll(scores: List<ScoreEntity>) {
        val remaining = scoresFlow.value.filterNot { existing ->
            scores.any {
                it.gameId == existing.gameId &&
                it.gender == existing.gender &&
                it.scoreDate == existing.scoreDate
            }
        }

        scoresFlow.value = remaining + scores
    }
}