package com.example.nutrition.nutritionUI.analysisUI

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TrendDetailContent(
    allEntries: List<DiaryEntry>,
    goalKcal: Int,
    timeSpan: Int,
    customStartMillis: Long?,
    customEndMillis: Long?,
    cardColor: Color,
    textColor: Color,
    grayText: Color,
    accentBlue: Color,
    dividerColor: Color,
    onTimeSpanChanged: (Int) -> Unit,
    onCustomRangeSelected: (Long, Long) -> Unit,
    onFoodClick: (AggregatedFood) -> Unit,
    onJumpToDate: (String) -> Unit
) {
    val filteredEntries = remember(allEntries, timeSpan, customStartMillis, customEndMillis) {
        getFilteredEntries(allEntries, timeSpan, customStartMillis, customEndMillis)
    }
    val aggregatedFood = remember(filteredEntries) { aggregateFood(filteredEntries, "Kalorien") }
    val dailyData = prepareDailyKcal(filteredEntries, timeSpan, customStartMillis, customEndMillis)
    val daysCount = dailyData.size
    val averageKcal = if (daysCount > 0) filteredEntries.sumOf { it.calories } / daysCount else 0
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
                accentBlue = accentBlue,
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
                    "Durchschnitt: $averageKcal kcal / Tag",
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
                                .background(accentBlue.copy(alpha = 0.1f))
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
                                "$amount kcal",
                                color = accentBlue,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                StaticBarChart(
                    data = dailyData,
                    goal = goalKcal,
                    barColor = accentBlue,
                    labelColor = grayText,
                    timeSpan = daysCount,
                    selectedDate = selectedDayInfo?.first,
                    onBarClick = { date, amount ->
                        if (selectedDayInfo?.first == date) selectedDayInfo =
                            null else selectedDayInfo = Pair(date, amount)
                    }
                )
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
                            "Tipp: Tippe auf einen Balken im Diagramm, um Details zu sehen oder springe über das Buch-Symbol in der Liste direkt zum Tagebuch.",
                    color = grayText, fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text(
                        "Verstanden",
                        color = accentBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Composable
fun MacroDetailContent(
    allEntries: List<DiaryEntry>,
    timeSpan: Int,
    customStartMillis: Long?,
    customEndMillis: Long?,
    cardColor: Color,
    textColor: Color,
    grayText: Color,
    accentBlue: Color,
    dividerColor: Color,
    onTimeSpanChanged: (Int) -> Unit,
    onCustomRangeSelected: (Long, Long) -> Unit,
    onFoodClick: (AggregatedFood) -> Unit,
    onJumpToDate: (String) -> Unit
) {
    val filteredEntries = remember(allEntries, timeSpan, customStartMillis, customEndMillis) {
        getFilteredEntries(allEntries, timeSpan, customStartMillis, customEndMillis)
    }
    var selectedMacro by rememberSaveable { mutableStateOf("Protein") }
    var usePieChart by rememberSaveable { mutableStateOf(true) }
    var showInfoDialog by remember { mutableStateOf(false) }
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
        item {
            TimeSpanSelector(
                selectedSpan = timeSpan,
                accentBlue = accentBlue,
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
                        textColor,
                        onMacroSelected = { selectedMacro = it })
                    else CustomFocusMacroBar(
                        totalP,
                        totalC,
                        totalF,
                        totalFiber,
                        totalSugar,
                        selectedMacro,
                        onMacroSelected = { selectedMacro = it })
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
                            "Tipp: Tippe auf das Buch-Symbol im Tagebuch-Verlauf, um direkt zum Tagebuch dieses Tages zu springen.",
                    color = grayText, fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text(
                        "Verstanden",
                        color = accentBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Composable
fun CompareDetailContent(
    allEntries: List<DiaryEntry>,
    cardColor: Color,
    textColor: Color,
    grayText: Color,
    accentBlue: Color,
    selectedMetricScreen: String?,
    onMetricSelected: (String) -> Unit,
    onMetricBack: () -> Unit
) {
    var dateMillis1 by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var dateMillis2 by rememberSaveable { mutableStateOf(System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000) }
    var showInfoDialog by remember { mutableStateOf(false) }
    val entries1 =
        remember(allEntries, dateMillis1) { getEntriesForWeekOfDate(allEntries, dateMillis1) }
    val entries2 =
        remember(allEntries, dateMillis2) { getEntriesForWeekOfDate(allEntries, dateMillis2) }
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
    val label1 = getWeekLabel(dateMillis1)
    val label2 = getWeekLabel(dateMillis2)

    Crossfade(targetState = selectedMetricScreen, label = "MetricDetailCrossfade") { metricScreen ->
        if (metricScreen == null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Durchschnitt vergleichen", color = grayText, fontSize = 14.sp)
                        IconButton(
                            onClick = { showInfoDialog = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "Kalender Erklärung",
                                tint = accentBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WeekCalendarSelector(
                            dateMillis1,
                            cardColor,
                            textColor,
                            accentBlue,
                            Modifier.weight(1f)
                        ) { dateMillis1 = it }
                        Text("vs.", color = grayText, fontWeight = FontWeight.Bold)
                        WeekCalendarSelector(
                            dateMillis2,
                            cardColor,
                            textColor,
                            accentBlue,
                            Modifier.weight(1f)
                        ) { dateMillis2 = it }
                    }
                }
                item {
                    CompareVisualChart(
                        k1 = k1,
                        k2 = k2,
                        p1 = p1,
                        p2 = p2,
                        c1 = c1,
                        c2 = c2,
                        f1 = f1,
                        f2 = f2,
                        fib1 = fib1,
                        fib2 = fib2,
                        sug1 = sug1,
                        sug2 = sug2,
                        label1 = label1,
                        label2 = label2,
                        cardColor = cardColor,
                        textColor = textColor,
                        grayText = grayText,
                        accentBlue = accentBlue,
                        onMetricClick = { clickedMetric -> onMetricSelected(clickedMetric) }
                    )
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        } else {
            MetricDetailSubScreen(
                metricName = metricScreen,
                entries1 = entries1,
                entries2 = entries2,
                k1 = k1,
                k2 = k2,
                p1 = p1,
                p2 = p2,
                c1 = c1,
                c2 = c2,
                f1 = f1,
                f2 = f2,
                fib1 = fib1,
                fib2 = fib2,
                sug1 = sug1,
                sug2 = sug2,
                label1 = label1,
                label2 = label2,
                cardColor = cardColor,
                textColor = textColor,
                grayText = grayText,
                accentBlue = accentBlue,
                onBack = onMetricBack
            )
        }
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
                    "Tippe auf eine der beiden Wochen oben, um den Kalender zu öffnen. Sobald ein Tag ausgewählt wurde, berechnet die App automatisch die gesamte Kalenderwoche (von Montag bis Sonntag), in die dieser Tag fällt.\n\n" +
                            "Tipp: Klicke auf die Balken im Diagramm, um in die Detailansicht zu springen und zu sehen, welche Lebensmittel am meisten dazu beigetragen haben.",
                    color = grayText, fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text(
                        "Verstanden",
                        color = accentBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Composable
fun MetricDetailSubScreen(
    metricName: String, entries1: List<DiaryEntry>, entries2: List<DiaryEntry>,
    k1: Int, k2: Int, p1: Int, p2: Int, c1: Int, c2: Int,
    f1: Int, f2: Int, fib1: Int, fib2: Int, sug1: Int, sug2: Int,
    label1: String, label2: String,
    cardColor: Color, textColor: Color, grayText: Color, accentBlue: Color,
    onBack: () -> Unit
) {
    val metricColor = when (metricName) {
        "Protein" -> Color(0xFF30D158); "Carbs" -> Color(0xFFFF9F0A)
        "Fett" -> Color(0xFF5E5CE6); "Ballaststoffe" -> Color(0xFF64D2FF)
        "Zucker" -> Color(0xFFFF2D55); else -> accentBlue
    }

    val (val1, val2, unit) = when (metricName) {
        "Kalorien" -> Triple(k1, k2, "kcal"); "Protein" -> Triple(p1, p2, "g")
        "Carbs" -> Triple(c1, c2, "g"); "Fett" -> Triple(f1, f2, "g")
        "Ballaststoffe" -> Triple(fib1, fib2, "g"); "Zucker" -> Triple(sug1, sug2, "g")
        else -> Triple(0, 0, "")
    }

    val top1 = remember(entries1) { aggregateFood(entries1, metricName).take(5) }
    val top2 = remember(entries2) { aggregateFood(entries2, metricName).take(5) }
    val diff = val1 - val2
    val diffColor =
        if (diff > 0) Color(0xFFFF453A) else if (diff < 0) Color(0xFF30D158) else grayText
    val diffPrefix = if (diff > 0) "+" else ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(cardColor)
                        .padding(20.dp)
                ) {
                    Text("Differenz im Tagesdurchschnitt", color = grayText, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "$diffPrefix$diff $unit",
                        color = diffColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                }
            }
            item {
                Text(
                    label1,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textColor,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (top1.isEmpty()) Text(
                    "Keine Einträge für diese Woche.",
                    color = grayText,
                    modifier = Modifier.padding(8.dp)
                )
                top1.forEach { food ->
                    CompareFoodItemCard(
                        foodName = food.name,
                        amountVal = "${food.totalGrams.toInt()}g gegessen",
                        metricVal = "${getMetricValue(food, metricName)} $unit",
                        cardColor = cardColor,
                        textColor = textColor,
                        grayText = grayText,
                        metricColor = metricColor
                    )
                }
            }
            item {
                Text(
                    label2,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textColor,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (top2.isEmpty()) Text(
                    "Keine Einträge für diese Woche.",
                    color = grayText,
                    modifier = Modifier.padding(8.dp)
                )
                top2.forEach { food ->
                    CompareFoodItemCard(
                        foodName = food.name,
                        amountVal = "${food.totalGrams.toInt()}g gegessen",
                        metricVal = "${getMetricValue(food, metricName)} $unit",
                        cardColor = cardColor,
                        textColor = textColor,
                        grayText = grayText,
                        metricColor = Color(0xFFFF9F0A)
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}