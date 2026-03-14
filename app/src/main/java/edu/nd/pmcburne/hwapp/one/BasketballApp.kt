package edu.nd.pmcburne.hwapp.one

import android.app.Application
import androidx.room.Room

import edu.nd.pmcburne.hwapp.one.data.local.AppDatabase
import edu.nd.pmcburne.hwapp.one.data.remote.ScoreApiService
import edu.nd.pmcburne.hwapp.one.data.repo.ScoreRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BasketballApp : Application() {

    // initialize room db with lazy
     val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "basketball_scores.db"
        ).build()
    }

    // connect to retrofit api service
    private val api: ScoreApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://ncaa-api.henrygd.me/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ScoreApiService::class.java)
    }

    // repository to get scores from api and store updated in room db
    val repository by lazy {
        ScoreRepository(
            dao = database.scoreDao(),
            api = api
        )
    }
}