package com.example.nutrition.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.nutrition.model.WaterRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_records WHERE date = :date ORDER BY timestamp DESC")
    fun getWaterRecordsByDate(date: String): Flow<List<WaterRecord>>

    @Query("SELECT * FROM water_records ORDER BY timestamp DESC")
    fun getAllWaterRecords(): Flow<List<WaterRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterRecord(record: WaterRecord)

    @Update
    suspend fun updateWaterRecord(record: WaterRecord)

    @Query("DELETE FROM water_records WHERE id = :id")
    suspend fun deleteWaterRecordById(id: Int)
}