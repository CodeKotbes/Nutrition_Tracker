package com.example.nutrition.nutritionUI.foodUI

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.nutrition.model.DiaryEntry
import com.example.nutrition.model.FoodItem
import com.example.nutrition.nutritionUI.foodViewModel.FoodViewModel
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodScreen(viewModel: FoodViewModel) {
    val context = LocalContext.current
    val allEntries by viewModel.analysisEntries.collectAsState()
    val diaryEntries by viewModel.diaryEntries.collectAsState()
    val previewProduct by viewModel.scannedProductPreview.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    val allFoodsHistory by viewModel.allFoods.collectAsState()
    val allRecipes by viewModel.allRecipes.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()
    val bgColor = if (isDark) Color(0xFF000000) else Color(0xFFF2F2F7)
    val cardColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFFFFFFF)
    val textColor = if (isDark) Color.White else Color.Black
    val grayText = if (isDark) Color(0xFFAEAEB2) else Color(0xFF8E8E93)
    val accentBlue = if (isDark) Color(0xFF0A84FF) else Color(0xFF007AFF)
    val dividerColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
    val waterBlue = Color(0xFF32ADE6)
    var selectedMeal by rememberSaveable { mutableStateOf<String?>(null) }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var showGoalDialog by rememberSaveable { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<DiaryEntry?>(null) }
    var entryActionSelection by remember { mutableStateOf<DiaryEntry?>(null) }
    var entryToEditAmount by remember { mutableStateOf<DiaryEntry?>(null) }
    var entryToEditFull by remember { mutableStateOf<DiaryEntry?>(null) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val datePickerState =
        rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var showCustomWaterDialog by rememberSaveable { mutableStateOf(false) }
    var showWaterHistorySheet by rememberSaveable { mutableStateOf(false) }
    var showWaterGoalDialog by rememberSaveable { mutableStateOf(false) }
    val totalKcal = diaryEntries.sumOf { it.calories }
    val totalProtein = diaryEntries.sumOf { it.protein }
    val totalCarbs = diaryEntries.sumOf { it.carbs }
    val totalFat = diaryEntries.sumOf { it.fat }
    val totalFiber = diaryEntries.sumOf { it.fiber }
    val totalSugar = diaryEntries.sumOf { it.sugar }
    val goalKcal by viewModel.goalKcal.collectAsState()
    val goalProtein by viewModel.goalProtein.collectAsState()
    val goalCarbs by viewModel.goalCarbs.collectAsState()
    val goalFat by viewModel.goalFat.collectAsState()
    val goalFiber by viewModel.goalFiber.collectAsState()
    val goalSugar by viewModel.goalSugar.collectAsState()
    val waterRecords by viewModel.waterRecords.collectAsState()
    val waterIntake = waterRecords.sumOf { it.amount }
    val waterGoal by viewModel.waterGoal.collectAsState()
    val displayDate = remember(currentDate) {
        val formatIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatOut = SimpleDateFormat("dd. MMMM yyyy", Locale.getDefault())
        val date = formatIn.parse(currentDate) ?: Date()
        formatOut.format(date)
    }
    var jsonToExport by remember { mutableStateOf("") }
    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            uri?.let {
                try {
                    context.contentResolver.openOutputStream(it)?.use { out ->
                        out.write(jsonToExport.toByteArray())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    val onExportFood: (FoodItem) -> Unit = { food ->
        val root = JSONObject()
        root.put("type", "FoodItem")
        root.put("name", food.name)
        root.put("calories", food.calories)
        root.put("protein", food.protein)
        root.put("carbs", food.carbs)
        root.put("fat", food.fat)
        root.put("fiber", food.fiber)
        root.put("sugar", food.sugar)
        jsonToExport = root.toString(4)
        exportLauncher.launch("${food.name.replace(" ", "_")}.json")
    }

    if (showDatePicker) {
        val datePickerColors = DatePickerDefaults.colors(
            containerColor = cardColor,
            titleContentColor = grayText,
            headlineContentColor = textColor,
            weekdayContentColor = grayText,
            subheadContentColor = textColor,
            yearContentColor = textColor,
            currentYearContentColor = accentBlue,
            selectedYearContentColor = Color.White,
            selectedYearContainerColor = accentBlue,
            dayContentColor = textColor,
            disabledDayContentColor = grayText.copy(alpha = 0.5f),
            selectedDayContentColor = Color.White,
            disabledSelectedDayContentColor = grayText.copy(alpha = 0.5f),
            selectedDayContainerColor = accentBlue,
            disabledSelectedDayContainerColor = grayText.copy(alpha = 0.5f),
            todayContentColor = accentBlue,
            todayDateBorderColor = accentBlue
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.setDate(it) }
                    showDatePicker = false
                }) { Text("OK", color = accentBlue, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(
                        "Abbrechen",
                        color = grayText
                    )
                }
            },
            colors = DatePickerDefaults.colors(containerColor = cardColor)
        ) { DatePicker(state = datePickerState, colors = datePickerColors) }
    }

    if (showGoalDialog) {
        Dialog(onDismissRequest = { showGoalDialog = false }) {
            val dialogFocusManager = LocalFocusManager.current
            var inputKcal by remember { mutableStateOf(goalKcal.toString()) }
            var inputProtein by remember { mutableStateOf(goalProtein.toString()) }
            var inputCarbs by remember { mutableStateOf(goalCarbs.toString()) }
            var inputFat by remember { mutableStateOf(goalFat.toString()) }
            var inputFiber by remember { mutableStateOf(goalFiber.toString()) }
            var inputSugar by remember { mutableStateOf(goalSugar.toString()) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardColor)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Tagesziele festlegen",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = textColor
                )

                OutlinedTextField(
                    value = inputKcal,
                    onValueChange = { inputKcal = it },
                    label = { Text("Kalorien Ziel (kcal)", color = grayText) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        dialogFocusManager.moveFocus(FocusDirection.Next)
                    }),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = accentBlue
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = inputProtein,
                        onValueChange = { inputProtein = it },
                        label = { Text("Protein (g)", color = grayText) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = {
                            dialogFocusManager.moveFocus(FocusDirection.Next)
                        }),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = accentBlue
                        )
                    )
                    OutlinedTextField(
                        value = inputCarbs,
                        onValueChange = { inputCarbs = it },
                        label = { Text("Carbs (g)", color = grayText) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = {
                            dialogFocusManager.moveFocus(FocusDirection.Next)
                        }),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = accentBlue
                        )
                    )
                    OutlinedTextField(
                        value = inputFat,
                        onValueChange = { inputFat = it },
                        label = { Text("Fett (g)", color = grayText) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = {
                            dialogFocusManager.moveFocus(FocusDirection.Next)
                        }),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = accentBlue
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = inputFiber,
                        onValueChange = { inputFiber = it },
                        label = { Text("Ballastst. (g)", color = grayText) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = {
                            dialogFocusManager.moveFocus(FocusDirection.Next)
                        }),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = accentBlue
                        )
                    )
                    OutlinedTextField(
                        value = inputSugar,
                        onValueChange = { inputSugar = it },
                        label = { Text("Zucker Max. (g)", color = grayText) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { dialogFocusManager.clearFocus() }),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = accentBlue
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showGoalDialog = false },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = grayText.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Abbrechen", color = textColor, fontWeight = FontWeight.SemiBold) }
                    Button(
                        onClick = {
                            viewModel.updateAllGoals(
                                inputKcal.toIntOrNull() ?: 2500, inputProtein.toIntOrNull() ?: 150,
                                inputCarbs.toIntOrNull() ?: 250, inputFat.toIntOrNull() ?: 80,
                                inputFiber.toIntOrNull() ?: 30, inputSugar.toIntOrNull() ?: 50
                            )
                            showGoalDialog = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Speichern", color = Color.White, fontWeight = FontWeight.Bold) }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showWaterGoalDialog) {
        Dialog(onDismissRequest = { showWaterGoalDialog = false }) {
            val dialogFocusManager = LocalFocusManager.current
            var inputWaterGoal by remember { mutableStateOf(waterGoal.toString()) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardColor)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Tägliches Wasserziel",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = inputWaterGoal,
                    onValueChange = { inputWaterGoal = it },
                    label = { Text("Ziel in ml", color = grayText) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { dialogFocusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = waterBlue
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showWaterGoalDialog = false },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = grayText.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Abbrechen", color = textColor, fontWeight = FontWeight.SemiBold) }
                    Button(
                        onClick = {
                            val newGoal = inputWaterGoal.toIntOrNull()
                            if (newGoal != null && newGoal > 0) viewModel.updateWaterGoal(newGoal)
                            showWaterGoalDialog = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = waterBlue),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Speichern", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }

    if (showCustomWaterDialog) {
        Dialog(onDismissRequest = { showCustomWaterDialog = false }) {
            val dialogFocusManager = LocalFocusManager.current
            var customWaterInput by remember { mutableStateOf("") }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardColor)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Wassermenge hinzufügen",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = customWaterInput,
                    onValueChange = { customWaterInput = it },
                    label = { Text("Menge in ml", color = grayText) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { dialogFocusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = waterBlue
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showCustomWaterDialog = false },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = grayText.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Abbrechen", color = textColor, fontWeight = FontWeight.SemiBold) }
                    Button(
                        onClick = {
                            val amount = customWaterInput.toIntOrNull()
                            if (amount != null && amount > 0) viewModel.addWaterRecord(amount)
                            showCustomWaterDialog = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = waterBlue),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Hinzufügen", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.changeDate(-7) }) {
                            Icon(
                                Icons.Default.ChevronLeft,
                                "Zurück",
                                tint = accentBlue
                            )
                        }
                        Text(
                            displayDate,
                            modifier = Modifier.padding(horizontal = 8.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = textColor
                        )
                        IconButton(onClick = { viewModel.changeDate(7) }) {
                            Icon(
                                Icons.Default.ChevronRight,
                                "Vor",
                                tint = accentBlue
                            )
                        }
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.setDate(System.currentTimeMillis()) }) {
                        Text("Heute", color = accentBlue, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Datum wählen",
                            tint = accentBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgColor,
                    titleContentColor = textColor
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalWeekCalendar(
                currentDateStr = currentDate,
                allEntries = allEntries,
                accentBlue = accentBlue,
                textColor = textColor,
                grayText = grayText,
                cardColor = cardColor,
                onDateSelected = { viewModel.setDate(it) }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    DashboardSummary(
                        totalKcal, goalKcal, totalProtein, goalProtein, totalCarbs, goalCarbs,
                        totalFat, goalFat, totalFiber, goalFiber, totalSugar, goalSugar,
                        cardColor, textColor, grayText, accentBlue, dividerColor,
                        onEditGoalsClick = { showGoalDialog = true }
                    )
                }

                val meals = listOf("Frühstück", "Mittagessen", "Abendessen", "Snacks")
                meals.forEach { mealName ->
                    item {
                        val entriesForMeal = diaryEntries.filter { it.mealType == mealName }
                        MealSection(
                            title = mealName,
                            entries = entriesForMeal,
                            cardColor = cardColor,
                            textColor = textColor,
                            grayText = grayText,
                            accentBlue = accentBlue,
                            dividerColor = dividerColor,
                            onAddClick = { selectedMeal = mealName; showAddSheet = true },
                            onDeleteClick = { entryToDelete = it },
                            onEntryClick = { entryActionSelection = it }
                        )
                    }
                }

                item {
                    WaterTrackerSection(
                        waterIntake = waterIntake,
                        waterGoal = waterGoal,
                        cardColor = cardColor,
                        textColor = textColor,
                        grayText = grayText,
                        waterBlue = waterBlue,
                        onAddWater = { amount -> viewModel.addWaterRecord(amount) },
                        onCustomClick = { showCustomWaterDialog = true },
                        onCardClick = { showWaterHistorySheet = true },
                        onEditGoalClick = { showWaterGoalDialog = true }
                    )
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }

        if (showWaterHistorySheet) {
            val waterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showWaterHistorySheet = false },
                sheetState = waterSheetState,
                containerColor = bgColor
            ) {
                WaterHistorySheetContent(
                    records = waterRecords,
                    viewModel = viewModel,
                    bgColor = bgColor,
                    cardColor = cardColor,
                    textColor = textColor,
                    grayText = grayText,
                    waterBlue = waterBlue,
                    dividerColor = dividerColor
                )
            }
        }

        if (entryActionSelection != null) {
            Dialog(onDismissRequest = { entryActionSelection = null }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardColor)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Optionen für '${entryActionSelection!!.foodName}'",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = {
                            entryToEditAmount = entryActionSelection; entryActionSelection = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            "Menge anpassen",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            entryToEditFull = entryActionSelection; entryActionSelection = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = grayText.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            "Produkt bearbeiten",
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (entryToEditAmount != null) {
            Dialog(onDismissRequest = { entryToEditAmount = null }) {
                val dialogFocusManager = LocalFocusManager.current
                var editGrams by remember {
                    mutableStateOf(
                        entryToEditAmount!!.amountInGrams.toInt().toString()
                    )
                }
                val newGrams = editGrams.toDoubleOrNull() ?: 0.0

                val factor =
                    if (entryToEditAmount!!.amountInGrams > 0) newGrams / entryToEditAmount!!.amountInGrams else 0.0
                val newKcal = (entryToEditAmount!!.calories * factor).toInt()
                val newP = entryToEditAmount!!.protein * factor
                val newC = entryToEditAmount!!.carbs * factor
                val newF = entryToEditAmount!!.fat * factor
                val newFib = entryToEditAmount!!.fiber * factor
                val newSug = entryToEditAmount!!.sugar * factor

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardColor)
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Menge anpassen",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = textColor
                        )

                        IconButton(onClick = {
                            val f = 100.0 / entryToEditAmount!!.amountInGrams
                            val root = JSONObject()
                            root.put("type", "FoodItem")
                            root.put("name", entryToEditAmount!!.foodName)
                            root.put("calories", (entryToEditAmount!!.calories * f).toInt())
                            root.put("protein", entryToEditAmount!!.protein * f)
                            root.put("carbs", entryToEditAmount!!.carbs * f)
                            root.put("fat", entryToEditAmount!!.fat * f)
                            root.put("fiber", entryToEditAmount!!.fiber * f)
                            root.put("sugar", entryToEditAmount!!.sugar * f)
                            jsonToExport = root.toString(4)
                            exportLauncher.launch(
                                "${
                                    entryToEditAmount!!.foodName.replace(
                                        " ",
                                        "_"
                                    )
                                }.json"
                            )
                        }) {
                            Icon(Icons.Default.Share, "Zutat Exportieren", tint = accentBlue)
                        }
                    }

                    Text(entryToEditAmount!!.foodName, color = grayText, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editGrams,
                        onValueChange = { editGrams = it },
                        label = { Text("Neue Menge in g", color = grayText) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { dialogFocusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = accentBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val df = java.text.DecimalFormat("#.#")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Kalorien:", color = textColor); Text(
                        "$newKcal kcal",
                        color = accentBlue,
                        fontWeight = FontWeight.Bold
                    )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Protein:", color = textColor); Text(
                        "${df.format(newP)}g",
                        color = Color(0xFF30D158),
                        fontWeight = FontWeight.Bold
                    )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Carbs:", color = textColor); Text(
                        "${df.format(newC)}g",
                        color = Color(0xFFFF9F0A),
                        fontWeight = FontWeight.Bold
                    )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Fett:", color = textColor); Text(
                        "${df.format(newF)}g",
                        color = Color(0xFF5E5CE6),
                        fontWeight = FontWeight.Bold
                    )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ballaststoffe:", color = textColor); Text(
                        "${df.format(newFib)}g",
                        color = Color(0xFF64D2FF),
                        fontWeight = FontWeight.Bold
                    )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Zucker:", color = textColor); Text(
                        "${df.format(newSug)}g",
                        color = Color(0xFFFF2D55),
                        fontWeight = FontWeight.Bold
                    )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { entryToEditAmount = null },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = grayText.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("Abbrechen", color = textColor, fontWeight = FontWeight.SemiBold) }
                        Button(
                            onClick = {
                                val updated = entryToEditAmount!!.copy(
                                    amountInGrams = newGrams,
                                    calories = newKcal,
                                    protein = newP,
                                    carbs = newC,
                                    fat = newF,
                                    fiber = newFib,
                                    sugar = newSug
                                )
                                viewModel.updateDiaryEntry(updated)
                                entryToEditAmount = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("Speichern", color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }

        if (entryToEditFull != null) {
            Dialog(onDismissRequest = { entryToEditFull = null }) {
                val dialogFocusManager = LocalFocusManager.current
                val currentFactor = (entryToEditFull?.amountInGrams ?: 100.0) / 100.0
                var editName by remember(entryToEditFull) {
                    mutableStateOf(
                        entryToEditFull?.foodName ?: ""
                    )
                }
                var editKcal by remember(entryToEditFull) {
                    mutableStateOf(
                        if (currentFactor > 0) (entryToEditFull!!.calories / currentFactor).toInt()
                            .toString() else "0"
                    )
                }
                var editProtein by remember(entryToEditFull) {
                    mutableStateOf(
                        if (currentFactor > 0) (entryToEditFull!!.protein / currentFactor).toInt()
                            .toString() else "0"
                    )
                }
                var editCarbs by remember(entryToEditFull) {
                    mutableStateOf(
                        if (currentFactor > 0) (entryToEditFull!!.carbs / currentFactor).toInt()
                            .toString() else "0"
                    )
                }
                var editFat by remember(entryToEditFull) {
                    mutableStateOf(
                        if (currentFactor > 0) (entryToEditFull!!.fat / currentFactor).toInt()
                            .toString() else "0"
                    )
                }
                var editFiber by remember(entryToEditFull) {
                    mutableStateOf(
                        if (currentFactor > 0) (entryToEditFull!!.fiber / currentFactor).toInt()
                            .toString() else "0"
                    )
                }
                var editSugar by remember(entryToEditFull) {
                    mutableStateOf(
                        if (currentFactor > 0) (entryToEditFull!!.sugar / currentFactor).toInt()
                            .toString() else "0"
                    )
                }
                var editGrams by remember(entryToEditFull) {
                    mutableStateOf(
                        entryToEditFull?.amountInGrams?.toInt()?.toString() ?: "100"
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardColor)
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Produkt bearbeiten",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = textColor
                        )
                        IconButton(onClick = {
                            val f = 100.0 / entryToEditFull!!.amountInGrams
                            val root = JSONObject()
                            root.put("type", "FoodItem")
                            root.put("name", entryToEditFull!!.foodName)
                            root.put("calories", (entryToEditFull!!.calories * f).toInt())
                            root.put("protein", entryToEditFull!!.protein * f)
                            root.put("carbs", entryToEditFull!!.carbs * f)
                            root.put("fat", entryToEditFull!!.fat * f)
                            root.put("fiber", entryToEditFull!!.fiber * f)
                            root.put("sugar", entryToEditFull!!.sugar * f)
                            jsonToExport = root.toString(4)
                            exportLauncher.launch(
                                "${
                                    entryToEditFull!!.foodName.replace(
                                        " ",
                                        "_"
                                    )
                                }.json"
                            )
                        }) { Icon(Icons.Default.Share, "Zutat Exportieren", tint = accentBlue) }
                    }

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name", color = grayText) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = {
                            dialogFocusManager.moveFocus(FocusDirection.Next)
                        }),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = accentBlue
                        )
                    )
                    OutlinedTextField(
                        value = editKcal,
                        onValueChange = { editKcal = it },
                        label = { Text("Kalorien (pro 100g)", color = grayText) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = {
                            dialogFocusManager.moveFocus(FocusDirection.Next)
                        }),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = accentBlue
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editProtein,
                            onValueChange = { editProtein = it },
                            label = { Text("Protein", color = grayText) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = {
                                dialogFocusManager.moveFocus(FocusDirection.Next)
                            }),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = accentBlue
                            )
                        )
                        OutlinedTextField(
                            value = editCarbs,
                            onValueChange = { editCarbs = it },
                            label = { Text("Carbs", color = grayText) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = {
                                dialogFocusManager.moveFocus(FocusDirection.Next)
                            }),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = accentBlue
                            )
                        )
                        OutlinedTextField(
                            value = editFat,
                            onValueChange = { editFat = it },
                            label = { Text("Fett", color = grayText) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = {
                                dialogFocusManager.moveFocus(FocusDirection.Next)
                            }),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = accentBlue
                            )
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editFiber,
                            onValueChange = { editFiber = it },
                            label = { Text("Ballastst.", color = grayText) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = {
                                dialogFocusManager.moveFocus(FocusDirection.Next)
                            }),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = accentBlue
                            )
                        )
                        OutlinedTextField(
                            value = editSugar,
                            onValueChange = { editSugar = it },
                            label = { Text("Zucker", color = grayText) },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = {
                                dialogFocusManager.moveFocus(FocusDirection.Next)
                            }),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = accentBlue
                            )
                        )
                    }

                    HorizontalDivider(
                        color = dividerColor,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    OutlinedTextField(
                        value = editGrams,
                        onValueChange = { editGrams = it },
                        label = { Text("Menge in g", color = grayText) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { dialogFocusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = accentBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { entryToEditFull = null },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = grayText.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("Abbrechen", color = textColor, fontWeight = FontWeight.SemiBold) }
                        Button(
                            onClick = {
                                val grams = editGrams.toDoubleOrNull() ?: 100.0
                                val f = grams / 100.0
                                val updated = entryToEditFull!!.copy(
                                    foodName = editName,
                                    amountInGrams = grams,
                                    calories = ((editKcal.toIntOrNull() ?: 0) * f).toInt(),
                                    protein = (editProtein.toDoubleOrNull() ?: 0.0) * f,
                                    carbs = (editCarbs.toDoubleOrNull() ?: 0.0) * f,
                                    fat = (editFat.toDoubleOrNull() ?: 0.0) * f,
                                    fiber = (editFiber.toDoubleOrNull() ?: 0.0) * f,
                                    sugar = (editSugar.toDoubleOrNull() ?: 0.0) * f
                                )
                                viewModel.updateDiaryEntry(updated)
                                entryToEditFull = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("Speichern", color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (entryToDelete != null) {
            Dialog(onDismissRequest = { entryToDelete = null }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardColor)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Eintrag entfernen",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Möchtest du '${entryToDelete?.foodName}' wirklich aus deinem Tagebuch löschen?",
                        color = grayText,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { entryToDelete = null },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = grayText.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("Abbrechen", color = textColor, fontWeight = FontWeight.SemiBold) }
                        Button(
                            onClick = {
                                entryToDelete?.let { viewModel.deleteDiaryEntry(it) }; entryToDelete =
                                null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A)),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("Löschen", color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }

        if (showAddSheet) {
            val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(onDismissRequest = {
                showAddSheet = false; viewModel.clearPreview(); viewModel.clearBarcodeError()
            }, sheetState = addSheetState, containerColor = bgColor) {
                AddFoodSheetContent(
                    viewModel = viewModel,
                    mealName = selectedMeal ?: "",
                    previewProduct = previewProduct,
                    searchResults = searchResults,
                    historyFoods = allFoodsHistory,
                    recipes = allRecipes,
                    bgColor = bgColor,
                    cardColor = cardColor,
                    textColor = textColor,
                    grayText = grayText,
                    accentBlue = accentBlue,
                    dividerColor = dividerColor,
                    onTextSearch = { viewModel.searchFoodByName(it) },
                    onProductSelected = { viewModel.selectProductForPreview(it) },
                    onAdd = { food, grams ->
                        viewModel.addFoodToDiary(
                            food,
                            grams,
                            selectedMeal ?: ""
                        ); showAddSheet = false
                    },
                    onCustomAdd = { name, kcal, p, c, f, fiber, sugar, grams ->
                        viewModel.addCustomFoodToDiary(
                            name,
                            kcal,
                            p,
                            c,
                            f,
                            fiber,
                            sugar,
                            grams,
                            selectedMeal ?: ""
                        ); showAddSheet = false
                    },
                    onRecipeAdd = {
                        viewModel.addRecipeToDiary(
                            it,
                            selectedMeal ?: ""
                        ); showAddSheet = false
                    },
                    onExportFood = onExportFood
                )
            }
        }
    }
}