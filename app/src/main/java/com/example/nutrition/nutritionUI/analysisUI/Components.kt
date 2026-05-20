package com.example.nutrition.nutritionUI.analysisUI

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WeekDropdownSelector(
    currentOffset: Int,
    cardColor: Color,
    textColor: Color,
    accentBlue: Color,
    modifier: Modifier = Modifier,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(cardColor)
                .clickable { expanded = true }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                getWeekDropdownLabel(currentOffset),
                color = accentBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                maxLines = 1
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Wählen", tint = accentBlue)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(cardColor)
        ) {
            (0..3).forEach { offset ->
                DropdownMenuItem(text = {
                    Text(
                        getWeekDropdownLabel(offset),
                        color = textColor,
                        fontSize = 13.sp
                    )
                }, onClick = { onSelect(offset); expanded = false })
            }
        }
    }
}

@Composable
fun CompareRowCard(
    title: String,
    val1Str: String,
    val2Str: String,
    diff: Double,
    unit: String,
    label1: String,
    label2: String,
    cardColor: Color,
    textColor: Color,
    grayText: Color
) {
    val diffColor =
        if (diff > 0) Color(0xFFFF453A) else if (diff < 0) Color(0xFF30D158) else grayText
    val diffPrefix = if (diff > 0) "+" else ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardColor)
            .padding(20.dp)
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(label1, color = grayText, fontSize = 12.sp); Text(
                val1Str,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = textColor
            )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    label2,
                    color = grayText,
                    fontSize = 12.sp
                ); Text(val2Str, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
            }
        }
        Spacer(modifier = Modifier.height(12.dp)); HorizontalDivider(color = grayText.copy(alpha = 0.2f)); Spacer(
        modifier = Modifier.height(12.dp)
    )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Differenz", color = grayText, fontSize = 14.sp)
            Text(
                text = "$diffPrefix${diff.toInt()} $unit",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = diffColor
            )
        }
    }
}

@Composable
fun StaticBarChart(
    data: List<Pair<String, Int>>,
    goal: Int,
    barColor: Color,
    labelColor: Color,
    timeSpan: Int,
    isWater: Boolean = false
) {
    val maxChartValue =
        (data.maxOfOrNull { it.second } ?: 1000).coerceAtLeast(goal + if (isWater) 500 else 500)
            .toFloat()
    val showLabels = timeSpan <= 7

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
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
            data.forEach { pair ->
                val fillHeightPercentage = (pair.second / maxChartValue).coerceIn(0.02f, 1.0f)
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (timeSpan == 30) 0.8f else 0.6f)
                            .fillMaxHeight(fillHeightPercentage)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                if (!isWater && pair.second > goal + 150) Color(0xFFFF453A) else barColor.copy(
                                    alpha = 0.8f
                                )
                            )
                    )
                    if (showLabels) {
                        Spacer(modifier = Modifier.height(8.dp)); Text(
                            pair.first,
                            fontSize = 10.sp,
                            color = labelColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomPieChart(
    p: Double,
    c: Double,
    f: Double,
    fib: Double,
    sug: Double,
    selectedMacro: String,
    textColor: Color
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
        Canvas(modifier = Modifier.size(160.dp)) {
            val strokeWidth = 30.dp.toPx()
            var startAngle = -90f
            drawArc(
                color = Color(0xFF30D158).copy(alpha = if (selectedMacro == "Protein") 1f else 0.2f),
                startAngle = startAngle,
                sweepAngle = pAngle,
                useCenter = false,
                style = Stroke(strokeWidth)
            ); startAngle += pAngle
            drawArc(
                color = Color(0xFFFF9F0A).copy(alpha = if (selectedMacro == "Carbs") 1f else 0.2f),
                startAngle = startAngle,
                sweepAngle = cAngle,
                useCenter = false,
                style = Stroke(strokeWidth)
            ); startAngle += cAngle
            drawArc(
                color = Color(0xFF5E5CE6).copy(alpha = if (selectedMacro == "Fett") 1f else 0.2f),
                startAngle = startAngle,
                sweepAngle = fAngle,
                useCenter = false,
                style = Stroke(strokeWidth)
            ); startAngle += fAngle
            drawArc(
                color = Color(0xFF64D2FF).copy(alpha = if (selectedMacro == "Ballaststoffe") 1f else 0.2f),
                startAngle = startAngle,
                sweepAngle = fibAngle,
                useCenter = false,
                style = Stroke(strokeWidth)
            ); startAngle += fibAngle
            drawArc(
                color = Color(0xFFFF2D55).copy(alpha = if (selectedMacro == "Zucker") 1f else 0.2f),
                startAngle = startAngle,
                sweepAngle = sugAngle,
                useCenter = false,
                style = Stroke(strokeWidth)
            )
        }
        val percent = if (total > 0) {
            when (selectedMacro) {
                "Protein" -> (p / total * 100); "Carbs" -> (c / total * 100); "Fett" -> (f / total * 100); "Ballaststoffe" -> (fib / total * 100); else -> (sug / total * 100)
            }
        } else 0.0
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${percent.toInt()}%",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Text(selectedMacro, fontSize = 14.sp, color = Color.Gray)
        }
    }
}

@Composable
fun CustomFocusMacroBar(
    p: Double,
    c: Double,
    f: Double,
    fib: Double,
    sug: Double,
    selectedMacro: String
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
            .height(30.dp)
            .clip(RoundedCornerShape(15.dp))
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .weight(pPerc)
                .background(Color(0xFF30D158).copy(alpha = if (selectedMacro == "Protein") 1f else 0.2f))
        )
        Box(
            Modifier
                .fillMaxHeight()
                .weight(cPerc)
                .background(Color(0xFFFF9F0A).copy(alpha = if (selectedMacro == "Carbs") 1f else 0.2f))
        )
        Box(
            Modifier
                .fillMaxHeight()
                .weight(fPerc)
                .background(Color(0xFF5E5CE6).copy(alpha = if (selectedMacro == "Fett") 1f else 0.2f))
        )
        Box(
            Modifier
                .fillMaxHeight()
                .weight(fibPerc)
                .background(Color(0xFF64D2FF).copy(alpha = if (selectedMacro == "Ballaststoffe") 1f else 0.2f))
        )
        Box(
            Modifier
                .fillMaxHeight()
                .weight(sugPerc)
                .background(Color(0xFFFF2D55).copy(alpha = if (selectedMacro == "Zucker") 1f else 0.2f))
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    val percent = when (selectedMacro) {
        "Protein" -> pPerc; "Carbs" -> cPerc; "Fett" -> fPerc; "Ballaststoffe" -> fibPerc; else -> sugPerc
    }
    Text(
        "${(percent * 100).toInt()}% des Nährstoffgewichts",
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
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
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

@Composable
fun TimeSpanSelector(
    selectedSpan: Int,
    accentBlue: Color,
    textColor: Color,
    cardColor: Color,
    onSelected: (Int) -> Unit
) {
    val spans = listOf(1 to "Tag", 7 to "7T", 14 to "14T", 30 to "1M")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
            .padding(4.dp)
    ) {
        spans.forEach { (days, label) ->
            val isSelected = selectedSpan == days
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) accentBlue else Color.Transparent)
                    .clickable { onSelected(days) }
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
    }
}

@Composable
fun FoodRow(
    name: String,
    valueLabel: String,
    subLabel: String,
    cardColor: Color,
    textColor: Color,
    grayText: Color,
    valueColor: Color,
    onClick: (() -> Unit)? = null
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
            Text(
                name,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            ); Text(subLabel, color = grayText, fontSize = 12.sp)
        }
        Text(valueLabel, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = valueColor)
    }
}