package com.example.nutrition.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_entries")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val weight: Double,
    val date: String,
    val timestamp: Long
)