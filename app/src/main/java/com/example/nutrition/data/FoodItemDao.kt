package com.example.nutrition.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nutrition.model.FoodItem
import kotlinx.coroutines.flow.Flow
import kotlin.jvm.JvmSuppressWildcards

@Dao
interface FoodItemDao {

    @JvmSuppressWildcards
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodItem): Long

    @Query("SELECT * FROM food_items WHERE barcode = :barcode LIMIT 1")
    suspend fun getFoodByBarcode(barcode: String): FoodItem?

    @Query("SELECT * FROM food_items ORDER BY name ASC")
    fun getAllFoods(): Flow<List<FoodItem>>
}