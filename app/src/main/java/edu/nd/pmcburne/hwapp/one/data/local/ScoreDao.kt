package edu.nd.pmcburne.hwapp.one.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Query("""
        SELECT * FROM scores
        WHERE gender = :gender AND scoreDate = :scoreDate
        ORDER BY startTimeEpoch ASC, gameID ASC
    """)

    fun observeScores(scoreDate: String, gender:String):Flow<List<ScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(scores: List<ScoreEntity>)
}