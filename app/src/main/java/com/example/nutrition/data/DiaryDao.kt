package com.example.nutrition.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nutrition.model.DiaryEntry
import kotlinx.coroutines.flow.Flow
import kotlin.jvm.JvmSuppressWildcards

@Dao
interface DiaryDao {

    @JvmSuppressWildcards
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaryEntry(entry: DiaryEntry): Long

    @JvmSuppressWildcards
    @Delete
    suspend fun deleteDiaryEntry(entry: DiaryEntry): Int

    @Query("SELECT * FROM diary_entries WHERE date = :date")
    fun getEntriesByDate(date: String): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries WHERE date BETWEEN :startDate AND :endDate")
    fun getEntriesForDateRange(startDate: String, endDate: String): Flow<List<DiaryEntry>>
}