package com.example.nutrition.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nutrition.model.DiaryEntry
import com.example.nutrition.model.FoodItem
import com.example.nutrition.model.Recipe
import com.example.nutrition.model.RecipeIngredient

@Database(
    entities = [FoodItem::class, DiaryEntry::class, Recipe::class, RecipeIngredient::class],
    version = 3,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun foodItemDao(): FoodItemDao
    abstract fun diaryDao(): DiaryDao
    abstract fun recipeDao(): RecipeDao
}