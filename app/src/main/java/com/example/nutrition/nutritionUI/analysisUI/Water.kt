package com.example.nutrition.nutritionUI.analysisUI

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrition.model.WaterRecord

@Composable
fun WaterDetailContent(
    waterRecords: List<WaterRecord>,
    waterGoal: Int,
    timeSpan: Int,
    cardColor: Color,
    textColor: Color,
    grayText: Color,
    waterBlue: Color,
    dividerColor: Color,
    onTimeSpanChanged: (Int) -> Unit
) {
    val filteredRecords =
        remember(waterRecords, timeSpan) { filterWaterRecordsByDays(waterRecords, timeSpan) }
    val dailyData = prepareDailyWater(waterRecords, timeSpan)
    val averageWater = if (timeSpan > 0) dailyData.sumOf { it.second } / timeSpan else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { TimeSpanSelector(timeSpan, waterBlue, textColor, cardColor, onTimeSpanChanged) }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardColor)
                    .padding(20.dp)
            ) {
                Text(
                    "Durchschnitt: $averageWater ml / Tag",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(20.dp))
                StaticBarChart(dailyData, waterGoal, waterBlue, grayText, timeSpan, isWater = true)
            }
        }
        item {
            DailyWaterHistoryAccordion(
                filteredRecords,
                cardColor,
                textColor,
                grayText,
                waterBlue,
                dividerColor
            )
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}