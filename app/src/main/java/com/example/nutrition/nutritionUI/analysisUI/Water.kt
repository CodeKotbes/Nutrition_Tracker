package com.example.nutrition.nutritionUI.analysisUI

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.nutrition.model.WaterRecord
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun WaterDetailContent(
    waterRecords: List<WaterRecord>,
    waterGoal: Int,
    timeSpan: Int,
    customStartMillis: Long?,
    customEndMillis: Long?,
    cardColor: Color,
    textColor: Color,
    grayText: Color,
    waterBlue: Color,
    dividerColor: Color,
    onTimeSpanChanged: (Int) -> Unit,
    onCustomRangeSelected: (Long, Long) -> Unit,
    onJumpToDate: (String) -> Unit
) {
    val filteredRecords = remember(waterRecords, timeSpan, customStartMillis, customEndMillis) {
        getFilteredWater(waterRecords, timeSpan, customStartMillis, customEndMillis)
    }
    val dailyData = prepareDailyWater(filteredRecords, timeSpan, customStartMillis, customEndMillis)
    val daysCount = dailyData.size
    val averageWater = if (daysCount > 0) filteredRecords.sumOf { it.amount } / daysCount else 0
    var showInfoDialog by remember { mutableStateOf(false) }
    var selectedDayInfo by remember(
        timeSpan,
        customStartMillis,
        customEndMillis
    ) { mutableStateOf<Pair<String, Int>?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TimeSpanSelector(
                selectedSpan = timeSpan,
                accentBlue = waterBlue,
                textColor = textColor,
                cardColor = cardColor,
                onSelectedSpan = onTimeSpanChanged,
                onCustomRangeSelected = onCustomRangeSelected,
                onInfoClick = { showInfoDialog = true }
            )
        }
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
                Spacer(modifier = Modifier.height(12.dp))

                AnimatedVisibility(visible = selectedDayInfo != null) {
                    selectedDayInfo?.let { (dateStr, amount) ->
                        val date =
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                        val displayDate = date?.let {
                            SimpleDateFormat(
                                "EEEE, dd. MMM yyyy",
                                Locale.GERMAN
                            ).format(it)
                        } ?: dateStr

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(waterBlue.copy(alpha = 0.1f))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                displayDate,
                                color = textColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                "$amount ml",
                                color = waterBlue,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                StaticBarChart(
                    data = dailyData,
                    goal = waterGoal,
                    barColor = waterBlue,
                    labelColor = grayText,
                    timeSpan = daysCount,
                    isWater = true,
                    selectedDate = selectedDayInfo?.first,
                    onBarClick = { date, amount ->
                        if (selectedDayInfo?.first == date) selectedDayInfo =
                            null else selectedDayInfo = Pair(date, amount)
                    }
                )
            }
        }
        item {
            DailyWaterHistoryAccordion(
                filteredRecords,
                cardColor,
                textColor,
                grayText,
                waterBlue,
                dividerColor,
                onDayClick = onJumpToDate
            )
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false }, containerColor = cardColor,
            title = {
                Text(
                    "Auswahl erklärt",
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "• Standard-Zeiträume (Tag, 1W, 14T, 1M): Die App berechnet den Trend rückwirkend ab heute.\n\n" +
                            "• Kalender: Hier kann ein freier Zeitraum gewählt werden.\n\n" +
                            "Tipp: Tippe auf das Buch-Symbol im Wasser-Verlauf, um direkt zum Tagebuch dieses Tages zu springen.",
                    color = grayText, fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text(
                        "Verstanden",
                        color = waterBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}