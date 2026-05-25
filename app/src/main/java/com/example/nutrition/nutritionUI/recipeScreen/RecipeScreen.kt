package com.example.nutrition.nutritionUI.recipeScreen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.nutrition.model.FoodItem
import com.example.nutrition.model.Recipe
import com.example.nutrition.model.RecipeIngredient
import com.example.nutrition.nutritionUI.foodUI.AddFoodSheetContent
import com.example.nutrition.nutritionUI.foodViewModel.FoodViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(viewModel: FoodViewModel) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val df = remember { DecimalFormat("#.#") }
    val recipeList by viewModel.allRecipes.collectAsState()
    val tempIngredients by viewModel.tempIngredients.collectAsState()
    val previewProduct by viewModel.scannedProductPreview.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val historyFoods by viewModel.allFoods.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()
    val bgColor = if (isDark) Color(0xFF000000) else Color(0xFFF2F2F7)
    val cardColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFFFFFFF)
    val textColor = if (isDark) Color.White else Color.Black
    val dividerColor = if (isDark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
    val grayText = if (isDark) Color(0xFFAEAEB2) else Color(0xFF8E8E93)
    val accentBlue = if (isDark) Color(0xFF0A84FF) else Color(0xFF007AFF)
    var isCreatingRecipe by rememberSaveable { mutableStateOf(false) }
    var isEditingExistingRecipe by rememberSaveable { mutableStateOf(false) }
    var recipeNameInput by rememberSaveable { mutableStateOf("") }
    var showIngredientSearchSheet by rememberSaveable { mutableStateOf(false) }
    var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }
    var ingredientToDelete by remember { mutableStateOf<RecipeIngredient?>(null) }
    var ingredientToEdit by remember { mutableStateOf<RecipeIngredient?>(null) }
    var showImportSuccess by remember { mutableStateOf(false) }
    var recipeSearchQuery by rememberSaveable { mutableStateOf("") }

    val importLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                try {
                    val jsonStr = context.contentResolver.openInputStream(it)?.bufferedReader()
                        ?.use { reader -> reader.readText() }
                    if (jsonStr != null) {
                        val root = JSONObject(jsonStr)
                        val name = root.optString("recipeName", "Importierte Mahlzeit")
                        val ingredients = root.optJSONArray("ingredients")

                        viewModel.resetRecipeBuilder()
                        if (ingredients != null) {
                            for (i in 0 until ingredients.length()) {
                                val obj = ingredients.getJSONObject(i)
                                viewModel.addCustomIngredientToTempRecipe(
                                    obj.optString("name", "Produkt"),
                                    obj.optInt("calories", 0),
                                    obj.optDouble("protein", 0.0),
                                    obj.optDouble("carbs", 0.0),
                                    obj.optDouble("fat", 0.0),
                                    obj.optDouble("fiber", 0.0),
                                    obj.optDouble("sugar", 0.0),
                                    obj.optDouble("amountInGrams", 100.0)
                                )
                            }
                        }
                        viewModel.saveRecipe(name) {
                            showImportSuccess = true
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
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

    if (showImportSuccess) {
        AlertDialog(
            onDismissRequest = { showImportSuccess = false },
            containerColor = cardColor,
            title = { Text("Import erfolgreich", color = textColor, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Die Mahlzeit wurde erfolgreich zu deinen gespeicherten Mahlzeiten hinzugefügt.",
                    color = grayText
                )
            },
            confirmButton = {
                TextButton(onClick = { showImportSuccess = false }) {
                    Text(
                        "Super",
                        color = accentBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    if (recipeToDelete != null) {
        Dialog(onDismissRequest = { recipeToDelete = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardColor)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Mahlzeit löschen",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Möchtest du die Mahlzeit '${recipeToDelete?.name}' wirklich löschen?",
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
                        onClick = { recipeToDelete = null },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = grayText.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Abbrechen", color = textColor, fontWeight = FontWeight.SemiBold) }
                    Button(
                        onClick = {
                            recipeToDelete?.let { viewModel.deleteRecipe(it) }; recipeToDelete =
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

    if (ingredientToDelete != null) {
        Dialog(onDismissRequest = { ingredientToDelete = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardColor)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Produkt entfernen",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = textColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Möchtest du '${ingredientToDelete?.foodName}' wirklich aus dieser Mahlzeit löschen?",
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
                        onClick = { ingredientToDelete = null },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = grayText.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Abbrechen", color = textColor, fontWeight = FontWeight.SemiBold) }
                    Button(
                        onClick = {
                            ingredientToDelete?.let {
                                viewModel.removeIngredientFromTempRecipe(
                                    it
                                )
                            }; ingredientToDelete = null
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A)),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Entfernen", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }

    if (ingredientToEdit != null) {
        Dialog(onDismissRequest = { ingredientToEdit = null }) {
            val dialogFocusManager = LocalFocusManager.current
            val currentFactor = (ingredientToEdit?.amountInGrams ?: 100.0) / 100.0
            var editIngName by remember(ingredientToEdit) {
                mutableStateOf(ingredientToEdit?.foodName ?: "")
            }
            var editIngKcal by remember(ingredientToEdit) {
                mutableStateOf(
                    if (currentFactor > 0) (ingredientToEdit!!.calories / currentFactor).toInt()
                        .toString() else "0"
                )
            }
            var editIngProtein by remember(ingredientToEdit) {
                mutableStateOf(
                    if (currentFactor > 0) df.format(ingredientToEdit!!.protein / currentFactor)
                        .replace(",", ".") else "0"
                )
            }
            var editIngCarbs by remember(ingredientToEdit) {
                mutableStateOf(
                    if (currentFactor > 0) df.format(ingredientToEdit!!.carbs / currentFactor)
                        .replace(",", ".") else "0"
                )
            }
            var editIngFat by remember(ingredientToEdit) {
                mutableStateOf(
                    if (currentFactor > 0) df.format(ingredientToEdit!!.fat / currentFactor)
                        .replace(",", ".") else "0"
                )
            }
            var editIngFiber by remember(ingredientToEdit) {
                mutableStateOf(
                    if (currentFactor > 0) df.format(ingredientToEdit!!.fiber / currentFactor)
                        .replace(",", ".") else "0"
                )
            }
            var editIngSugar by remember(ingredientToEdit) {
                mutableStateOf(
                    if (currentFactor > 0) df.format(ingredientToEdit!!.sugar / currentFactor)
                        .replace(",", ".") else "0"
                )
            }
            var editIngGrams by remember(ingredientToEdit) {
                mutableStateOf(
                    ingredientToEdit?.amountInGrams?.let {
                        if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
                    } ?: "100"
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
                Text(
                    "Produkt korrigieren",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = textColor
                )

                OutlinedTextField(
                    value = editIngName,
                    onValueChange = { editIngName = it },
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
                    value = editIngKcal,
                    onValueChange = { editIngKcal = it },
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
                        value = editIngProtein,
                        onValueChange = { editIngProtein = it },
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
                        value = editIngCarbs,
                        onValueChange = { editIngCarbs = it },
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
                        value = editIngFat,
                        onValueChange = { editIngFat = it },
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
                        value = editIngFiber,
                        onValueChange = { editIngFiber = it },
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
                        value = editIngSugar,
                        onValueChange = { editIngSugar = it },
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
                    value = editIngGrams,
                    onValueChange = { editIngGrams = it },
                    label = { Text("Menge in Gramm", color = grayText) },
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
                        onClick = { ingredientToEdit = null },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = grayText.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Abbrechen", color = textColor, fontWeight = FontWeight.SemiBold) }
                    Button(
                        onClick = {
                            val grams = editIngGrams.toDoubleOrNull() ?: 100.0
                            val f = grams / 100.0
                            val updated = ingredientToEdit!!.copy(
                                foodName = editIngName,
                                amountInGrams = grams,
                                calories = ((editIngKcal.toIntOrNull() ?: 0) * f).toInt(),
                                protein = (editIngProtein.toDoubleOrNull() ?: 0.0) * f,
                                carbs = (editIngCarbs.toDoubleOrNull() ?: 0.0) * f,
                                fat = (editIngFat.toDoubleOrNull() ?: 0.0) * f,
                                fiber = (editIngFiber.toDoubleOrNull() ?: 0.0) * f,
                                sugar = (editIngSugar.toDoubleOrNull() ?: 0.0) * f
                            )
                            viewModel.updateRecipeIngredient(ingredientToEdit!!, updated)
                            ingredientToEdit = null
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

    if (!isCreatingRecipe) {
        val filteredRecipes = remember(recipeSearchQuery, recipeList) {
            if (recipeSearchQuery.isBlank()) recipeList
            else recipeList.filter { it.name.contains(recipeSearchQuery, ignoreCase = true) }
        }

        Scaffold(
            containerColor = bgColor,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Mahlzeiten",
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = textColor
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            importLauncher.launch(
                                arrayOf(
                                    "application/json",
                                    "*/*"
                                )
                            )
                        }) {
                            Icon(
                                Icons.Default.Download,
                                "Mahlzeit importieren",
                                tint = accentBlue,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        IconButton(onClick = {
                            viewModel.resetRecipeBuilder(); isCreatingRecipe =
                            true; isEditingExistingRecipe = false
                        }) {
                            Icon(
                                Icons.Default.AddCircle,
                                "Mahlzeit erstellen",
                                tint = accentBlue,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {

                OutlinedTextField(
                    value = recipeSearchQuery,
                    onValueChange = { recipeSearchQuery = it },
                    placeholder = { Text("Mahlzeit suchen...", color = grayText) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Suchen",
                            tint = grayText
                        )
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor, unfocusedTextColor = textColor,
                        focusedBorderColor = accentBlue, unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = cardColor, unfocusedContainerColor = cardColor
                    )
                )

                if (filteredRecipes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            if (recipeSearchQuery.isBlank()) "Noch keine Mahlzeiten erstellt." else "Keine Mahlzeit gefunden.",
                            color = grayText
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredRecipes) { recipe ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.loadRecipeForEditing(recipe); recipeNameInput =
                                        recipe.name; isCreatingRecipe =
                                        true; isEditingExistingRecipe =
                                        true
                                    },
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = cardColor)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            recipe.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = textColor
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                "${recipe.totalCalories} kcal",
                                                fontWeight = FontWeight.Bold,
                                                color = accentBlue,
                                                fontSize = 16.sp
                                            )
                                            IconButton(
                                                onClick = { recipeToDelete = recipe },
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
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "P: ${df.format(recipe.totalProtein)}g | C: ${
                                            df.format(
                                                recipe.totalCarbs
                                            )
                                        }g | F: ${df.format(recipe.totalFat)}g",
                                        color = grayText,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    } else {
        Scaffold(
            containerColor = bgColor,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Mahlzeiten",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = textColor
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isCreatingRecipe = false; isEditingExistingRecipe =
                            false; recipeNameInput = ""; viewModel.resetRecipeBuilder()
                        }) { Icon(Icons.Default.ArrowBack, "Zurück", tint = accentBlue) }
                    },
                    actions = {
                        if (isEditingExistingRecipe) {
                            IconButton(onClick = {
                                if (tempIngredients.isNotEmpty()) {
                                    val root = JSONObject()
                                    val name = recipeNameInput.ifBlank { "Mahlzeit" }
                                    root.put("recipeName", name)
                                    val arr = JSONArray()
                                    tempIngredients.forEach { e ->
                                        val obj = JSONObject()
                                        obj.put("name", e.foodName)
                                        obj.put("amountInGrams", e.amountInGrams)
                                        obj.put("calories", e.calories)
                                        obj.put("protein", e.protein)
                                        obj.put("carbs", e.carbs)
                                        obj.put("fat", e.fat)
                                        obj.put("fiber", e.fiber)
                                        obj.put("sugar", e.sugar)
                                        arr.put(obj)
                                    }
                                    root.put("ingredients", arr)
                                    jsonToExport = root.toString(4)
                                    exportLauncher.launch("${name.replace(" ", "_")}.json")
                                }
                            }) {
                                Icon(Icons.Default.Share, "Exportieren", tint = accentBlue)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = recipeNameInput,
                    onValueChange = { recipeNameInput = it },
                    placeholder = { Text("Name", color = grayText) },
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardColor, RoundedCornerShape(14.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = accentBlue,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Produkte",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = textColor
                    )
                    Button(
                        onClick = { showIngredientSearchSheet = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp)); Spacer(
                        modifier = Modifier.width(6.dp)
                    ); Text("Produkt")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardColor)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (tempIngredients.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) { Text("Noch keine Produkte hinzugefügt.", color = grayText) }
                        }
                    } else {
                        items(tempIngredients) { ingredient ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { ingredientToEdit = ingredient }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        ingredient.foodName,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textColor
                                    )
                                    val formattedGrams =
                                        if (ingredient.amountInGrams % 1.0 == 0.0) ingredient.amountInGrams.toInt()
                                            .toString() else ingredient.amountInGrams.toString()
                                    Text(
                                        "${formattedGrams}g • ${ingredient.calories} kcal",
                                        color = grayText,
                                        fontSize = 12.sp
                                    )
                                }
                                IconButton(onClick = { ingredientToDelete = ingredient }) {
                                    Icon(
                                        Icons.Default.Close,
                                        "Entfernen",
                                        tint = grayText.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            HorizontalDivider(color = dividerColor)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.saveRecipe(recipeNameInput) {
                            isCreatingRecipe = false; isEditingExistingRecipe =
                            false; recipeNameInput = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .padding(bottom = 16.dp),
                    enabled = recipeNameInput.isNotBlank() && tempIngredients.isNotEmpty(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentBlue,
                        disabledContainerColor = grayText.copy(alpha = 0.3f)
                    )
                ) { Text("Mahlzeit speichern", fontSize = 18.sp, fontWeight = FontWeight.Bold) }

                if (showIngredientSearchSheet) {
                    val searchSheetState =
                        rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ModalBottomSheet(onDismissRequest = {
                        showIngredientSearchSheet =
                            false; viewModel.clearPreview(); viewModel.clearBarcodeError()
                    }, sheetState = searchSheetState, containerColor = bgColor) {
                        AddFoodSheetContent(
                            viewModel = viewModel,
                            mealName = "Mahlzeit",
                            previewProduct = previewProduct,
                            searchResults = searchResults,
                            historyFoods = historyFoods,
                            recipes = emptyList(),
                            bgColor = bgColor,
                            cardColor = cardColor,
                            textColor = textColor,
                            grayText = grayText,
                            accentBlue = accentBlue,
                            dividerColor = dividerColor,
                            onTextSearch = { viewModel.searchFoodByName(it) },
                            onProductSelected = { viewModel.selectProductForPreview(it) },
                            onAdd = { food, grams ->
                                viewModel.addIngredientToTempRecipe(
                                    food,
                                    grams
                                ); showIngredientSearchSheet = false
                            },
                            onCustomAdd = { name, kcal, p, c, f, fiber, sugar, grams ->
                                viewModel.addCustomIngredientToTempRecipe(
                                    name,
                                    kcal,
                                    p,
                                    c,
                                    f,
                                    fiber,
                                    sugar,
                                    grams
                                ); showIngredientSearchSheet = false
                            },
                            onRecipeAdd = { _ -> },
                            onExportFood = onExportFood
                        )
                    }
                }
            }
        }
    }
}