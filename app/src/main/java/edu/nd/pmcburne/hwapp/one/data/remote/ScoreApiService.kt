package edu.nd.pmcburne.hwapp.one.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface ScoreApiService {
    @GET("scoreboard/basketball-{gender}/d1/{year}/{month}/{day}")
    suspend fun getScores(
        @Path("gender") gender: String,
        @Path("year") year: Int,
        @Path("month") month: String,
        @Path("day") day: String
    ): ScoreboardResponse

}