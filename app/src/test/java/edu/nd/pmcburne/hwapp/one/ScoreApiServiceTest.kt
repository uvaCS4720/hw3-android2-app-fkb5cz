package edu.nd.pmcburne.hwapp.one

import edu.nd.pmcburne.hwapp.one.data.remote.ScoreApiService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ScoreApiServiceTest {
    private val api = Retrofit.Builder()
        .baseUrl("https://ncaa-api.henrygd.me/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ScoreApiService::class.java)

    @Test
    fun returnsMensDate_live() = runBlocking {
        val response = api.getScores(
            gender = "men",
            year = 2026,
            month = "02",
            day = "17",
        )

        assertNotNull(response)
        assertNotNull(response.games)
        assertTrue(response.games is List<*>)
    }

    @Test
    fun returnsWomensDate_live() = runBlocking {
        val response = api.getScores(
            gender = "women",
            year = 2026,
            month = "01",
            day = "30"
        )
        assertNotNull(response)
        assertNotNull(response.games)
        assertTrue(response.games is List<*>)
    }
}

