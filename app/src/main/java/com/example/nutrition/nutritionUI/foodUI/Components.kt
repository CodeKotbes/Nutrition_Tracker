package com.example.nutrition.nutritionUI.foodUI

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutrition.model.DiaryEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun DashboardSummary(
    kcal: Int, goalKcal: Int, p: Double, goalP: Int, c: Double, goalC: Int,
    f: Double, goalF: Int, fib: Double, goalFib: Int, sug: Double, goalSug: Int,
    cardColor: Color, textColor: Color, grayText: Color, accentBlue: Color, dividerColor: Color,
    onEditGoalsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardColor)
            .clickable { onEditGoalsClick() }
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Tagesübersicht", color = grayText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(24.dp))
        SemiCircleCalorieChart(kcal, goalKcal, accentBlue, grayText, textColor)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Gegessen", color = grayText, fontSize = 13.sp)
                Text(
                    "$kcal kcal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (kcal > goalKcal) Color(0xFFFF453A) else accentBlue
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Tagesziel", color = grayText, fontSize = 13.sp)
                Text(
                    "$goalKcal kcal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp)); HorizontalDivider(color = dividerColor); Spacer(
        modifier = Modifier.height(16.dp)
    )
        NutrientProgressBar(
            "Protein",
            p,
            goalP,
            Color(0xFF30D158),
            textColor,
            grayText,
            dividerColor
        )
        NutrientProgressBar(
            "Kohlenhydrate",
            c,
            goalC,
            Color(0xFFFF9F0A),
            textColor,
            grayText,
            dividerColor
        )
        NutrientProgressBar("Fett", f, goalF, Color(0xFF5E5CE6), textColor, grayText, dividerColor)
        NutrientProgressBar(
            "Ballaststoffe",
            fib,
            goalFib,
            Color(0xFF64D2FF),
            textColor,
            grayText,
            dividerColor
        )
        NutrientProgressBar(
            "Zucker",
            sug,
            goalSug,
            Color(0xFFFF2D55),
            textColor,
            grayText,
            dividerColor
        )
    }
}

@Composable
fun SemiCircleCalorieChart(
    kcal: Int,
    goalKcal: Int,
    accentBlue: Color,
    grayText: Color,
    textColor: Color
) {
    val progress = if (goalKcal > 0) (kcal.toFloat() / goalKcal).coerceIn(0f, 1f) else 0f
    val isOver = kcal > goalKcal
    val remainingColor = if (isOver) Color(0xFFFF453A) else Color(0xFF30D158)
    val remainingValue = abs(goalKcal - kcal)

    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .aspectRatio(2f),
        contentAlignment = Alignment.BottomCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 26.dp.toPx()
            val arcRect = androidx.compose.ui.geometry.Rect(
                left = strokeWidth / 2,
                top = strokeWidth / 2,
                right = size.width - strokeWidth / 2,
                bottom = size.height * 2 - strokeWidth / 2
            )
            drawArc(
                color = if (isOver) Color.Gray.copy(alpha = 0.15f) else remainingColor.copy(
                    alpha = 0.25f
                ),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = arcRect.topLeft,
                size = arcRect.size
            )
            drawArc(
                color = if (isOver) Color(0xFFFF453A) else accentBlue,
                startAngle = 180f,
                sweepAngle = 180f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = arcRect.topLeft,
                size = arcRect.size
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = (-4).dp)
        ) {
            Text(if (isOver) "Überzogen" else "Verbleibend", color = grayText, fontSize = 14.sp)
            Text(
                text = "$remainingValue",
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp,
                color = remainingColor
            )
            Text("kcal", color = remainingColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun NutrientProgressBar(
    label: String,
    current: Double,
    goal: Int,
    barColor: Color,
    textColor: Color,
    grayText: Color,
    trackColor: Color
) {
    val progress = if (goal > 0) (current.toFloat() / goal).coerceIn(0f, 1f) else 0f
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = textColor)
            Text("${current.toInt()}g / ${goal}g", fontSize = 12.sp, color = grayText)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = trackColor
        )
    }
}

@Composable
fun MealSection(
    title: String,
    entries: List<DiaryEntry>,
    cardColor: Color,
    textColor: Color,
    grayText: Color,
    accentBlue: Color,
    dividerColor: Color,
    onAddClick: () -> Unit,
    onDeleteClick: (DiaryEntry) -> Unit,
    onEntryClick: (DiaryEntry) -> Unit
) {
    val mealKcal = entries.sumOf { it.calories }
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Auf/Zuklappen",
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$mealKcal kcal",
                    color = grayText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(onClick = onAddClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.AddCircle,
                        "Hinzufügen",
                        tint = accentBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        AnimatedVisibility(visible = isExpanded) {
            if (entries.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(top = 0.dp)
                ) {
                    entries.forEachIndexed { index, entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEntryClick(entry) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    entry.foodName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = textColor
                                )
                                Text(
                                    "${entry.amountInGrams.toInt()} g • P: ${entry.protein.toInt()}g | C: ${entry.carbs.toInt()}g | F: ${entry.fat.toInt()}g",
                                    color = grayText,
                                    fontSize = 12.sp
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${entry.calories} kcal",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = textColor
                                )
                                IconButton(
                                    onClick = { onDeleteClick(entry) },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(start = 8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        "Löschen",
                                        tint = grayText.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                        if (index < entries.size - 1) HorizontalDivider(color = dividerColor)
                    }
                }
            } else {
                Text(
                    "Noch keine Einträge",
                    color = grayText,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 48.dp, bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
fun HorizontalWeekCalendar(
    currentDateStr: String,
    allEntries: List<DiaryEntry>,
    accentBlue: Color,
    textColor: Color,
    grayText: Color,
    cardColor: Color,
    onDateSelected: (Long) -> Unit
) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val dayFormat = remember { SimpleDateFormat("EEE", Locale.GERMAN) }
    val numFormat = remember { SimpleDateFormat("dd", Locale.getDefault()) }
    val weekDays = remember(currentDateStr, allEntries) {
        val date = try {
            sdf.parse(currentDateStr)
        } catch (e: Exception) {
            Date()
        }
        val cal = Calendar.getInstance().apply { time = date ?: Date() }

        var dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek == Calendar.SUNDAY) dayOfWeek = 8
        cal.add(Calendar.DAY_OF_YEAR, -(dayOfWeek - Calendar.MONDAY))

        (0..6).map {
            val dStr = sdf.format(cal.time)
            val millis = cal.timeInMillis
            val dNum = numFormat.format(cal.time)
            val dName = dayFormat.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 1)
            Triple(dStr, dName to dNum, millis)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        weekDays.forEach { (dateStr, labels, millis) ->
            val (dayName, dayNum) = labels
            val isSelected = dateStr == currentDateStr
            val hasData = allEntries.any { it.date == dateStr }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) accentBlue else Color.Transparent)
                    .clickable { onDateSelected(millis) }
                    .padding(vertical = 8.dp, horizontal = 10.dp)
            ) {
                Text(
                    dayName,
                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else grayText,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    dayNum,
                    color = if (isSelected) Color.White else textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (hasData) (if (isSelected) Color.White else accentBlue) else Color.Transparent)
                )
            }
        }
    }
}