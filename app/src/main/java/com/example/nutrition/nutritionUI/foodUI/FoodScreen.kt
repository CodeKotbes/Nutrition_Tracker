package com.example.nutrition.nutritionUI.foodUI

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.nutrition.model.DiaryEntry
import com.example.nutrition.nutritionUI.foodViewModel.FoodViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodScreen(viewModel: FoodViewModel) {
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
    var entryToEdit by remember { mutableStateOf<DiaryEntry?>(null) }
    var editGramsInput by rememberSaveable { mutableStateOf("") }
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
        val formatOut = SimpleDateFormat("dd. MMM yyyy", Locale.getDefault())
        val date = formatIn.parse(currentDate) ?: Date()
        formatOut.format(date)
    }

    if (showDatePicker) {
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
        ) { DatePicker(state = datePickerState) }
    }

    if (showGoalDialog) {
        Dialog(onDismissRequest = { showGoalDialog = false }) {
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
                    .padding(24.dp),
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
            }
        }
    }

    if (showWaterGoalDialog) {
        Dialog(onDismissRequest = { showWaterGoalDialog = false }) {
            var inputWaterGoal by remember { mutableStateOf(waterGoal.toString()) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardColor)
                    .padding(24.dp),
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
                    value = inputWaterGoal, onValueChange = { inputWaterGoal = it },
                    label = { Text("Ziel in ml", color = grayText) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
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
                            if (newGoal != null && newGoal > 0) {
                                viewModel.updateWaterGoal(newGoal)
                            }
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
            var customWaterInput by remember { mutableStateOf("") }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardColor)
                    .padding(24.dp),
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
                    value = customWaterInput, onValueChange = { customWaterInput = it },
                    label = { Text("Menge in ml", color = grayText) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
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
                            if (amount != null && amount > 0) {
                                viewModel.addWaterRecord(amount)
                            }
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.changeDate(-1) }) {
                            Icon(
                                Icons.Default.ChevronLeft,
                                "Zurück",
                                tint = accentBlue
                            )
                        }
                        TextButton(onClick = { showDatePicker = true }) {
                            Text(
                                displayDate,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = textColor
                            )
                        }
                        IconButton(onClick = { viewModel.changeDate(1) }) {
                            Icon(
                                Icons.Default.ChevronRight,
                                "Vor",
                                tint = accentBlue
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgColor,
                    titleContentColor = textColor
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
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
                        onEntryClick = {
                            entryToEdit = it; editGramsInput = it.amountInGrams.toInt().toString()
                        }
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

        if (showWaterHistorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showWaterHistorySheet = false },
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

        if (entryToEdit != null) {
            Dialog(onDismissRequest = { entryToEdit = null }) {
                val currentFactor = (entryToEdit?.amountInGrams ?: 100.0) / 100.0
                var editName by remember(entryToEdit) {
                    mutableStateOf(
                        entryToEdit?.foodName ?: ""
                    )
                }
                var editKcal by remember(entryToEdit) {
                    mutableStateOf(
                        if (currentFactor > 0) (entryToEdit!!.calories / currentFactor).toInt()
                            .toString() else "0"
                    )
                }
                var editProtein by remember(entryToEdit) {
                    mutableStateOf(
                        if (currentFactor > 0) (entryToEdit!!.protein / currentFactor).toInt()
                            .toString() else "0"
                    )
                }
                var editCarbs by remember(entryToEdit) {
                    mutableStateOf(
                        if (currentFactor > 0) (entryToEdit!!.carbs / currentFactor).toInt()
                            .toString() else "0"
                    )
                }
                var editFat by remember(entryToEdit) {
                    mutableStateOf(
                        if (currentFactor > 0) (entryToEdit!!.fat / currentFactor).toInt()
                            .toString() else "0"
                    )
                }
                var editFiber by remember(entryToEdit) {
                    mutableStateOf(
                        if (currentFactor > 0) (entryToEdit!!.fiber / currentFactor).toInt()
                            .toString() else "0"
                    )
                }
                var editSugar by remember(entryToEdit) {
                    mutableStateOf(
                        if (currentFactor > 0) (entryToEdit!!.sugar / currentFactor).toInt()
                            .toString() else "0"
                    )
                }
                var editGrams by remember(entryToEdit) {
                    mutableStateOf(
                        entryToEdit?.amountInGrams?.toInt()?.toString() ?: "100"
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardColor)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Eintrag korrigieren",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = textColor
                    )

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name", color = grayText) },
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        label = { Text("Menge in Gramm", color = grayText) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            onClick = { entryToEdit = null },
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
                                val updated = entryToEdit!!.copy(
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
                                entryToEdit = null
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
            ModalBottomSheet(onDismissRequest = {
                showAddSheet = false; viewModel.clearPreview(); viewModel.clearBarcodeError()
            }, containerColor = bgColor) {
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
                        )
                        showAddSheet = false
                    },
                    onRecipeAdd = {
                        viewModel.addRecipeToDiary(
                            it,
                            selectedMeal ?: ""
                        ); showAddSheet = false
                    }
                )
            }
        }
    }
}
