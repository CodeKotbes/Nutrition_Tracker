package com.example.nutrition.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nutrition.model.WeightEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {

    @Query("SELECT * FROM weight_entries ORDER BY timestamp ASC")
    fun getAllWeights(): Flow<List<WeightEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(weight: WeightEntry)

    @Delete
    suspend fun deleteWeight(weight: WeightEntry)

}