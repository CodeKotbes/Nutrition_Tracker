package com.example.nutrition.nutritionUI.analysisUI

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekCalendarSelector(
    selectedDateMillis: Long,
    cardColor: Color,
    textColor: Color,
    accentBlue: Color,
    modifier: Modifier = Modifier,
    onSelect: (Long) -> Unit
) {
    var showCalendar by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(cardColor)
                .clickable { showCalendar = true }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                getWeekLabel(selectedDateMillis),
                color = accentBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                maxLines = 1
            )
            Icon(Icons.Default.DateRange, contentDescription = "Woche wählen", tint = accentBlue)
        }

        if (showCalendar) {
            val datePickerColors = DatePickerDefaults.colors(
                containerColor = cardColor,
                titleContentColor = Color.Gray,
                headlineContentColor = textColor,
                weekdayContentColor = Color.Gray,
                subheadContentColor = textColor,
                yearContentColor = textColor,
                currentYearContentColor = accentBlue,
                selectedYearContentColor = Color.White,
                selectedYearContainerColor = accentBlue,
                dayContentColor = textColor,
                disabledDayContentColor = Color.Gray.copy(alpha = 0.5f),
                selectedDayContentColor = Color.White,
                disabledSelectedDayContentColor = Color.Gray.copy(alpha = 0.5f),
                selectedDayContainerColor = accentBlue,
                disabledSelectedDayContainerColor = Color.Gray.copy(alpha = 0.5f),
                todayContentColor = accentBlue,
                todayDateBorderColor = accentBlue
            )

            DatePickerDialog(
                onDismissRequest = { showCalendar = false },
                colors = DatePickerDefaults.colors(containerColor = cardColor),
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { onSelect(it) }
                        showCalendar = false
                    }) { Text("OK", color = accentBlue, fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showCalendar = false }) {
                        Text("Abbrechen", color = textColor)
                    }
                }
            ) {
                DatePicker(state = datePickerState, colors = datePickerColors)
            }
        }
    }
}

@Composable
fun StaticBarChart(
    data: List<Triple<String, String, Int>>,
    goal: Int,
    barColor: Color,
    labelColor: Color,
    timeSpan: Int,
    isWater: Boolean = false,
    selectedDate: String? = null,
    onBarClick: (String, Int) -> Unit
) {
    val maxChartValue = (data.maxOfOrNull { it.third } ?: 1000)
        .coerceAtLeast(goal + if (isWater) 500 else 500)
        .toFloat()
    val showLabels = timeSpan in 1..7

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val goalY =
                    size.height - (size.height * (goal / maxChartValue)).coerceIn(0f, size.height)
                drawLine(
                    color = if (isWater) barColor.copy(alpha = 0.6f) else Color.Red.copy(alpha = 0.6f),
                    start = Offset(0f, goalY),
                    end = Offset(size.width, goalY),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f))
                )
            }
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { triple ->
                    val fullDate = triple.first
                    val value = triple.third

                    val fillHeightPercentage = (value / maxChartValue).coerceIn(0.02f, 1.0f)
                    val alpha = if (selectedDate == null || selectedDate == fullDate) 1.0f else 0.4f
                    val finalBarColor =
                        if (!isWater && value > goal + 150) Color(0xFFFF453A).copy(alpha = alpha) else barColor.copy(
                            alpha = alpha
                        )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { onBarClick(fullDate, value) }
                            .padding(horizontal = 2.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (timeSpan > 7) 0.8f else 0.6f)
                                .fillMaxHeight(fillHeightPercentage)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(finalBarColor)
                        )
                    }
                }
            }
        }

        if (showLabels) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                data.forEach { triple ->
                    Text(
                        text = triple.second,
                        fontSize = 10.sp,
                        color = labelColor,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun CustomPieChart(
    p: Double, c: Double, f: Double, fib: Double, sug: Double,
    selectedMacro: String, textColor: Color,
    onMacroSelected: (String) -> Unit
) {
    val total = p + c + f + fib + sug
    val pAngle = if (total > 0) (p / total * 360).toFloat() else 0f
    val cAngle = if (total > 0) (c / total * 360).toFloat() else 0f
    val fAngle = if (total > 0) (f / total * 360).toFloat() else 0f
    val fibAngle = if (total > 0) (fib / total * 360).toFloat() else 0f
    val sugAngle = if (total > 0) (sug / total * 360).toFloat() else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp), contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(160.dp)
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val dx = tapOffset.x - center.x
                        val dy = tapOffset.y - center.y

                        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                        angle = (angle + 360) % 360
                        val adjustedAngle = (angle + 90) % 360

                        var currentAngle = 0f
                        if (adjustedAngle in currentAngle..(currentAngle + pAngle)) onMacroSelected(
                            "Protein"
                        )
                        currentAngle += pAngle
                        if (adjustedAngle in currentAngle..(currentAngle + cAngle)) onMacroSelected(
                            "Carbs"
                        )
                        currentAngle += cAngle
                        if (adjustedAngle in currentAngle..(currentAngle + fAngle)) onMacroSelected(
                            "Fett"
                        )
                        currentAngle += fAngle
                        if (adjustedAngle in currentAngle..(currentAngle + fibAngle)) onMacroSelected(
                            "Ballaststoffe"
                        )
                        currentAngle += fibAngle
                        if (adjustedAngle in currentAngle..(currentAngle + sugAngle)) onMacroSelected(
                            "Zucker"
                        )
                    }
                }
        ) {
            val strokeWidth = 32.dp.toPx()
            var startAngle = -90f

            fun drawMacroArc(color: Color, sweep: Float, isSelected: Boolean) {
                if (sweep > 0) {
                    drawArc(
                        color = color.copy(alpha = if (isSelected) 1f else 0.3f),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(
                            width = if (isSelected) strokeWidth * 1.15f else strokeWidth,
                            cap = StrokeCap.Butt
                        )
                    )
                }
                startAngle += sweep
            }

            drawMacroArc(Color(0xFF30D158), pAngle, selectedMacro == "Protein")
            drawMacroArc(Color(0xFFFF9F0A), cAngle, selectedMacro == "Carbs")
            drawMacroArc(Color(0xFF5E5CE6), fAngle, selectedMacro == "Fett")
            drawMacroArc(Color(0xFF64D2FF), fibAngle, selectedMacro == "Ballaststoffe")
            drawMacroArc(Color(0xFFFF2D55), sugAngle, selectedMacro == "Zucker")
        }

        val percent = if (total > 0) {
            when (selectedMacro) {
                "Protein" -> (p / total * 100); "Carbs" -> (c / total * 100); "Fett" -> (f / total * 100); "Ballaststoffe" -> (fib / total * 100); else -> (sug / total * 100)
            }
        } else 0.0

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${percent.toInt()}%",
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                color = textColor
            )
            Text(
                selectedMacro,
                fontSize = 13.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun CustomFocusMacroBar(
    p: Double, c: Double, f: Double, fib: Double, sug: Double,
    selectedMacro: String,
    onMacroSelected: (String) -> Unit
) {
    val total = p + c + f + fib + sug
    val pPerc = if (total > 0) (p / total).toFloat().coerceAtLeast(0.01f) else 0.01f
    val cPerc = if (total > 0) (c / total).toFloat().coerceAtLeast(0.01f) else 0.01f
    val fPerc = if (total > 0) (f / total).toFloat().coerceAtLeast(0.01f) else 0.01f
    val fibPerc = if (total > 0) (fib / total).toFloat().coerceAtLeast(0.01f) else 0.01f
    val sugPerc = if (total > 0) (sug / total).toFloat().coerceAtLeast(0.01f) else 0.01f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .weight(pPerc)
                .background(Color(0xFF30D158).copy(alpha = if (selectedMacro == "Protein") 1f else 0.3f))
                .clickable { onMacroSelected("Protein") })
        Box(
            Modifier
                .fillMaxHeight()
                .weight(cPerc)
                .background(Color(0xFFFF9F0A).copy(alpha = if (selectedMacro == "Carbs") 1f else 0.3f))
                .clickable { onMacroSelected("Carbs") })
        Box(
            Modifier
                .fillMaxHeight()
                .weight(fPerc)
                .background(Color(0xFF5E5CE6).copy(alpha = if (selectedMacro == "Fett") 1f else 0.3f))
                .clickable { onMacroSelected("Fett") })
        Box(
            Modifier
                .fillMaxHeight()
                .weight(fibPerc)
                .background(Color(0xFF64D2FF).copy(alpha = if (selectedMacro == "Ballaststoffe") 1f else 0.3f))
                .clickable { onMacroSelected("Ballaststoffe") })
        Box(
            Modifier
                .fillMaxHeight()
                .weight(sugPerc)
                .background(Color(0xFFFF2D55).copy(alpha = if (selectedMacro == "Zucker") 1f else 0.3f))
                .clickable { onMacroSelected("Zucker") })
    }
    Spacer(modifier = Modifier.height(12.dp))
    val percent = when (selectedMacro) {
        "Protein" -> pPerc; "Carbs" -> cPerc; "Fett" -> fPerc; "Ballaststoffe" -> fibPerc; else -> sugPerc
    }
    Text(
        "${(percent * 100).toInt()}% der Nährstoff-Verteilung",
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        color = when (selectedMacro) {
            "Protein" -> Color(0xFF30D158); "Carbs" -> Color(0xFFFF9F0A); "Fett" -> Color(0xFF5E5CE6); "Ballaststoffe" -> Color(
                0xFF64D2FF
            ); else -> Color(0xFFFF2D55)
        }
    )
}

@Composable
fun MacroSelectorButton(
    label: String, color: Color, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                if (isSelected) 2.dp else 1.dp,
                if (isSelected) color else Color.Gray.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (isSelected) color else Color.Gray,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSpanSelector(
    selectedSpan: Int,
    accentBlue: Color,
    textColor: Color,
    cardColor: Color,
    onSelectedSpan: (Int) -> Unit,
    onCustomRangeSelected: (Long, Long) -> Unit,
    onInfoClick: () -> Unit = {}
) {
    val spans = listOf(1 to "Tag", 7 to "1W", 14 to "14T", 30 to "1M")
    var showRangePicker by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        spans.forEach { (days, label) ->
            val isSelected = selectedSpan == days
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) accentBlue else Color.Transparent)
                    .clickable { onSelectedSpan(days) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (isSelected) Color.White else textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        IconButton(
            onClick = { showRangePicker = true },
            modifier = Modifier
                .padding(start = 4.dp)
                .size(36.dp)
        ) {
            Icon(
                Icons.Default.DateRange,
                contentDescription = "Benutzerdefiniert",
                tint = if (selectedSpan == -1) accentBlue else textColor
            )
        }

        IconButton(
            onClick = onInfoClick,
            modifier = Modifier
                .padding(end = 4.dp)
                .size(36.dp)
        ) {
            Icon(Icons.Default.Info, contentDescription = "Zeitraum Info", tint = accentBlue)
        }
    }

    if (showRangePicker) {
        val rangePickerColors = DatePickerDefaults.colors(
            containerColor = cardColor,
            titleContentColor = Color.Gray,
            headlineContentColor = textColor,
            weekdayContentColor = Color.Gray,
            subheadContentColor = textColor,
            yearContentColor = textColor,
            currentYearContentColor = accentBlue,
            selectedYearContentColor = Color.White,
            selectedYearContainerColor = accentBlue,
            dayContentColor = textColor,
            disabledDayContentColor = Color.Gray.copy(alpha = 0.5f),
            selectedDayContentColor = Color.White,
            disabledSelectedDayContentColor = Color.Gray.copy(alpha = 0.5f),
            selectedDayContainerColor = accentBlue,
            disabledSelectedDayContainerColor = Color.Gray.copy(alpha = 0.5f),
            todayContentColor = accentBlue,
            todayDateBorderColor = accentBlue,
            dayInSelectionRangeContentColor = textColor,
            dayInSelectionRangeContainerColor = accentBlue.copy(alpha = 0.2f)
        )

        DatePickerDialog(
            onDismissRequest = { showRangePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val start = dateRangePickerState.selectedStartDateMillis
                    val end = dateRangePickerState.selectedEndDateMillis ?: start
                    if (start != null && end != null) {
                        onCustomRangeSelected(start, end)
                    }
                    showRangePicker = false
                }) { Text("Anwenden", color = accentBlue, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showRangePicker = false }) {
                    Text(
                        "Abbrechen",
                        color = textColor
                    )
                }
            },
            colors = DatePickerDefaults.colors(containerColor = cardColor)
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = {
                    Text(
                        "Zeitraum wählen",
                        modifier = Modifier.padding(16.dp),
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                headline = { Spacer(modifier = Modifier.height(0.dp)) },
                showModeToggle = false,
                modifier = Modifier.weight(1f),
                colors = rangePickerColors
            )
        }
    }
}

@Composable
fun FoodRow(
    name: String, valueLabel: String, subLabel: String, cardColor: Color,
    textColor: Color, grayText: Color, valueColor: Color, onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .let { if (onClick != null) it.clickable { onClick() } else it }
            .padding(16.dp)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.SemiBold, color = textColor)
            Text(subLabel, color = grayText, fontSize = 12.sp)
        }
        Text(valueLabel, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = valueColor)
    }
}

@Composable
fun CompareFoodItemCard(
    foodName: String, amountVal: String, metricVal: String,
    cardColor: Color, textColor: Color, grayText: Color, metricColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(cardColor)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                foodName,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                fontSize = 15.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(amountVal, color = grayText, fontSize = 12.sp)
        }
        Text(metricVal, fontWeight = FontWeight.Black, color = metricColor, fontSize = 16.sp)
    }
}

data class MetricData(
    val name: String,
    val val1: Int,
    val val2: Int,
    val unit: String,
    val baseColor: Color
)

fun getMetricValue(food: AggregatedFood, metricName: String): Int {
    return when (metricName) {
        "Kalorien" -> food.calories
        "Protein" -> food.protein.toInt()
        "Carbs" -> food.carbs.toInt()
        "Fett" -> food.fat.toInt()
        "Ballaststoffe" -> food.fiber.toInt()
        "Zucker" -> food.sugar.toInt()
        else -> 0
    }
}

@Composable
fun CompareVisualChart(
    k1: Int, k2: Int, p1: Int, p2: Int, c1: Int, c2: Int,
    f1: Int, f2: Int, fib1: Int, fib2: Int, sug1: Int, sug2: Int,
    label1: String, label2: String,
    cardColor: Color, textColor: Color, grayText: Color, accentBlue: Color,
    onMetricClick: (String) -> Unit
) {
    val week2Color = Color(0xFFFF9F0A)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Vergleich des Tagesdurchschnitts",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = textColor
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(accentBlue)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(label1, fontSize = 11.sp, color = grayText, maxLines = 1)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(week2Color)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(label2, fontSize = 11.sp, color = grayText, maxLines = 1)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val metrics = listOf(
                MetricData("Kalorien", k1, k2, "kcal", accentBlue),
                MetricData("Protein", p1, p2, "g", Color(0xFF30D158)),
                MetricData("Carbs", c1, c2, "g", Color(0xFFFF9F0A)),
                MetricData("Fett", f1, f2, "g", Color(0xFF5E5CE6)),
                MetricData("Ballaststoffe", fib1, fib2, "g", Color(0xFF64D2FF)),
                MetricData("Zucker", sug1, sug2, "g", Color(0xFFFF2D55))
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                metrics.forEach { metric ->
                    val maxVal = maxOf(metric.val1, metric.val2, 1).toFloat()
                    val fill1 = (metric.val1 / maxVal).coerceIn(0.03f, 1f)
                    val fill2 = (metric.val2 / maxVal).coerceIn(0.03f, 1f)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onMetricClick(metric.name) }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                metric.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${metric.val1}${metric.unit} vs ${metric.val2}${metric.unit}",
                                    fontSize = 12.sp,
                                    color = grayText,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Details",
                                    tint = grayText,
                                    modifier = Modifier
                                        .padding(start = 4.dp)
                                        .size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(grayText.copy(alpha = 0.1f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fill1)
                                    .fillMaxHeight()
                                    .background(accentBlue, RoundedCornerShape(4.dp))
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(grayText.copy(alpha = 0.1f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fill2)
                                    .fillMaxHeight()
                                    .background(week2Color, RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}