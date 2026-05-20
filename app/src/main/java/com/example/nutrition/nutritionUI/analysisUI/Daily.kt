package com.example.nutrition.nutritionUI.analysisUI

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrition.model.DiaryEntry
import com.example.nutrition.model.WaterRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.component1
import kotlin.collections.component2

@Composable
fun DailyFoodHistoryAccordion(
    entries: List<DiaryEntry>,
    cardColor: Color,
    textColor: Color,
    grayText: Color,
    accentBlue: Color,
    dividerColor: Color
) {
    val groupedEntries =
        remember(entries) { entries.groupBy { it.date }.toSortedMap(reverseOrder()) }
    var expandedDates by remember { mutableStateOf(setOf<String>()) }
    val formatIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formatOut = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Tagebuch-Verlauf",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = textColor,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        )

        if (groupedEntries.isEmpty()) {
            Text("Keine Historie vorhanden.", color = grayText)
        } else {
            groupedEntries.forEach { (dateStr, dayEntries) ->
                val isExpanded = expandedDates.contains(dateStr)
                val totalKcal = dayEntries.sumOf { it.calories }
                val displayDate = try {
                    val date = formatIn.parse(dateStr)
                    if (date != null) formatOut.format(date) else dateStr
                } catch (e: Exception) {
                    dateStr
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedDates =
                                    if (isExpanded) expandedDates - dateStr else expandedDates + dateStr
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                displayDate,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = textColor
                            )
                        }
                        Text(
                            "$totalKcal kcal",
                            fontWeight = FontWeight.Medium,
                            color = accentBlue,
                            fontSize = 14.sp
                        )
                    }
                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 8.dp)
                        ) {
                            dayEntries.forEachIndexed { index, entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            entry.foodName,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = textColor
                                        )
                                        Text(
                                            "${entry.amountInGrams.toInt()} g • P: ${entry.protein.toInt()}g | C: ${entry.carbs.toInt()}g | F: ${entry.fat.toInt()}g",
                                            color = grayText,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Text(
                                        "${entry.calories} kcal",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = textColor
                                    )
                                }
                                if (index < dayEntries.size - 1) HorizontalDivider(color = dividerColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyWaterHistoryAccordion(
    records: List<WaterRecord>,
    cardColor: Color,
    textColor: Color,
    grayText: Color,
    waterBlue: Color,
    dividerColor: Color
) {
    val groupedRecords =
        remember(records) { records.groupBy { it.date }.toSortedMap(reverseOrder()) }
    var expandedDates by remember { mutableStateOf(setOf<String>()) }
    val formatIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formatOut = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Wasser-Verlauf",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = textColor,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        )

        if (groupedRecords.isEmpty()) {
            Text("Keine Historie vorhanden.", color = grayText)
        } else {
            groupedRecords.forEach { (dateStr, dayRecords) ->
                val isExpanded = expandedDates.contains(dateStr)
                val totalMl = dayRecords.sumOf { it.amount }
                val displayDate = try {
                    val date = formatIn.parse(dateStr)
                    if (date != null) formatOut.format(date) else dateStr
                } catch (e: Exception) {
                    dateStr
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedDates =
                                    if (isExpanded) expandedDates - dateStr else expandedDates + dateStr
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = textColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                displayDate,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = textColor
                            )
                        }
                        Text(
                            "$totalMl ml",
                            fontWeight = FontWeight.Medium,
                            color = waterBlue,
                            fontSize = 14.sp
                        )
                    }
                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 8.dp)
                        ) {
                            dayRecords.sortedByDescending { it.timestamp }
                                .forEachIndexed { index, record ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            timeFormat.format(Date(record.timestamp)),
                                            color = grayText,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            "${record.amount} ml",
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = textColor
                                        )
                                    }
                                    if (index < dayRecords.size - 1) HorizontalDivider(color = dividerColor)
                                }
                        }
                    }
                }
            }
        }
    }
}