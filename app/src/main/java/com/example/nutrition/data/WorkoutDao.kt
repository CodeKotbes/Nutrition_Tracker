package com.example.nutrition.data

import androidx.room.*
import com.example.nutrition.model.WorkoutEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_entries WHERE date = :date ORDER BY timestamp DESC")
    fun getWorkoutsByDate(date: String): Flow<List<WorkoutEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: WorkoutEntry)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntry)
}