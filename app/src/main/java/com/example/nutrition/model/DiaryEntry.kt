package com.example.nutrition.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val foodId: Int,
    val foodName: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val amountInGrams: Double,
    val mealType: String,
    val date: String
)