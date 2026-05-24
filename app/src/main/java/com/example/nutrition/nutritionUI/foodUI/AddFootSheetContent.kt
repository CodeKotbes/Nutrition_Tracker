package com.example.nutrition.nutritionUI.foodUI

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.nutrition.model.FoodItem
import com.example.nutrition.model.Recipe
import com.example.nutrition.nutritionUI.barcodeScanner.BarcodeScanner
import com.example.nutrition.nutritionUI.foodViewModel.FoodViewModel
import org.json.JSONObject

@Composable
fun AddFoodSheetContent(
    viewModel: FoodViewModel,
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
    onTextSearch: (String) -> Unit,
    onProductSelected: (FoodItem) -> Unit,
    onAdd: (FoodItem, Double) -> Unit,
    onCustomAdd: (String, Int, Double, Double, Double, Double, Double, Double) -> Unit,
    onRecipeAdd: (Recipe) -> Unit,
    onExportFood: (FoodItem) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var searchInput by rememberSaveable { mutableStateOf("") }
    var gramsInput by rememberSaveable { mutableStateOf("100") }
    var isCustomMode by rememberSaveable { mutableStateOf(false) }
    var isScannerOpen by rememberSaveable { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editKcal by remember { mutableStateOf("") }
    var editProtein by remember { mutableStateOf("") }
    var editCarbs by remember { mutableStateOf("") }
    var editFat by remember { mutableStateOf("") }
    var editFiber by remember { mutableStateOf("") }
    var editSugar by remember { mutableStateOf("") }
    var customName by rememberSaveable { mutableStateOf("") }
    var customKcal by rememberSaveable { mutableStateOf("") }
    var customProtein by rememberSaveable { mutableStateOf("") }
    var customCarbs by rememberSaveable { mutableStateOf("") }
    var customFat by rememberSaveable { mutableStateOf("") }
    var customFiber by rememberSaveable { mutableStateOf("") }
    var customSugar by rememberSaveable { mutableStateOf("") }
    val importLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                try {
                    val jsonStr = context.contentResolver.openInputStream(it)?.bufferedReader()
                        ?.use { reader -> reader.readText() }
                    if (jsonStr != null) {
                        val root = JSONObject(jsonStr)
                        if (root.optString("type") == "FoodItem") {
                            customName = root.optString("name", "")
                            customKcal = root.optInt("calories", 0).toString()
                            customProtein = root.optDouble("protein", 0.0).toString()
                            customCarbs = root.optDouble("carbs", 0.0).toString()
                            customFat = root.optDouble("fat", 0.0).toString()
                            customFiber = root.optDouble("fiber", 0.0).toString()
                            customSugar = root.optDouble("sugar", 0.0).toString()
                            isCustomMode = true
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    LaunchedEffect(previewProduct) {
        previewProduct?.let {
            editName = it.name; editKcal = it.calories.toString(); editProtein =
            it.protein.toInt().toString(); editCarbs = it.carbs.toInt().toString()
            editFat = it.fat.toInt().toString(); editFiber =
            it.fiber.toInt().toString(); editSugar = it.sugar.toInt().toString()
        }
    }

    val barcodeError by viewModel.barcodeError.collectAsState()
    val isBarcodeLoading by viewModel.isBarcodeLoading.collectAsState()
    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) isScannerOpen = true
        }

    val filteredHistory = remember(searchInput, historyFoods) {
        if (searchInput.isBlank()) historyFoods.reversed()
        else historyFoods.reversed().filter { it.name.contains(searchInput, ignoreCase = true) }
    }

    val filteredRecipes = remember(searchInput, recipes) {
        if (searchInput.isBlank()) recipes
        else recipes.filter { it.name.contains(searchInput, ignoreCase = true) }
    }

    if (isScannerOpen) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            if (!isBarcodeLoading && barcodeError == null) {
                BarcodeScanner(onBarcodeDetected = { barcode ->
                    viewModel.searchBarcode(barcode) {
                        isScannerOpen = false
                    }
                })
            }
            if (isBarcodeLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = accentBlue) }
            }
            barcodeError?.let { error ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.85f))
                        .padding(24.dp), contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            null,
                            tint = Color(0xFFFF453A),
                            modifier = Modifier.size(48.dp)
                        ); Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            error,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ); Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.clearBarcodeError() },
                            colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                            shape = RoundedCornerShape(10.dp)
                        ) { Text("Weiter scannen", fontWeight = FontWeight.Bold) }
                    }
                }
            }
            IconButton(
                onClick = { isScannerOpen = false; viewModel.clearBarcodeError() },
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
            Text("$mealName", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = textColor)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    importLauncher.launch(
                        arrayOf(
                            "application/json",
                            "*/*"
                        )
                    )
                }) {
                    Icon(Icons.Default.Download, "Importieren", tint = accentBlue)
                }
                TextButton(onClick = { isCustomMode = !isCustomMode }) {
                    Text(
                        if (isCustomMode) "Zurück zur Suche" else "Eigenes Produkt",
                        color = accentBlue
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (!isCustomMode) {
            OutlinedTextField(
                value = searchInput,
                onValueChange = { searchInput = it },
                placeholder = { Text("Produkt suchen...", color = grayText) },
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    IconButton(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Icon(Icons.Default.CameraAlt, "Scannen", tint = accentBlue)
                    }
                },
                trailingIcon = {
                    Button(
                        onClick = {
                            val q = searchInput.trim(); if (q.isNotBlank()) {
                            if (q.all { it.isDigit() }) viewModel.searchBarcode(q) {} else onTextSearch(
                                q
                            )
                            focusManager.clearFocus()
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

            if (previewProduct == null) {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {

                    if (filteredRecipes.isNotEmpty()) {
                        item {
                            Text(
                                "Meine Mahlzeiten",
                                fontWeight = FontWeight.Bold,
                                color = grayText,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(filteredRecipes) { recipe ->
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

                    if (filteredHistory.isNotEmpty()) {
                        item {
                            Text(
                                "Zuletzt verwendet",
                                fontWeight = FontWeight.Bold,
                                color = grayText,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(filteredHistory) { food ->
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
                                    )
                                    Text(
                                        "${food.calories} kcal • P: ${food.protein.toInt()}g | C: ${food.carbs.toInt()}g | F: ${food.fat.toInt()}g",
                                        color = grayText,
                                        fontSize = 11.sp
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { onExportFood(food) }) {
                                        Icon(
                                            Icons.Default.Share,
                                            "Teilen",
                                            tint = accentBlue
                                        )
                                    }
                                    Button(
                                        onClick = { onProductSelected(food) },
                                        colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
                                    ) { Text("Wählen", color = Color.White) }
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }

                    if (searchResults.isNotEmpty()) {
                        item {
                            Text(
                                "Suchergebnisse",
                                fontWeight = FontWeight.Bold,
                                color = grayText,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
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
                                    )
                                    Text(
                                        "${food.calories} kcal • P: ${food.protein.toInt()}g | C: ${food.carbs.toInt()}g | F: ${food.fat.toInt()}g",
                                        color = grayText,
                                        fontSize = 11.sp
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { onExportFood(food) }) {
                                        Icon(
                                            Icons.Default.Share,
                                            "Teilen",
                                            tint = accentBlue
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

                    if (filteredRecipes.isEmpty() && filteredHistory.isEmpty() && searchResults.isEmpty()) {
                        item {
                            Text(
                                if (searchInput.isBlank()) "Keine Einträge vorhanden." else "Keine lokalen Treffer. Tippe auf 'Suchen' für Online-Ergebnisse.",
                                color = grayText,
                                modifier = Modifier.padding(8.dp)
                            )
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
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Produkt korrigieren",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = accentBlue
                    )
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name", color = grayText) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(FocusDirection.Next)
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
                            focusManager.moveFocus(FocusDirection.Next)
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
                                focusManager.moveFocus(FocusDirection.Next)
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
                                focusManager.moveFocus(FocusDirection.Next)
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
                                focusManager.moveFocus(FocusDirection.Next)
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
                                focusManager.moveFocus(FocusDirection.Next)
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
                                focusManager.moveFocus(FocusDirection.Next)
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
                        value = gramsInput,
                        onValueChange = { gramsInput = it },
                        label = { Text("Menge in Gramm", color = grayText) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = accentBlue
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            val grams = gramsInput.toDoubleOrNull() ?: 100.0
                            val correctedProduct = FoodItem(
                                id = previewProduct.id,
                                name = editName,
                                calories = editKcal.toIntOrNull() ?: 0,
                                protein = editProtein.toDoubleOrNull() ?: 0.0,
                                carbs = editCarbs.toDoubleOrNull() ?: 0.0,
                                fat = editFat.toDoubleOrNull() ?: 0.0,
                                fiber = editFiber.toDoubleOrNull() ?: 0.0,
                                sugar = editSugar.toDoubleOrNull() ?: 0.0,
                                barcode = previewProduct.barcode,
                                isCustom = previewProduct.isCustom
                            )
                            onAdd(correctedProduct, grams)
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
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Name", color = grayText) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(FocusDirection.Next)
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
                    value = customKcal,
                    onValueChange = { customKcal = it },
                    label = { Text("Kalorien (pro 100g)", color = grayText) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(FocusDirection.Next)
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
                        value = customProtein,
                        onValueChange = { customProtein = it },
                        label = { Text("Protein", color = grayText) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(FocusDirection.Next)
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
                        value = customCarbs,
                        onValueChange = { customCarbs = it },
                        label = { Text("Carbs", color = grayText) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(FocusDirection.Next)
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
                        value = customFat,
                        onValueChange = { customFat = it },
                        label = { Text("Fett", color = grayText) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(FocusDirection.Next)
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
                        value = customFiber,
                        onValueChange = { customFiber = it },
                        label = { Text("Ballastst.", color = grayText) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(FocusDirection.Next)
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
                        value = customSugar,
                        onValueChange = { customSugar = it },
                        label = { Text("Zucker", color = grayText) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(FocusDirection.Next)
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
                OutlinedTextField(
                    value = gramsInput,
                    onValueChange = { gramsInput = it },
                    label = { Text("Gegessene Menge (in g)", color = grayText) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
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
                            customFiber.toDoubleOrNull() ?: 0.0,
                            customSugar.toDoubleOrNull() ?: 0.0,
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

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}