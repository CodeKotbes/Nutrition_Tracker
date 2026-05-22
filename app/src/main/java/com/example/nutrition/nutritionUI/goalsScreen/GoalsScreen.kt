package com.example.nutrition.nutritionUI.goalsScreen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.nutrition.nutritionUI.foodViewModel.FoodViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

enum class GoalsViewState { OVERVIEW, WEIGHT_DETAIL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(viewModel: FoodViewModel) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val currentGoal by viewModel.goalKcal.collectAsState()
    val steps by viewModel.currentSteps.collectAsState()
    val burnedKcal by viewModel.totalActivityKcal.collectAsState()
    val weightHistory by viewModel.weightHistory.collectAsState()
    val bgColor = if (isDark) Color(0xFF000000) else Color(0xFFF2F2F7)
    val cardColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFFFFFFF)
    val textColor = if (isDark) Color.White else Color.Black
    val grayText = if (isDark) Color(0xFFAEAEB2) else Color(0xFF8E8E93)
    val accentBlue = if (isDark) Color(0xFF0A84FF) else Color(0xFF007AFF)
    var currentView by rememberSaveable { mutableStateOf(GoalsViewState.OVERVIEW) }
    var ageInput by rememberSaveable { mutableStateOf(viewModel.getSavedAge()) }
    var heightInput by rememberSaveable { mutableStateOf(viewModel.getSavedHeight()) }
    var targetWeightInput by rememberSaveable { mutableStateOf(viewModel.getSavedTargetWeight()) }
    var isMale by rememberSaveable { mutableStateOf(viewModel.getSavedIsMale()) }
    var selectedActivityLevel by rememberSaveable { mutableStateOf(viewModel.getSavedActivityLevel()) }
    var selectedGoalOffset by rememberSaveable { mutableStateOf(viewModel.getSavedGoalOffset()) }
    var selectedStartMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    val sortedHistory = weightHistory.sortedBy { it.timestamp }
    var showLogDialog by rememberSaveable { mutableStateOf(false) }
    val latestWeight = sortedHistory.lastOrNull()?.weight
    val previousWeight =
        if (sortedHistory.size >= 2) sortedHistory[sortedHistory.size - 2].weight else null
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.syncHealthData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BackHandler(enabled = currentView != GoalsViewState.OVERVIEW) {
        currentView = GoalsViewState.OVERVIEW
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (currentView == GoalsViewState.OVERVIEW) "Meine Ziele" else "Gewichtsverlauf",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = textColor
                    )
                },
                navigationIcon = {
                    if (currentView != GoalsViewState.OVERVIEW) {
                        IconButton(onClick = { currentView = GoalsViewState.OVERVIEW }) {
                            Icon(Icons.Default.ArrowBack, "Zurück", tint = accentBlue)
                        }
                    }
                },
                actions = {
                    if (currentView == GoalsViewState.WEIGHT_DETAIL) {
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(accentBlue.copy(alpha = 0.15f))
                                .clickable { showLogDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                "Eintrag hinzufügen",
                                tint = accentBlue,
                                modifier = Modifier.size(20.dp)
                            )
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
            Crossfade(targetState = currentView, label = "Goals Transition") { view ->
                when (view) {
                    GoalsViewState.OVERVIEW -> {
                        OverviewContent(
                            currentGoal = currentGoal,
                            steps = steps,
                            burnedKcal = burnedKcal,
                            latestWeight = latestWeight,
                            previousWeight = previousWeight,
                            selectedGoalOffset = selectedGoalOffset,
                            ageInput = ageInput,
                            heightInput = heightInput,
                            isMale = isMale,
                            selectedActivityLevel = selectedActivityLevel,
                            targetWeightInput = targetWeightInput,
                            onAgeChange = { ageInput = it },
                            onHeightChange = { heightInput = it },
                            onMaleChange = { isMale = it },
                            onActivityChange = { selectedActivityLevel = it },
                            onGoalOffsetChange = { selectedGoalOffset = it },
                            onTargetWeightChange = { targetWeightInput = it },
                            onCalculate = {
                                if (isMale != null && selectedActivityLevel != null && selectedGoalOffset != null && ageInput.isNotBlank() && heightInput.isNotBlank()) {
                                    val weight = latestWeight ?: 80.0
                                    viewModel.calculateAndSetGoal(
                                        isMale!!,
                                        weight,
                                        heightInput.toDoubleOrNull() ?: 180.0,
                                        ageInput.toIntOrNull() ?: 25,
                                        selectedActivityLevel!!,
                                        selectedGoalOffset!!
                                    )
                                }
                            },
                            onOpenWeightDetail = { currentView = GoalsViewState.WEIGHT_DETAIL },
                            viewModel = viewModel,
                            cardColor = cardColor,
                            textColor = textColor,
                            grayText = grayText,
                            accentBlue = accentBlue
                        )
                    }

                    GoalsViewState.WEIGHT_DETAIL -> {
                        WeightDetailContent(
                            sortedHistory = sortedHistory,
                            goalOffset = selectedGoalOffset,
                            targetWeight = targetWeightInput.toDoubleOrNull(),
                            selectedStartMillis = selectedStartMillis,
                            onStartSelected = { selectedStartMillis = it },
                            onGoalOffsetChange = { newOffset ->
                                selectedGoalOffset = newOffset
                                if (isMale != null && selectedActivityLevel != null && ageInput.isNotBlank() && heightInput.isNotBlank()) {
                                    viewModel.calculateAndSetGoal(
                                        isMale = isMale!!,
                                        weightKg = latestWeight ?: 80.0,
                                        heightCm = heightInput.toDoubleOrNull() ?: 180.0,
                                        ageYears = ageInput.toIntOrNull() ?: 25,
                                        activityLevel = selectedActivityLevel!!,
                                        goalOffset = newOffset
                                    )
                                }
                            },
                            onSetTarget = { newTarget ->
                                targetWeightInput = newTarget
                                viewModel.saveTargetWeight(newTarget)
                            },
                            viewModel = viewModel,
                            cardColor = cardColor,
                            textColor = textColor,
                            grayText = grayText,
                            accentBlue = accentBlue
                        )
                    }
                }
            }
        }
    }
    if (showLogDialog) {
        WeightLogDateDialog(
            initialWeight = "",
            initialDate = System.currentTimeMillis(),
            title = "Gewicht eintragen",
            onDismiss = { showLogDialog = false },
            onConfirm = { w, d ->
                viewModel.addWeightEntryWithDate(w, d)
                showLogDialog = false
            },
            cardColor = cardColor,
            textColor = textColor,
            accentBlue = accentBlue,
            grayText = grayText
        )
    }
}

@Composable
fun NutritionProgressDashboardCard(
    title: String,
    unit: String,
    startValue: Double?,
    currentValue: Double?,
    previousValue: Double?,
    targetValue: Double?,
    goalOffset: Int?,
    onSetTargetClick: () -> Unit,
    onAddLogClick: () -> Unit,
    availableEntries: List<Pair<Long, Double>> = emptyList(),
    onStartSelected: (Long) -> Unit = {},
    cardColor: Color,
    textColor: Color,
    accentBlue: Color,
    grayText: Color
) {
    var startDropdownExpanded by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                if (availableEntries.isNotEmpty()) startDropdownExpanded = true
                            }
                            .padding(4.dp)
                    ) {
                        Text("START", style = MaterialTheme.typography.labelSmall, color = grayText)
                        Text(
                            if (startValue != null) "$startValue $unit" else "-",
                            fontWeight = FontWeight.Black,
                            color = textColor
                        )
                    }

                    DropdownMenu(
                        expanded = startDropdownExpanded,
                        onDismissRequest = { startDropdownExpanded = false },
                        shape = RoundedCornerShape(12.dp),
                        containerColor = cardColor,
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        availableEntries.forEachIndexed { index, entry ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            dateFormat.format(Date(entry.first)),
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(
                                            "${entry.second} $unit",
                                            fontWeight = FontWeight.Bold,
                                            color = accentBlue
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.DateRange, null, tint = accentBlue)
                                },
                                onClick = {
                                    onStartSelected(entry.first); startDropdownExpanded = false
                                }
                            )
                            if (index < availableEntries.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    color = grayText.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("AKTUELL", style = MaterialTheme.typography.labelSmall, color = grayText)
                    Text(
                        if (currentValue != null) "$currentValue $unit" else "-",
                        fontWeight = FontWeight.Black,
                        color = accentBlue,
                        fontSize = 20.sp
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSetTargetClick() }
                        .padding(4.dp)
                ) {
                    Text("ZIEL", style = MaterialTheme.typography.labelSmall, color = grayText)
                    Text(
                        if (targetValue != null) "$targetValue $unit" else "Setzen",
                        fontWeight = FontWeight.Black,
                        color = textColor
                    )
                }
            }

            if (targetValue != null && startValue != null && currentValue != null && startValue != targetValue) {
                Spacer(modifier = Modifier.height(16.dp))
                val totalDiff = targetValue - startValue
                val currentDiff = currentValue - startValue
                val rawProgress = (currentDiff / totalDiff).toFloat()
                val progress = rawProgress.coerceIn(0f, 1f)

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = accentBlue,
                    trackColor = grayText.copy(alpha = 0.2f)
                )
                Text(
                    "${(progress * 100).toInt()}% erreicht",
                    style = MaterialTheme.typography.labelSmall,
                    color = grayText,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                )
            }

            if (currentValue != null && previousValue != null) {
                val diff = currentValue - previousValue
                if (abs(diff) >= 0.1) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = grayText.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))

                    val isGoalLoss = (goalOffset ?: 0) < 0
                    val isGood = if (isGoalLoss) diff < 0 else diff > 0
                    val trendColor = if (isGood) Color(0xFF30D158) else Color(0xFFFF453A)
                    val prefix = if (diff > 0) "+" else ""

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Veränderung zum letzten Mal: ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = grayText
                        )
                        Text(
                            "$prefix${String.format(Locale.getDefault(), "%.1f", diff)} $unit",
                            color = trendColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}