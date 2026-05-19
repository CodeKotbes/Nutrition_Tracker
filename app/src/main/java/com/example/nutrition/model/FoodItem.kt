package com.example.nutrition.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val brand: String? = null,
    val barcode: String? = null,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val isCustom: Boolean = false
)