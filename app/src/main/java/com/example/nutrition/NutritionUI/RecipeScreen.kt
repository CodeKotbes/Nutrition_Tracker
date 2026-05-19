package com.example.nutrition.ui

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
import com.example.nutrition.model.Recipe
import com.example.nutrition.model.RecipeIngredient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScreen(viewModel: FoodViewModel) {
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
    var recipeNameInput by rememberSaveable { mutableStateOf("") }
    var showIngredientSearchSheet by rememberSaveable { mutableStateOf(false) }

    var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }
    var ingredientToDelete by remember { mutableStateOf<RecipeIngredient?>(null) }
    var ingredientToEdit by remember { mutableStateOf<RecipeIngredient?>(null) }
    var editIngredientGramsInput by rememberSaveable { mutableStateOf("") }

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
                    ) {
                        Text("Abbrechen", color = textColor, fontWeight = FontWeight.SemiBold)
                    }
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
                    ) {
                        Text("Löschen", color = Color.White, fontWeight = FontWeight.Bold)
                    }
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
                    "Zutat entfernen",
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
                    ) {
                        Text("Abbrechen", color = textColor, fontWeight = FontWeight.SemiBold)
                    }
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
                    ) {
                        Text("Entfernen", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (ingredientToEdit != null) {
        Dialog(onDismissRequest = { ingredientToEdit = null }) {
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
                    ingredientToEdit?.foodName ?: "",
                    color = grayText,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = editIngredientGramsInput,
                    onValueChange = { editIngredientGramsInput = it },
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
                        onClick = { ingredientToEdit = null },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = grayText.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Abbrechen", color = textColor, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            val grams =
                                editIngredientGramsInput.toDoubleOrNull(); if (grams != null && ingredientToEdit != null) {
                            viewModel.updateRecipeIngredientGrams(ingredientToEdit!!, grams)
                        }; ingredientToEdit = null
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
                            viewModel.resetRecipeBuilder(); isCreatingRecipe = true
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
            if (recipeList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) { Text("Noch keine Mahlzeiten erstellt.", color = grayText) }
            } else {
                LazyColumn(
                    contentPadding = paddingValues,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recipeList) { recipe ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.loadRecipeForEditing(recipe); recipeNameInput =
                                    recipe.name; isCreatingRecipe = true
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
                                                tint = Color.Red.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "P: ${recipe.totalProtein.toInt()}g | C: ${recipe.totalCarbs.toInt()}g | F: ${recipe.totalFat.toInt()}g",
                                    color = grayText,
                                    fontSize = 13.sp
                                )
                            }
                        }
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
                            isCreatingRecipe = false; recipeNameInput =
                            ""; viewModel.resetRecipeBuilder()
                        }) { Icon(Icons.Default.ArrowBack, "Zurück", tint = accentBlue) }
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
                        "Zutaten",
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
                    ); Text("Zutat")
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
                            ) { Text("Noch keine Zutaten hinzugefügt.", color = grayText) }
                        }
                    } else {
                        items(tempIngredients) { ingredient ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        ingredientToEdit = ingredient; editIngredientGramsInput =
                                        ingredient.amountInGrams.toInt().toString()
                                    }
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
                                    Text(
                                        "${ingredient.amountInGrams.toInt()}g • ${ingredient.calories} kcal",
                                        color = grayText,
                                        fontSize = 12.sp
                                    )
                                }
                                IconButton(onClick = { ingredientToDelete = ingredient }) {
                                    Icon(
                                        Icons.Default.Close,
                                        "Entfernen",
                                        tint = grayText
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
                            isCreatingRecipe = false; recipeNameInput = ""
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
            }

            if (showIngredientSearchSheet) {
                ModalBottomSheet(onDismissRequest = {
                    showIngredientSearchSheet = false; viewModel.clearPreview()
                }, containerColor = bgColor) {
                    AddFoodSheetContent(
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
                        onBarcodeSearch = { viewModel.searchBarcode(it) },
                        onTextSearch = { viewModel.searchFoodByName(it) },
                        onProductSelected = { viewModel.selectProductForPreview(it) },
                        onAdd = { food, grams ->
                            viewModel.addIngredientToTempRecipe(
                                food,
                                grams
                            ); showIngredientSearchSheet = false
                        },
                        onCustomAdd = { _, _, _, _, _, _ -> },
                        onRecipeAdd = { _ -> },
                        onCancel = { showIngredientSearchSheet = false; viewModel.clearPreview() }
                    )
                }
            }
        }
    }
}