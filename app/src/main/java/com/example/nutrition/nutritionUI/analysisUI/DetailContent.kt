package com.example.nutrition.nutritionUI.analysisUI

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrition.model.DiaryEntry

@Composable
fun TrendDetailContent(
    allEntries: List<DiaryEntry>,
    goalKcal: Int,
    timeSpan: Int,
    cardColor: Color,
    textColor: Color,
    grayText: Color,
    accentBlue: Color,
    dividerColor: Color,
    onTimeSpanChanged: (Int) -> Unit,
    onFoodClick: (AggregatedFood) -> Unit
) {
    val filteredEntries =
        remember(allEntries, timeSpan) { filterEntriesByDays(allEntries, timeSpan) }
    val aggregatedFood = remember(filteredEntries) { aggregateFood(filteredEntries, "Kalorien") }
    val averageKcal = if (timeSpan == 1) filteredEntries.sumOf { it.calories } else {
        val total = filteredEntries.sumOf { it.calories }; if (timeSpan > 0) total / timeSpan else 0
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { TimeSpanSelector(timeSpan, accentBlue, textColor, cardColor, onTimeSpanChanged) }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardColor)
                    .padding(20.dp)
            ) {
                Text(
                    "Durchschnitt: $averageKcal kcal / Tag",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(20.dp))
                val dailyData = prepareDailyKcal(filteredEntries, timeSpan)
                StaticBarChart(dailyData, goalKcal, accentBlue, grayText, timeSpan)
            }
        }
        item {
            Text(
                "Top Kalorien-Quellen",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = textColor,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        if (aggregatedFood.isEmpty()) item {
            Text(
                "Keine Daten in diesem Zeitraum.",
                color = grayText,
                modifier = Modifier.padding(16.dp)
            )
        }
        else items(aggregatedFood.take(5)) { food ->
            FoodRow(
                food.name,
                "${food.calories} kcal",
                "${food.totalGrams.toInt()}g gesamt",
                cardColor,
                textColor,
                grayText,
                accentBlue,
                onClick = { onFoodClick(food) })
        }
        item {
            DailyFoodHistoryAccordion(
                filteredEntries,
                cardColor,
                textColor,
                grayText,
                accentBlue,
                dividerColor
            )
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun MacroDetailContent(
    allEntries: List<DiaryEntry>,
    timeSpan: Int,
    cardColor: Color,
    textColor: Color,
    grayText: Color,
    accentBlue: Color,
    dividerColor: Color,
    onTimeSpanChanged: (Int) -> Unit,
    onFoodClick: (AggregatedFood) -> Unit
) {
    val filteredEntries =
        remember(allEntries, timeSpan) { filterEntriesByDays(allEntries, timeSpan) }
    var selectedMacro by rememberSaveable { mutableStateOf("Protein") }
    var usePieChart by rememberSaveable { mutableStateOf(true) }

    val totalP = filteredEntries.sumOf { it.protein }
    val totalC = filteredEntries.sumOf { it.carbs }
    val totalF = filteredEntries.sumOf { it.fat }
    val totalFiber = filteredEntries.sumOf { it.fiber }
    val totalSugar = filteredEntries.sumOf { it.sugar }
    val totalNutrients = totalP + totalC + totalF + totalFiber + totalSugar

    val aggregatedFood =
        remember(filteredEntries, selectedMacro) { aggregateFood(filteredEntries, selectedMacro) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { TimeSpanSelector(timeSpan, accentBlue, textColor, cardColor, onTimeSpanChanged) }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardColor)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Nährstoffe",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = textColor
                    )
                    IconButton(onClick = { usePieChart = !usePieChart }) {
                        Icon(
                            imageVector = if (usePieChart) Icons.Default.BarChart else Icons.Default.PieChart,
                            contentDescription = "Darstellung wechseln",
                            tint = accentBlue
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MacroSelectorButton(
                        "Protein",
                        Color(0xFF30D158),
                        selectedMacro == "Protein",
                        { selectedMacro = "Protein" },
                        Modifier.weight(1f)
                    )
                    MacroSelectorButton(
                        "Carbs",
                        Color(0xFFFF9F0A),
                        selectedMacro == "Carbs",
                        { selectedMacro = "Carbs" },
                        Modifier.weight(1f)
                    )
                    MacroSelectorButton(
                        "Fett",
                        Color(0xFF5E5CE6),
                        selectedMacro == "Fett",
                        { selectedMacro = "Fett" },
                        Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MacroSelectorButton(
                        "Ballaststoffe",
                        Color(0xFF64D2FF),
                        selectedMacro == "Ballaststoffe",
                        { selectedMacro = "Ballaststoffe" },
                        Modifier.weight(1f)
                    )
                    MacroSelectorButton(
                        "Zucker",
                        Color(0xFFFF2D55),
                        selectedMacro == "Zucker",
                        { selectedMacro = "Zucker" },
                        Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                if (totalNutrients > 0) {
                    if (usePieChart) CustomPieChart(
                        totalP,
                        totalC,
                        totalF,
                        totalFiber,
                        totalSugar,
                        selectedMacro,
                        textColor
                    )
                    else CustomFocusMacroBar(
                        totalP,
                        totalC,
                        totalF,
                        totalFiber,
                        totalSugar,
                        selectedMacro
                    )
                } else {
                    Text(
                        "Keine Daten vorhanden.",
                        color = grayText,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
        item {
            Text(
                "Top $selectedMacro-Quellen",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = textColor,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        if (aggregatedFood.isEmpty()) item {
            Text(
                "Keine Daten in diesem Zeitraum.",
                color = grayText,
                modifier = Modifier.padding(16.dp)
            )
        }
        else items(aggregatedFood.take(5)) { food ->
            val displayVal = when (selectedMacro) {
                "Protein" -> "${food.protein.toInt()}g"; "Carbs" -> "${food.carbs.toInt()}g"; "Fett" -> "${food.fat.toInt()}g"
                "Ballaststoffe" -> "${food.fiber.toInt()}g"; else -> "${food.sugar.toInt()}g"
            }
            val viewColor = when (selectedMacro) {
                "Protein" -> Color(0xFF30D158); "Carbs" -> Color(0xFFFF9F0A); "Fett" -> Color(
                    0xFF5E5CE6
                )

                "Ballaststoffe" -> Color(0xFF64D2FF); else -> Color(0xFFFF2D55)
            }
            FoodRow(
                food.name,
                displayVal,
                "${food.totalGrams.toInt()}g gesamt",
                cardColor,
                textColor,
                grayText,
                viewColor,
                onClick = { onFoodClick(food) })
        }
        item {
            DailyFoodHistoryAccordion(
                filteredEntries,
                cardColor,
                textColor,
                grayText,
                accentBlue,
                dividerColor
            )
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
fun CompareDetailContent(
    allEntries: List<DiaryEntry>,
    cardColor: Color,
    textColor: Color,
    grayText: Color,
    accentBlue: Color
) {
    var offset1 by rememberSaveable { mutableStateOf(0) }
    var offset2 by rememberSaveable { mutableStateOf(1) }
    val entries1 = remember(allEntries, offset1) { getEntriesForPastWeek(allEntries, offset1) }
    val entries2 = remember(allEntries, offset2) { getEntriesForPastWeek(allEntries, offset2) }
    val k1 = if (entries1.isEmpty()) 0 else entries1.sumOf { it.calories } / 7
    val k2 = if (entries2.isEmpty()) 0 else entries2.sumOf { it.calories } / 7
    val p1 = if (entries1.isEmpty()) 0 else (entries1.sumOf { it.protein } / 7).toInt()
    val p2 = if (entries2.isEmpty()) 0 else (entries2.sumOf { it.protein } / 7).toInt()
    val c1 = if (entries1.isEmpty()) 0 else (entries1.sumOf { it.carbs } / 7).toInt()
    val c2 = if (entries2.isEmpty()) 0 else (entries2.sumOf { it.carbs } / 7).toInt()
    val f1 = if (entries1.isEmpty()) 0 else (entries1.sumOf { it.fat } / 7).toInt()
    val f2 = if (entries2.isEmpty()) 0 else (entries2.sumOf { it.fat } / 7).toInt()
    val fib1 = if (entries1.isEmpty()) 0 else (entries1.sumOf { it.fiber } / 7).toInt()
    val fib2 = if (entries2.isEmpty()) 0 else (entries2.sumOf { it.fiber } / 7).toInt()
    val sug1 = if (entries1.isEmpty()) 0 else (entries1.sumOf { it.sugar } / 7).toInt()
    val sug2 = if (entries2.isEmpty()) 0 else (entries2.sumOf { it.sugar } / 7).toInt()

    val label1 = getWeekShortLabel(offset1)
    val label2 = getWeekShortLabel(offset2)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("Durchschnitt pro Tag vergleichen", color = grayText, fontSize = 14.sp) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WeekDropdownSelector(
                    offset1,
                    cardColor,
                    textColor,
                    accentBlue,
                    Modifier.weight(1f)
                ) { offset1 = it }
                Text("vs.", color = grayText, fontWeight = FontWeight.Bold)
                WeekDropdownSelector(
                    offset2,
                    cardColor,
                    textColor,
                    accentBlue,
                    Modifier.weight(1f)
                ) { offset2 = it }
            }
        }
        item {
            CompareRowCard(
                "Kalorien",
                "$k1 kcal",
                "$k2 kcal",
                (k1 - k2).toDouble(),
                "kcal",
                label1,
                label2,
                cardColor,
                textColor,
                grayText
            )
        }
        item {
            CompareRowCard(
                "Protein",
                "${p1}g",
                "${p2}g",
                (p1 - p2).toDouble(),
                "g",
                label1,
                label2,
                cardColor,
                textColor,
                grayText
            )
        }
        item {
            CompareRowCard(
                "Kohlenhydrate",
                "${c1}g",
                "${c2}g",
                (c1 - c2).toDouble(),
                "g",
                label1,
                label2,
                cardColor,
                textColor,
                grayText
            )
        }
        item {
            CompareRowCard(
                "Fett",
                "${f1}g",
                "${f2}g",
                (f1 - f2).toDouble(),
                "g",
                label1,
                label2,
                cardColor,
                textColor,
                grayText
            )
        }
        item {
            CompareRowCard(
                "Ballaststoffe",
                "${fib1}g",
                "${fib2}g",
                (fib1 - fib2).toDouble(),
                "g",
                label1,
                label2,
                cardColor,
                textColor,
                grayText
            )
        }
        item {
            CompareRowCard(
                "Zucker",
                "${sug1}g",
                "${sug2}g",
                (sug1 - sug2).toDouble(),
                "g",
                label1,
                label2,
                cardColor,
                textColor,
                grayText
            )
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}
