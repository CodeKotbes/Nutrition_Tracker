package com.example.nutrition.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_records")
data class WaterRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Int,
    val timestamp: Long,
    val date: String
)
