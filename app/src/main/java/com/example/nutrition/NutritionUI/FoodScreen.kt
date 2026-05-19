package com.example.nutrition.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.nutrition.NutritionUI.BarcodeScanner
import com.example.nutrition.model.DiaryEntry
import com.example.nutrition.model.FoodItem
import com.example.nutrition.model.Recipe
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

    var selectedMeal by rememberSaveable { mutableStateOf<String?>(null) }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }

    var entryToDelete by remember { mutableStateOf<DiaryEntry?>(null) }
    var entryToEdit by remember { mutableStateOf<DiaryEntry?>(null) }
    var editGramsInput by rememberSaveable { mutableStateOf("") }

    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val datePickerState =
        rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    val totalKcal = diaryEntries.sumOf { it.calories }
    val totalProtein = diaryEntries.sumOf { it.protein }.toInt()
    val totalCarbs = diaryEntries.sumOf { it.carbs }.toInt()
    val totalFat = diaryEntries.sumOf { it.fat }.toInt()
    val goalKcal = viewModel.goalKcal.collectAsState().value

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
                    datePickerState.selectedDateMillis?.let {
                        viewModel.setDate(
                            it
                        )
                    }; showDatePicker = false
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
                    totalKcal,
                    goalKcal,
                    totalProtein,
                    totalCarbs,
                    totalFat,
                    cardColor,
                    textColor,
                    grayText,
                    accentBlue,
                    dividerColor
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
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }

        if (entryToEdit != null) {
            Dialog(onDismissRequest = { entryToEdit = null }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardColor)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Menge anpassen",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        entryToEdit?.foodName ?: "",
                        color = grayText,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = editGramsInput,
                        onValueChange = { editGramsInput = it },
                        label = { Text("Menge in Gramm", color = grayText) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = accentBlue,
                            unfocusedBorderColor = grayText.copy(alpha = 0.3f)
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))
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
                        ) {
                            Text("Abbrechen", color = textColor, fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = {
                                val grams =
                                    editGramsInput.toDoubleOrNull(); if (grams != null && entryToEdit != null) {
                                viewModel.updateDiaryEntryGrams(entryToEdit!!, grams)
                            }; entryToEdit = null
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
                        ) {
                            Text("Abbrechen", color = textColor, fontWeight = FontWeight.SemiBold)
                        }
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
                        ) {
                            Text("Löschen", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (showAddSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddSheet = false; viewModel.clearPreview() },
                containerColor = bgColor
            ) {
                AddFoodSheetContent(
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
                    onBarcodeSearch = { viewModel.searchBarcode(it) },
                    onTextSearch = { viewModel.searchFoodByName(it) },
                    onProductSelected = { viewModel.selectProductForPreview(it) },
                    onAdd = { food, grams ->
                        viewModel.addFoodToDiary(
                            food,
                            grams,
                            selectedMeal ?: ""
                        ); showAddSheet = false
                    },
                    onCustomAdd = { name, kcal, p, c, f, grams ->
                        viewModel.addCustomFoodToDiary(
                            name,
                            kcal,
                            p,
                            c,
                            f,
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
                    onCancel = { showAddSheet = false; viewModel.clearPreview() }
                )
            }
        }
    }
}

@Composable
fun DashboardSummary(
    kcal: Int,
    goalKcal: Int,
    protein: Int,
    carbs: Int,
    fat: Int,
    cardColor: Color,
    textColor: Color,
    grayText: Color,
    accentBlue: Color,
    dividerColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardColor)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Gegessen", color = grayText, fontSize = 14.sp); Text(
                "$kcal",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = textColor
            )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Verbleibend",
                    color = grayText,
                    fontSize = 14.sp
                ); Text(
                "${goalKcal - kcal}",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = accentBlue
            )
            }
        }
        Spacer(modifier = Modifier.height(16.dp)); HorizontalDivider(color = dividerColor); Spacer(
        modifier = Modifier.height(16.dp)
    )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MacroRing("Protein", protein, "g", textColor, grayText); MacroRing(
            "Carbs",
            carbs,
            "g",
            textColor,
            grayText
        ); MacroRing("Fett", fat, "g", textColor, grayText)
        }
    }
}

@Composable
fun MacroRing(label: String, value: Int, unit: String, textColor: Color, grayText: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value$unit", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
        Text(label, color = grayText, fontSize = 12.sp)
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                ); Text("$mealKcal kcal", color = grayText, fontSize = 14.sp)
            }
            IconButton(onClick = onAddClick) {
                Icon(
                    Icons.Default.AddCircle,
                    "Hinzufügen",
                    tint = accentBlue,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        if (entries.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
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
        }
    }
}

@Composable
fun AddFoodSheetContent(
    mealName: String,
    previewProduct: FoodItem?,
    searchResults: List<FoodItem>,
    historyFoods: List<FoodItem>,
    recipes: List<Recipe>,
    bgColor: Color,
    cardColor: Color,
    textColor: Color,
    grayText: Color,
    accentBlue: Color,
    dividerColor: Color,
    onBarcodeSearch: (String) -> Unit,
    onTextSearch: (String) -> Unit,
    onProductSelected: (FoodItem) -> Unit,
    onAdd: (FoodItem, Double) -> Unit,
    onCustomAdd: (String, Int, Double, Double, Double, Double) -> Unit,
    onRecipeAdd: (Recipe) -> Unit,
    onCancel: () -> Unit
) {
    var searchInput by rememberSaveable { mutableStateOf("") }
    var gramsInput by rememberSaveable { mutableStateOf("100") }
    var isCustomMode by rememberSaveable { mutableStateOf(false) }
    var isScannerOpen by rememberSaveable { mutableStateOf(false) }
    var customName by rememberSaveable { mutableStateOf("") }
    var customKcal by rememberSaveable { mutableStateOf("") }
    var customProtein by rememberSaveable { mutableStateOf("") }
    var customCarbs by rememberSaveable { mutableStateOf("") }
    var customFat by rememberSaveable { mutableStateOf("") }

    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) isScannerOpen = true
        }

    if (isScannerOpen) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clip(RoundedCornerShape(16.dp))) {
            BarcodeScanner(onBarcodeDetected = { barcode ->
                isScannerOpen = false; onBarcodeSearch(
                barcode
            )
            })
            IconButton(
                onClick = { isScannerOpen = false },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(50))
            ) { Icon(Icons.Default.Close, "Schließen") }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Zu $mealName", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textColor)
            TextButton(onClick = {
                isCustomMode = !isCustomMode
            }) {
                Text(
                    if (isCustomMode) "Zurück zur Suche" else "Eigenes Produkt",
                    color = accentBlue
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (!isCustomMode) {
            OutlinedTextField(
                value = searchInput,
                onValueChange = { searchInput = it },
                placeholder = { Text("Produkt/Zutat", color = grayText) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    IconButton(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Icon(
                            Icons.Default.CameraAlt,
                            "Scannen",
                            tint = accentBlue
                        )
                    }
                },
                trailingIcon = {
                    Button(
                        onClick = {
                            val q = searchInput.trim(); if (q.isNotBlank()) {
                            if (q.all { it.isDigit() }) onBarcodeSearch(q) else onTextSearch(q)
                        }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.padding(end = 4.dp)
                    ) { Text("Suchen", color = Color.White) }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedContainerColor = cardColor,
                    unfocusedContainerColor = cardColor,
                    focusedBorderColor = accentBlue,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (searchInput.isBlank() && searchResults.isEmpty() && previewProduct == null) {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    if (recipes.isNotEmpty()) {
                        item {
                            Text(
                                "Mahlzeiten",
                                fontWeight = FontWeight.Bold,
                                color = grayText,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(recipes) { recipe ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(cardColor, RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        recipe.name,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textColor
                                    )
                                    Text(
                                        "${recipe.totalCalories} kcal",
                                        color = accentBlue,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Button(
                                    onClick = { onRecipeAdd(recipe) },
                                    colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
                                ) { Text("Hinzufügen", color = Color.White) }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                    item {
                        Text(
                            "Zuletzt verwendet",
                            fontWeight = FontWeight.Bold,
                            color = grayText,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(historyFoods.reversed()) { food ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(cardColor, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    food.name,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor
                                ); Text(
                                "${food.calories} kcal pro 100g",
                                color = grayText,
                                fontSize = 12.sp
                            )
                            }
                            Button(
                                onClick = { onProductSelected(food) },
                                colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
                            ) { Text("Wählen", color = Color.White) }
                        }
                    }
                }
            }
            if (searchResults.isNotEmpty() && previewProduct == null) {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(searchResults) { food ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(cardColor, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    food.name,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textColor
                                ); Text(
                                "${food.calories} kcal pro 100g",
                                color = grayText,
                                fontSize = 12.sp
                            )
                            }
                            Button(
                                onClick = { onProductSelected(food) },
                                colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
                            ) { Text("Wählen", color = Color.White) }
                        }
                    }
                }
            }
            if (previewProduct != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardColor)
                        .padding(16.dp)
                ) {
                    Text(
                        previewProduct.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = textColor
                    )
                    Text("${previewProduct.calories} kcal pro 100g", color = grayText)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = gramsInput,
                        onValueChange = { gramsInput = it },
                        label = { Text("Menge in Gramm", color = grayText) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = accentBlue
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val grams = gramsInput.toDoubleOrNull() ?: 100.0; onAdd(
                            previewProduct,
                            grams
                        )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
                    ) {
                        Text(
                            "Hinzufügen",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
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
                    value = customKcal,
                    onValueChange = { customKcal = it },
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
                        value = customProtein,
                        onValueChange = { customProtein = it },
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
                        value = customCarbs,
                        onValueChange = { customCarbs = it },
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
                        value = customFat,
                        onValueChange = { customFat = it },
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
                OutlinedTextField(
                    value = gramsInput,
                    onValueChange = { gramsInput = it },
                    label = { Text("Gegessene Menge (Gramm)", color = grayText) },
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
                Button(
                    onClick = {
                        if (customName.isNotBlank()) onCustomAdd(
                            customName,
                            customKcal.toIntOrNull() ?: 0,
                            customProtein.toDoubleOrNull() ?: 0.0,
                            customCarbs.toDoubleOrNull() ?: 0.0,
                            customFat.toDoubleOrNull() ?: 0.0,
                            gramsInput.toDoubleOrNull() ?: 100.0
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
                ) {
                    Text(
                        "Speichern & Hinzufügen",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}