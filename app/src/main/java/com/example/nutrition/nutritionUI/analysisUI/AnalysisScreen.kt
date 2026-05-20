package com.example.nutrition.nutritionUI.analysisUI

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.nutrition.nutritionUI.foodViewModel.FoodViewModel


enum class AnalysisViewState { OVERVIEW, TREND_DETAIL, MACRO_DETAIL, COMPARE_DETAIL, WATER_DETAIL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(viewModel: FoodViewModel) {
    val allEntries by viewModel.analysisEntries.collectAsState()
    val goalKcal by viewModel.goalKcal.collectAsState()
    val allWaterRecords by viewModel.allWaterRecords.collectAsState()
    val waterGoal by viewModel.waterGoal.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()
    val bgColor = if (isDark) Color(0xFF000000) else Color(0xFFF2F2F7)
    val cardColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFFFFFFF)
    val textColor = if (isDark) Color.White else Color.Black
    val grayText = if (isDark) Color(0xFFAEAEB2) else Color(0xFF8E8E93)
    val accentBlue = if (isDark) Color(0xFF0A84FF) else Color(0xFF007AFF)
    val dividerColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
    val waterBlue = Color(0xFF32ADE6)
    var currentView by rememberSaveable { mutableStateOf(AnalysisViewState.OVERVIEW) }
    var selectedTimeSpan by rememberSaveable { mutableStateOf(7) }
    var selectedFoodForDetails by remember { mutableStateOf<AggregatedFood?>(null) }

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
                            AnalysisViewState.MACRO_DETAIL -> "Nährstoff-Verteilung"
                            AnalysisViewState.COMPARE_DETAIL -> "Wochen-Vergleich"
                            AnalysisViewState.WATER_DETAIL -> "Wasser-Trend"
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
                        cardColor, textColor, grayText, accentBlue, waterBlue,
                        onOpenTrend = { currentView = AnalysisViewState.TREND_DETAIL },
                        onOpenMacros = { currentView = AnalysisViewState.MACRO_DETAIL },
                        onOpenCompare = { currentView = AnalysisViewState.COMPARE_DETAIL },
                        onOpenWater = { currentView = AnalysisViewState.WATER_DETAIL }
                    )

                    AnalysisViewState.TREND_DETAIL -> TrendDetailContent(
                        allEntries,
                        goalKcal,
                        selectedTimeSpan,
                        cardColor,
                        textColor,
                        grayText,
                        accentBlue,
                        dividerColor,
                        onTimeSpanChanged = { selectedTimeSpan = it },
                        onFoodClick = { selectedFoodForDetails = it }
                    )

                    AnalysisViewState.MACRO_DETAIL -> MacroDetailContent(
                        allEntries,
                        selectedTimeSpan,
                        cardColor,
                        textColor,
                        grayText,
                        accentBlue,
                        dividerColor,
                        onTimeSpanChanged = { selectedTimeSpan = it },
                        onFoodClick = { selectedFoodForDetails = it }
                    )

                    AnalysisViewState.COMPARE_DETAIL -> CompareDetailContent(
                        allEntries, cardColor, textColor, grayText, accentBlue
                    )

                    AnalysisViewState.WATER_DETAIL -> WaterDetailContent(
                        allWaterRecords,
                        waterGoal,
                        selectedTimeSpan,
                        cardColor,
                        textColor,
                        grayText,
                        waterBlue,
                        dividerColor,
                        onTimeSpanChanged = { selectedTimeSpan = it }
                    )
                }
            }
        }
    }

    if (selectedFoodForDetails != null) {
        Dialog(onDismissRequest = { selectedFoodForDetails = null }) {
            val food = selectedFoodForDetails!!
            val factorTo100g = if (food.totalGrams > 0) 100.0 / food.totalGrams else 1.0
            val base100gKcal = (food.calories * factorTo100g).toInt()
            val base100gP = food.protein * factorTo100g
            val base100gC = food.carbs * factorTo100g
            val base100gF = food.fat * factorTo100g
            val base100gFib = food.fiber * factorTo100g
            val base100gSug = food.sugar * factorTo100g
            var calcGrams by remember { mutableStateOf(food.totalGrams.toInt().toString()) }
            val currentGrams = calcGrams.toDoubleOrNull() ?: 100.0
            val currentFactor = currentGrams / 100.0

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardColor)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    food.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Gesamt gegessen in diesem Zeitraum: ${food.totalGrams.toInt()}g",
                    color = grayText,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = calcGrams, onValueChange = { calcGrams = it },
                    label = { Text("Ansicht für Menge (g)", color = grayText) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = accentBlue
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { calcGrams = "100" },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = grayText.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("100g", color = textColor, fontSize = 12.sp) }
                    Button(
                        onClick = { calcGrams = food.totalGrams.toInt().toString() },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = grayText.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) { Text("Gesamt", color = textColor, fontSize = 12.sp) }
                }

                Spacer(modifier = Modifier.height(24.dp)); HorizontalDivider(color = dividerColor); Spacer(
                modifier = Modifier.height(16.dp)
            )
                val df = java.text.DecimalFormat("#")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Kalorien:",
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    ); Text(
                    "${(base100gKcal * currentFactor).toInt()} kcal",
                    color = accentBlue,
                    fontWeight = FontWeight.Bold
                )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Protein:",
                        color = textColor
                    ); Text(
                    "${df.format(base100gP * currentFactor)}g",
                    color = Color(0xFF30D158),
                    fontWeight = FontWeight.Bold
                )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Kohlenhydrate:",
                        color = textColor
                    ); Text(
                    "${df.format(base100gC * currentFactor)}g",
                    color = Color(0xFFFF9F0A),
                    fontWeight = FontWeight.Bold
                )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Fett:",
                        color = textColor
                    ); Text(
                    "${df.format(base100gF * currentFactor)}g",
                    color = Color(0xFF5E5CE6),
                    fontWeight = FontWeight.Bold
                )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Ballaststoffe:",
                        color = textColor
                    ); Text(
                    "${df.format(base100gFib * currentFactor)}g",
                    color = Color(0xFF64D2FF),
                    fontWeight = FontWeight.Bold
                )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Zucker:",
                        color = textColor
                    ); Text(
                    "${df.format(base100gSug * currentFactor)}g",
                    color = Color(0xFFFF2D55),
                    fontWeight = FontWeight.Bold
                )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { selectedFoodForDetails = null },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Schließen", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }
    }
}
