package com.example.nutrition.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nutrition.model.DiaryEntry
import com.example.nutrition.model.FoodItem
import com.example.nutrition.model.Recipe
import com.example.nutrition.model.RecipeIngredient
import com.example.nutrition.model.WaterRecord

@Database(
    entities = [FoodItem::class, DiaryEntry::class, Recipe::class,
        RecipeIngredient::class, WaterRecord::class],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodItemDao(): FoodItemDao
    abstract fun diaryDao(): DiaryDao
    abstract fun recipeDao(): RecipeDao
    abstract fun waterDao(): WaterDao
}