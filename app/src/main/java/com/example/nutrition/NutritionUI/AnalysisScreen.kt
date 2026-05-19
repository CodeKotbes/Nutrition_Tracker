package com.example.nutrition.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.nutrition.model.DiaryEntry
import java.text.SimpleDateFormat
import java.util.*

enum class AnalysisViewState { OVERVIEW, TREND_DETAIL, MACRO_DETAIL, COMPARE_DETAIL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(viewModel: FoodViewModel) {
    val allEntries by viewModel.analysisEntries.collectAsState()
    val goalKcal by viewModel.goalKcal.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()

    val bgColor = if (isDark) Color(0xFF000000) else Color(0xFFF2F2F7)
    val cardColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFFFFFFF)
    val textColor = if (isDark) Color.White else Color.Black
    val grayText = if (isDark) Color(0xFFAEAEB2) else Color(0xFF8E8E93)
    val accentBlue = if (isDark) Color(0xFF0A84FF) else Color(0xFF007AFF)

    var currentView by rememberSaveable { mutableStateOf(AnalysisViewState.OVERVIEW) }
    var selectedTimeSpan by rememberSaveable { mutableStateOf(7) }

    BackHandler(enabled = currentView != AnalysisViewState.OVERVIEW) {
        currentView = AnalysisViewState.OVERVIEW
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentView) {
                            AnalysisViewState.OVERVIEW -> "Analyse"
                            AnalysisViewState.TREND_DETAIL -> "Kalorien-Trend"
                            AnalysisViewState.MACRO_DETAIL -> "Makro-Verteilung"
                            AnalysisViewState.COMPARE_DETAIL -> "Wochen-Vergleich"
                        },
                        fontWeight = FontWeight.Bold, fontSize = 24.sp, color = textColor
                    )
                },
                navigationIcon = {
                    if (currentView != AnalysisViewState.OVERVIEW) {
                        IconButton(onClick = { currentView = AnalysisViewState.OVERVIEW }) {
                            Icon(Icons.Default.ArrowBack, "Zurück", tint = accentBlue)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Crossfade(targetState = currentView, label = "Analysis Transition") { view ->
                when (view) {
                    AnalysisViewState.OVERVIEW -> OverviewContent(
                        cardColor, textColor, grayText, accentBlue,
                        onOpenTrend = { currentView = AnalysisViewState.TREND_DETAIL },
                        onOpenMacros = { currentView = AnalysisViewState.MACRO_DETAIL },
                        onOpenCompare = { currentView = AnalysisViewState.COMPARE_DETAIL }
                    )

                    AnalysisViewState.TREND_DETAIL -> TrendDetailContent(
                        allEntries,
                        goalKcal,
                        selectedTimeSpan,
                        cardColor,
                        textColor,
                        grayText,
                        accentBlue,
                        onTimeSpanChanged = { selectedTimeSpan = it }
                    )

                    AnalysisViewState.MACRO_DETAIL -> MacroDetailContent(
                        allEntries, selectedTimeSpan, cardColor, textColor, grayText, accentBlue,
                        onTimeSpanChanged = { selectedTimeSpan = it }
                    )

                    AnalysisViewState.COMPARE_DETAIL -> CompareDetailContent(
                        allEntries, cardColor, textColor, grayText, accentBlue
                    )
                }
            }
        }
    }
}

@Composable
fun OverviewContent(
    cardColor: Color,
    textColor: Color,
    grayText: Color,
    accentBlue: Color,
    onOpenTrend: () -> Unit,
    onOpenMacros: () -> Unit,
    onOpenCompare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OverviewCard(
            "Kalorien-Trend",
            "Verlauf",
            Icons.Default.BarChart,
            accentBlue,
            cardColor,
            textColor,
            grayText,
            onOpenTrend
        )
        OverviewCard(
            "Makro-Verteilung",
            "Top-Quellen & Verhältnisse",
            Icons.Default.PieChart,
            Color(0xFFFF9F0A),
            cardColor,
            textColor,
            grayText,
            onOpenMacros
        )
        OverviewCard(
            "Wochen-Vergleich",
            "Diät vs. Aufbau",
            Icons.Default.CompareArrows,
            Color(0xFF30D158),
            cardColor,
            textColor,
            grayText,
            onOpenCompare
        )
    }
}

@Composable
fun OverviewCard(
    title: String,
    subTitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    cardColor: Color,
    textColor: Color,
    grayText: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(cardColor)
            .clickable { onClick() }
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.15f)), contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textColor)
                Text(subTitle, fontSize = 14.sp, color = grayText)
            }
        }
    }
}

@Composable
fun TrendDetailContent(
    allEntries: List<DiaryEntry>,
    goalKcal: Int,
    timeSpan: Int,
    cardColor: Color,
    textColor: Color,
    grayText: Color,
    accentBlue: Color,
    onTimeSpanChanged: (Int) -> Unit
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
        else items(aggregatedFood) { food ->
            FoodRow(
                food.name,
                "${food.calories} kcal",
                "${food.totalGrams.toInt()}g gesamt",
                cardColor,
                textColor,
                grayText,
                accentBlue
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
    onTimeSpanChanged: (Int) -> Unit
) {
    val filteredEntries =
        remember(allEntries, timeSpan) { filterEntriesByDays(allEntries, timeSpan) }
    var selectedMacro by rememberSaveable { mutableStateOf("Protein") }
    var usePieChart by rememberSaveable { mutableStateOf(true) }

    val totalP = filteredEntries.sumOf { it.protein }
    val totalC = filteredEntries.sumOf { it.carbs }
    val totalF = filteredEntries.sumOf { it.fat }
    val totalMacros = totalP + totalC + totalF

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
                        "Makro-Fokus",
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
                Spacer(modifier = Modifier.height(32.dp))

                if (totalMacros > 0) {
                    if (usePieChart) CustomPieChart(
                        totalP,
                        totalC,
                        totalF,
                        selectedMacro,
                        textColor
                    )
                    else CustomFocusMacroBar(totalP, totalC, totalF, selectedMacro)
                } else {
                    Text(
                        "Keine Makro-Daten vorhanden.",
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
        else items(aggregatedFood) { food ->
            val displayVal = when (selectedMacro) {
                "Protein" -> "${food.protein.toInt()}g"; "Carbs" -> "${food.carbs.toInt()}g"; else -> "${food.fat.toInt()}g"
            }
            FoodRow(
                food.name,
                displayVal,
                "${food.totalGrams.toInt()}g gesamt",
                cardColor,
                textColor,
                grayText,
                when (selectedMacro) {
                    "Protein" -> Color(0xFF30D158); "Carbs" -> Color(0xFFFF9F0A); else -> Color(
                    0xFF5E5CE6
                )
                }
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

    val label1 = getWeekShortLabel(offset1)
    val label2 = getWeekShortLabel(offset2)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Durchschnitt pro Tag vergleichen", color = grayText, fontSize = 14.sp)
        }

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
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

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
                DropdownMenuItem(
                    text = {
                        Text(
                            getWeekDropdownLabel(offset),
                            color = textColor,
                            fontSize = 13.sp
                        )
                    },
                    onClick = { onSelect(offset); expanded = false }
                )
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
                Text(label1, color = grayText, fontSize = 12.sp)
                Text(val1Str, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(label2, color = grayText, fontSize = 12.sp)
                Text(val2Str, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = grayText.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(12.dp))

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
    timeSpan: Int
) {
    val maxChartValue = (data.maxOfOrNull { it.second } ?: 1000).coerceAtLeast(goal + 500).toFloat()
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
                color = Color.Red.copy(alpha = 0.6f),
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
                                if (pair.second > goal + 150) Color(0xFFFF453A) else barColor.copy(
                                    alpha = 0.8f
                                )
                            )
                    )
                    if (showLabels) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(pair.first, fontSize = 10.sp, color = labelColor)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomPieChart(p: Double, c: Double, f: Double, selectedMacro: String, textColor: Color) {
    val total = p + c + f
    val pAngle = (p / total * 360).toFloat()
    val cAngle = (c / total * 360).toFloat()
    val fAngle = (f / total * 360).toFloat()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp), contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(160.dp)) {
            val alphaP = if (selectedMacro == "Protein") 1f else 0.2f
            val alphaC = if (selectedMacro == "Carbs") 1f else 0.2f
            val alphaF = if (selectedMacro == "Fett") 1f else 0.2f
            val strokeWidth = 30.dp.toPx()

            drawArc(
                color = Color(0xFF30D158).copy(alpha = alphaP),
                startAngle = -90f,
                sweepAngle = pAngle,
                useCenter = false,
                style = Stroke(strokeWidth)
            )
            drawArc(
                color = Color(0xFFFF9F0A).copy(alpha = alphaC),
                startAngle = -90f + pAngle,
                sweepAngle = cAngle,
                useCenter = false,
                style = Stroke(strokeWidth)
            )
            drawArc(
                color = Color(0xFF5E5CE6).copy(alpha = alphaF),
                startAngle = -90f + pAngle + cAngle,
                sweepAngle = fAngle,
                useCenter = false,
                style = Stroke(strokeWidth)
            )
        }
        val percent = when (selectedMacro) {
            "Protein" -> (p / total * 100); "Carbs" -> (c / total * 100); else -> (f / total * 100)
        }
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
fun CustomFocusMacroBar(p: Double, c: Double, f: Double, selectedMacro: String) {
    val total = p + c + f
    val pPerc = (p / total).toFloat().coerceAtLeast(0.01f)
    val cPerc = (c / total).toFloat().coerceAtLeast(0.01f)
    val fPerc = (f / total).toFloat().coerceAtLeast(0.01f)

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
    }
    Spacer(modifier = Modifier.height(8.dp))
    val percent = when (selectedMacro) {
        "Protein" -> pPerc; "Carbs" -> cPerc; else -> fPerc
    }
    Text(
        "${(percent * 100).toInt()}% deines Verbrauchs",
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        color = when (selectedMacro) {
            "Protein" -> Color(0xFF30D158); "Carbs" -> Color(0xFFFF9F0A); else -> Color(0xFF5E5CE6)
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
            fontSize = 13.sp
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
    valueColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardColor)
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

fun getWeekDropdownLabel(offset: Int): String {
    val sdf = SimpleDateFormat("dd.MM.", Locale.getDefault())
    val endCal = Calendar.getInstance()
    endCal.add(Calendar.DAY_OF_YEAR, -(offset * 7))
    val startCal = Calendar.getInstance()
    startCal.add(Calendar.DAY_OF_YEAR, -(offset * 7) - 6)

    val dateRange = "${sdf.format(startCal.time)} - ${sdf.format(endCal.time)}"

    return when (offset) {
        0 -> "Diese ($dateRange)"
        1 -> "Letzte ($dateRange)"
        else -> "Vor $offset W. ($dateRange)"
    }
}

fun getWeekShortLabel(offset: Int): String {
    val sdf = SimpleDateFormat("dd.MM.", Locale.getDefault())
    val endCal = Calendar.getInstance()
    endCal.add(Calendar.DAY_OF_YEAR, -(offset * 7))
    val startCal = Calendar.getInstance()
    startCal.add(Calendar.DAY_OF_YEAR, -(offset * 7) - 6)

    return "${sdf.format(startCal.time)} - ${sdf.format(endCal.time)}"
}


fun getEntriesForPastWeek(entries: List<DiaryEntry>, weekOffset: Int): List<DiaryEntry> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val resultDates = mutableListOf<String>()
    val cal = Calendar.getInstance()

    cal.add(Calendar.DAY_OF_YEAR, -(weekOffset * 7))
    for (i in 0..6) {
        resultDates.add(sdf.format(cal.time))
        cal.add(Calendar.DAY_OF_YEAR, -1)
    }
    return entries.filter { it.date in resultDates }
}

fun filterEntriesByDays(entries: List<DiaryEntry>, days: Int): List<DiaryEntry> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -(days - 1))
    val startDateStr = sdf.format(cal.time)
    return entries.filter { it.date >= startDateStr }
}

fun prepareDailyKcal(entries: List<DiaryEntry>, days: Int): List<Pair<String, Int>> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val labelSdf = SimpleDateFormat("dd.MM", Locale.getDefault())
    val cal = Calendar.getInstance()
    val dateList = mutableListOf<String>()
    for (i in 0 until days) {
        dateList.add(sdf.format(cal.time)); cal.add(Calendar.DAY_OF_YEAR, -1)
    }
    dateList.reverse()

    return dateList.map { dateStr ->
        val kcal = entries.filter { it.date == dateStr }.sumOf { it.calories }
        val label = if (days <= 7) SimpleDateFormat(
            "E",
            Locale.getDefault()
        ).format(sdf.parse(dateStr)!!) else labelSdf.format(sdf.parse(dateStr)!!)
        label to kcal
    }
}

data class AggregatedFood(
    val name: String,
    val totalGrams: Double,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

fun aggregateFood(entries: List<DiaryEntry>, sortByMacro: String): List<AggregatedFood> {
    val grouped = entries.groupBy { it.foodName }.map { (name, list) ->
        AggregatedFood(
            name,
            list.sumOf { it.amountInGrams },
            list.sumOf { it.calories },
            list.sumOf { it.protein },
            list.sumOf { it.carbs },
            list.sumOf { it.fat })
    }
    return when (sortByMacro) {
        "Protein" -> grouped.sortedByDescending { it.protein }
        "Carbs" -> grouped.sortedByDescending { it.carbs }
        "Fett" -> grouped.sortedByDescending { it.fat }
        else -> grouped.sortedByDescending { it.calories }
    }
}