package com.example.nutrition.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrition.data.FoodRepository
import com.example.nutrition.model.DiaryEntry
import com.example.nutrition.model.FoodItem
import com.example.nutrition.model.Recipe
import com.example.nutrition.model.RecipeIngredient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FoodViewModel(private val repository: FoodRepository) : ViewModel() {
    val analysisEntries: StateFlow<List<DiaryEntry>> = flow {
        val cal = Calendar.getInstance()
        val end = dateFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -30)
        val start = dateFormat.format(cal.time)
        emitAll(repository.getEntriesForRange(start, end))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val _currentDate = MutableStateFlow(dateFormat.format(Date()))
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()
    private val _isDarkMode = MutableStateFlow(repository.getSavedDarkMode())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()
    private val _goalKcal = MutableStateFlow(repository.getSavedGoal())
    val goalKcal: StateFlow<Int> = _goalKcal.asStateFlow()
    private val _scannedProductPreview = MutableStateFlow<FoodItem?>(null)
    val scannedProductPreview: StateFlow<FoodItem?> = _scannedProductPreview.asStateFlow()
    private val _searchResults = MutableStateFlow<List<FoodItem>>(emptyList())
    val searchResults: StateFlow<List<FoodItem>> = _searchResults.asStateFlow()
    private val _tempIngredients = MutableStateFlow<List<RecipeIngredient>>(emptyList())
    val tempIngredients: StateFlow<List<RecipeIngredient>> = _tempIngredients.asStateFlow()
    private val _editingRecipeId = MutableStateFlow<Int?>(null)

    val diaryEntries: StateFlow<List<DiaryEntry>> = _currentDate
        .flatMapLatest { date -> repository.getDiaryEntriesByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFoods: StateFlow<List<FoodItem>> = repository.allFoods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecipes: StateFlow<List<Recipe>> = repository.allRecipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleDarkMode() {
        val newValue = !_isDarkMode.value
        _isDarkMode.value = newValue
        repository.saveDarkMode(newValue)
    }

    fun addRecipeToDiary(recipe: Recipe, mealType: String) {
        viewModelScope.launch {
            val ingredients = repository.getIngredientsForRecipe(recipe.id)

            ingredients.forEach { ingredient ->
                val entry = DiaryEntry(
                    foodId = ingredient.foodId,
                    foodName = ingredient.foodName,
                    calories = ingredient.calories,
                    protein = ingredient.protein,
                    carbs = ingredient.carbs,
                    fat = ingredient.fat,
                    amountInGrams = ingredient.amountInGrams,
                    mealType = mealType,
                    date = _currentDate.value
                )
                repository.insertDiaryEntry(entry)
            }
            clearPreview()
        }
    }

    fun calculateAndSetGoal(
        isMale: Boolean, weightKg: Double, heightCm: Double, ageYears: Int,
        activityLevel: Double, goalOffset: Int
    ) {
        val bmr = if (isMale) {
            (10 * weightKg) + (6.25 * heightCm) - (5 * ageYears) + 5
        } else {
            (10 * weightKg) + (6.25 * heightCm) - (5 * ageYears) - 161
        }
        val tdee = bmr * activityLevel
        var finalKcal = (tdee + goalOffset).toInt()

        if (finalKcal < 1200) finalKcal = 1200

        _goalKcal.value = finalKcal
        repository.saveGoal(finalKcal)
    }

    fun changeDate(daysToAdd: Int) {
        val cal = Calendar.getInstance()
        cal.time = dateFormat.parse(_currentDate.value) ?: Date()
        cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
        _currentDate.value = dateFormat.format(cal.time)
    }

    fun setDate(timeInMillis: Long) {
        _currentDate.value = dateFormat.format(Date(timeInMillis))
    }

    fun searchBarcode(barcode: String) {
        viewModelScope.launch {
            _scannedProductPreview.value = repository.getFoodByBarcode(barcode)
        }
    }

    fun searchFoodByName(query: String) {
        viewModelScope.launch { _searchResults.value = repository.searchFoodByName(query) }
    }

    fun selectProductForPreview(food: FoodItem) {
        _scannedProductPreview.value = food; _searchResults.value = emptyList()
    }

    fun clearPreview() {
        _scannedProductPreview.value = null; _searchResults.value = emptyList()
    }

    fun addIngredientToTempRecipe(food: FoodItem, amountInGrams: Double) {
        val factor = amountInGrams / 100.0
        val ingredient = RecipeIngredient(
            recipeId = 0,
            foodId = food.id,
            foodName = food.name,
            amountInGrams = amountInGrams,
            calories = (food.calories * factor).toInt(),
            protein = food.protein * factor,
            carbs = food.carbs * factor,
            fat = food.fat * factor
        )
        _tempIngredients.value = _tempIngredients.value + ingredient
        clearPreview()
    }

    fun removeIngredientFromTempRecipe(ingredient: RecipeIngredient) {
        _tempIngredients.value = _tempIngredients.value - ingredient
    }

    fun loadRecipeForEditing(recipe: Recipe) {
        viewModelScope.launch {
            _editingRecipeId.value = recipe.id
            _tempIngredients.value = repository.getIngredientsForRecipe(recipe.id)
        }
    }

    fun resetRecipeBuilder() {
        _editingRecipeId.value = null
        _tempIngredients.value = emptyList()
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch { repository.deleteRecipe(recipe) }
    }

    fun saveRecipe(recipeName: String, onDone: () -> Unit) {
        val ingredients = _tempIngredients.value
        if (recipeName.isBlank() || ingredients.isEmpty()) return

        viewModelScope.launch {
            val totalKcal = ingredients.sumOf { it.calories }
            val recipeIdToUse = _editingRecipeId.value ?: 0

            val recipe = Recipe(
                id = recipeIdToUse,
                name = recipeName,
                totalCalories = totalKcal,
                totalProtein = ingredients.sumOf { it.protein },
                totalCarbs = ingredients.sumOf { it.carbs },
                totalFat = ingredients.sumOf { it.fat }
            )
            repository.createRecipeWithIngredients(recipe, ingredients)
            resetRecipeBuilder()
            onDone()
        }
    }

    fun addFoodToDiary(food: FoodItem, amountInGrams: Double, mealType: String) {
        viewModelScope.launch {
            val factor = amountInGrams / 100.0
            val entry = DiaryEntry(
                foodId = food.id,
                foodName = food.name,
                calories = (food.calories * factor).toInt(),
                protein = food.protein * factor,
                carbs = food.carbs * factor,
                fat = food.fat * factor,
                amountInGrams = amountInGrams,
                mealType = mealType,
                date = _currentDate.value
            )
            repository.insertDiaryEntry(entry)
            clearPreview()
        }
    }

    fun addCustomFoodToDiary(
        name: String,
        kcal: Int,
        p: Double,
        c: Double,
        f: Double,
        grams: Double,
        mealType: String
    ) {
        viewModelScope.launch {
            val newFood = FoodItem(
                name = name,
                calories = kcal,
                protein = p,
                carbs = c,
                fat = f,
                isCustom = true
            )
            repository.insertFood(newFood)
            val factor = grams / 100.0
            val entry = DiaryEntry(
                foodId = 0,
                foodName = name,
                calories = (kcal * factor).toInt(),
                protein = p * factor,
                carbs = c * factor,
                fat = f * factor,
                amountInGrams = grams,
                mealType = mealType,
                date = _currentDate.value
            )
            repository.insertDiaryEntry(entry)
        }
    }

    fun updateDiaryEntryGrams(entry: DiaryEntry, newGrams: Double) {
        viewModelScope.launch {
            val ratio = newGrams / entry.amountInGrams
            val updatedEntry = entry.copy(
                amountInGrams = newGrams,
                calories = (entry.calories * ratio).toInt(),
                protein = entry.protein * ratio,
                carbs = entry.carbs * ratio,
                fat = entry.fat * ratio
            )
            repository.insertDiaryEntry(updatedEntry)
        }
    }

    fun updateRecipeIngredientGrams(ingredient: RecipeIngredient, newGrams: Double) {
        val ratio = newGrams / ingredient.amountInGrams
        val updatedIngredient = ingredient.copy(
            amountInGrams = newGrams,
            calories = (ingredient.calories * ratio).toInt(),
            protein = ingredient.protein * ratio,
            carbs = ingredient.carbs * ratio,
            fat = ingredient.fat * ratio
        )

        _tempIngredients.value = _tempIngredients.value.map {
            if (it == ingredient) updatedIngredient else it
        }
    }

    fun deleteDiaryEntry(entry: DiaryEntry) {
        viewModelScope.launch { repository.deleteDiaryEntry(entry) }
    }
}