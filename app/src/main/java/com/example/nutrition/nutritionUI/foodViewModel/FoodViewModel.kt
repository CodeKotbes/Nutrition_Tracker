package com.example.nutrition.nutritionUI.foodViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrition.data.FoodRepository
import com.example.nutrition.model.DiaryEntry
import com.example.nutrition.model.FoodItem
import com.example.nutrition.model.Recipe
import com.example.nutrition.model.RecipeIngredient
import com.example.nutrition.model.WaterRecord
import com.example.nutrition.model.WeightEntry
import com.example.nutrition.nutritionUI.goalsScreen.HealthConnectManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FoodViewModel(private val repository: FoodRepository, val healthConnectManager: HealthConnectManager) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _currentDate = MutableStateFlow(dateFormat.format(Date()))
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    private val _isDarkMode = MutableStateFlow(repository.getSavedDarkMode())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _goalKcal = MutableStateFlow(repository.getSavedGoal())
    val goalKcal: StateFlow<Int> = _goalKcal.asStateFlow()

    private val _goalProtein = MutableStateFlow(repository.getSavedProteinGoal())
    val goalProtein: StateFlow<Int> = _goalProtein.asStateFlow()

    private val _goalCarbs = MutableStateFlow(repository.getSavedCarbsGoal())
    val goalCarbs: StateFlow<Int> = _goalCarbs.asStateFlow()

    private val _goalFat = MutableStateFlow(repository.getSavedFatGoal())
    val goalFat: StateFlow<Int> = _goalFat.asStateFlow()

    private val _goalFiber = MutableStateFlow(repository.getSavedFiberGoal())
    val goalFiber: StateFlow<Int> = _goalFiber.asStateFlow()

    private val _goalSugar = MutableStateFlow(repository.getSavedSugarGoal())
    val goalSugar: StateFlow<Int> = _goalSugar.asStateFlow()

    private val _waterGoal = MutableStateFlow(2000)
    val waterGoal: StateFlow<Int> = _waterGoal.asStateFlow()

    private val _scannedProductPreview = MutableStateFlow<FoodItem?>(null)
    val scannedProductPreview: StateFlow<FoodItem?> = _scannedProductPreview.asStateFlow()

    private val _searchResults = MutableStateFlow<List<FoodItem>>(emptyList())
    val searchResults: StateFlow<List<FoodItem>> = _searchResults.asStateFlow()

    private val _tempIngredients = MutableStateFlow<List<RecipeIngredient>>(emptyList())
    val tempIngredients: StateFlow<List<RecipeIngredient>> = _tempIngredients.asStateFlow()

    private val _editingRecipeId = MutableStateFlow<Int?>(null)

    private val _currentSteps = MutableStateFlow(0)
    val currentSteps: StateFlow<Int> = _currentSteps.asStateFlow()

    private val _activityKcal = MutableStateFlow(0)
    val activityKcal: StateFlow<Int> = _activityKcal.asStateFlow()

    val diaryEntries: StateFlow<List<DiaryEntry>> = _currentDate
        .flatMapLatest { date -> repository.getDiaryEntriesByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val analysisEntries: StateFlow<List<DiaryEntry>> = flow {
        val cal = Calendar.getInstance()
        val end = dateFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -30)
        val start = dateFormat.format(cal.time)
        emitAll(repository.getEntriesForRange(start, end))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFoods: StateFlow<List<FoodItem>> = repository.allFoods
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecipes: StateFlow<List<Recipe>> = repository.allRecipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val waterRecords: StateFlow<List<WaterRecord>> = _currentDate
        .flatMapLatest { date -> repository.getWaterRecordsByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWaterRecords: StateFlow<List<WaterRecord>> = repository.allWaterRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weightHistory: StateFlow<List<WeightEntry>> = repository.allWeights
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            _currentDate.collect {
                syncHealthData()
            }
        }
    }

    fun syncHealthData() {
        viewModelScope.launch {
            if (healthConnectManager.isAvailable && healthConnectManager.hasAllPermissions()) {
                val stats = healthConnectManager.getTodayHealthStats()
                _currentSteps.value = stats.first
                _activityKcal.value = stats.second
            } else {
                _currentSteps.value = 0
                _activityKcal.value = 0
            }
        }
    }

    fun getSavedAge() = repository.getSavedAge()
    fun getSavedHeight() = repository.getSavedHeight()
    fun getSavedTargetWeight() = repository.getSavedTargetWeight()
    fun getSavedIsMale() = repository.getSavedIsMale()
    fun getSavedActivityLevel() = repository.getSavedActivityLevel()
    fun getSavedGoalOffset() = repository.getSavedGoalOffset()

    fun calculateAndSetGoal(
        isMale: Boolean, weightKg: Double, heightCm: Double, ageYears: Int,
        activityLevel: Double, goalOffset: Int, targetWeightStr: String
    ) {
        val bmr = if (isMale) {
            (10 * weightKg) + (6.25 * heightCm) - (5 * ageYears) + 5
        } else {
            (10 * weightKg) + (6.25 * heightCm) - (5 * ageYears) - 161
        }
        val tdee = bmr * activityLevel
        var finalKcal = (tdee + goalOffset).toInt()

        if (finalKcal < 1200) finalKcal = 1200

        val protein = (finalKcal * 0.30 / 4).toInt()
        val carbs = (finalKcal * 0.40 / 4).toInt()
        val fat = (finalKcal * 0.30 / 9).toInt()
        val fiber = 30
        val sugar = 50

        repository.saveCalculatorInputs(
            age = ageYears.toString(),
            height = heightCm.toString(),
            targetWeight = targetWeightStr,
            isMale = isMale,
            activityLevel = activityLevel,
            goalOffset = goalOffset
        )

        updateAllGoals(finalKcal, protein, carbs, fat, fiber, sugar)
    }

    fun addWeightEntry(weight: Double) {
        viewModelScope.launch {
            val entry = WeightEntry(
                weight = weight,
                date = _currentDate.value,
                timestamp = System.currentTimeMillis()
            )
            repository.insertWeight(entry)
        }
    }

    fun toggleDarkMode() {
        val newValue = !_isDarkMode.value
        _isDarkMode.value = newValue
        repository.saveDarkMode(newValue)
    }

    fun updateAllGoals(kcal: Int, protein: Int, carbs: Int, fat: Int, fiber: Int, sugar: Int) {
        repository.saveAllGoals(kcal, protein, carbs, fat, fiber, sugar)
        _goalKcal.value = kcal
        _goalProtein.value = protein
        _goalCarbs.value = carbs
        _goalFat.value = fat
        _goalFiber.value = fiber
        _goalSugar.value = sugar
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

    fun addWaterRecord(amount: Int) {
        viewModelScope.launch {
            val newRecord = WaterRecord(
                amount = amount,
                timestamp = System.currentTimeMillis(),
                date = _currentDate.value
            )
            repository.insertWaterRecord(newRecord)
        }
    }

    fun updateWaterRecord(id: Int, newAmount: Int) {
        viewModelScope.launch {
            val existingRecord = waterRecords.value.find { it.id == id }
            if (existingRecord != null) {
                repository.updateWaterRecord(existingRecord.copy(amount = newAmount))
            }
        }
    }

    fun updateWaterGoal(newGoal: Int) {
        _waterGoal.value = newGoal
    }

    fun deleteWaterRecord(id: Int) {
        viewModelScope.launch {
            repository.deleteWaterRecordById(id)
        }
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

    private val _barcodeError = MutableStateFlow<String?>(null)
    val barcodeError: StateFlow<String?> = _barcodeError.asStateFlow()

    private val _isBarcodeLoading = MutableStateFlow(false)
    val isBarcodeLoading: StateFlow<Boolean> = _isBarcodeLoading.asStateFlow()

    fun clearBarcodeError() {
        _barcodeError.value = null
    }

    fun searchBarcode(barcode: String, onFound: () -> Unit) {
        viewModelScope.launch {
            _isBarcodeLoading.value = true
            _barcodeError.value = null
            try {
                val product = repository.getFoodByBarcode(barcode)
                if (product != null) {
                    _scannedProductPreview.value = product
                    _barcodeError.value = null
                    onFound()
                } else {
                    _barcodeError.value =
                        "Code '$barcode' erkannt, aber kein Produkt in der Datenbank gefunden."
                }
            } catch (e: Exception) {
                _barcodeError.value =
                    "Netzwerkfehler beim Abrufen des Barcodes. Bitte erneut versuchen."
            } finally {
                _isBarcodeLoading.value = false
            }
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

    fun updateDiaryEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            repository.insertDiaryEntry(entry)
        }
    }

    fun deleteDiaryEntry(entry: DiaryEntry) {
        viewModelScope.launch { repository.deleteDiaryEntry(entry) }
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
                fiber = food.fiber * factor,
                sugar = food.sugar * factor,
                amountInGrams = amountInGrams,
                mealType = mealType,
                date = _currentDate.value
            )
            repository.insertDiaryEntry(entry)
            clearPreview()
        }
    }

    fun addCustomFoodToDiary(
        name: String, kcal: Int, p: Double, c: Double, f: Double,
        fiber: Double, sugar: Double, grams: Double, mealType: String
    ) {
        viewModelScope.launch {
            val newFood = FoodItem(
                name = name, calories = kcal, protein = p, carbs = c, fat = f,
                fiber = fiber, sugar = sugar, isCustom = true
            )
            repository.insertFood(newFood)
            val factor = grams / 100.0
            val entry = DiaryEntry(
                foodId = 0, foodName = name, calories = (kcal * factor).toInt(),
                protein = p * factor, carbs = c * factor, fat = f * factor,
                fiber = fiber * factor, sugar = sugar * factor,
                amountInGrams = grams, mealType = mealType, date = _currentDate.value
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
                fat = entry.fat * ratio,
                fiber = entry.fiber * ratio,
                sugar = entry.sugar * ratio
            )
            repository.insertDiaryEntry(updatedEntry)
        }
    }

    fun addRecipeToDiary(recipe: Recipe, mealType: String) {
        viewModelScope.launch {
            val ingredients = repository.getIngredientsForRecipe(recipe.id)
            ingredients.forEach { ingredient ->
                val entry = DiaryEntry(
                    foodId = ingredient.foodId, foodName = ingredient.foodName,
                    calories = ingredient.calories, protein = ingredient.protein,
                    carbs = ingredient.carbs, fat = ingredient.fat,
                    fiber = ingredient.fiber, sugar = ingredient.sugar,
                    amountInGrams = ingredient.amountInGrams, mealType = mealType,
                    date = _currentDate.value
                )
                repository.insertDiaryEntry(entry)
            }
            clearPreview()
        }
    }

    fun addIngredientToTempRecipe(food: FoodItem, amountInGrams: Double) {
        val factor = amountInGrams / 100.0
        val ingredient = RecipeIngredient(
            recipeId = 0, foodId = food.id, foodName = food.name, amountInGrams = amountInGrams,
            calories = (food.calories * factor).toInt(), protein = food.protein * factor,
            carbs = food.carbs * factor, fat = food.fat * factor,
            fiber = food.fiber * factor, sugar = food.sugar * factor
        )
        _tempIngredients.value = _tempIngredients.value + ingredient
        clearPreview()
    }

    fun addCustomIngredientToTempRecipe(
        name: String, kcal: Int, p: Double, c: Double, f: Double,
        fiber: Double, sugar: Double, grams: Double
    ) {
        viewModelScope.launch {
            val newFood = FoodItem(
                name = name,
                calories = kcal,
                protein = p,
                carbs = c,
                fat = f,
                fiber = fiber,
                sugar = sugar,
                isCustom = true
            )
            repository.insertFood(newFood)
            val factor = grams / 100.0
            val ingredient = RecipeIngredient(
                recipeId = 0, foodId = 0, foodName = name, amountInGrams = grams,
                calories = (kcal * factor).toInt(), protein = p * factor, carbs = c * factor,
                fat = f * factor, fiber = fiber * factor, sugar = sugar * factor
            )
            _tempIngredients.value = _tempIngredients.value + ingredient
            clearPreview()
        }
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

    fun saveRecipe(name: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val ingredients = _tempIngredients.value
            if (ingredients.isEmpty()) return@launch

            val newRecipe = Recipe(
                name = name,
                totalCalories = ingredients.sumOf { it.calories },
                totalProtein = ingredients.sumOf { it.protein },
                totalCarbs = ingredients.sumOf { it.carbs },
                totalFat = ingredients.sumOf { it.fat },
                totalFiber = ingredients.sumOf { it.fiber },
                totalSugar = ingredients.sumOf { it.sugar }
            )
            repository.createRecipeWithIngredients(newRecipe, ingredients)
            resetRecipeBuilder()
            onSuccess()
        }
    }

    fun updateRecipeIngredient(oldIngredient: RecipeIngredient, newIngredient: RecipeIngredient) {
        _tempIngredients.value = _tempIngredients.value.map {
            if (it == oldIngredient) newIngredient else it
        }
    }

    fun addWeightEntryWithDate(weight: Double, dateMillis: Long) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(dateMillis))
            repository.insertWeight(WeightEntry(weight = weight, date = dateStr, timestamp = dateMillis))
        }
    }

    fun updateWeightEntry(entry: WeightEntry, newWeight: Double, newDateMillis: Long) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(newDateMillis))
            repository.insertWeight(entry.copy(weight = newWeight, date = dateStr, timestamp = newDateMillis))
        }
    }

    fun deleteWeightEntry(entry: WeightEntry) {
        viewModelScope.launch {
            repository.deleteWeight(entry)
        }
    }

    fun updateRecipeIngredientGrams(ingredient: RecipeIngredient, newGrams: Double) {
        val ratio = newGrams / ingredient.amountInGrams
        val updatedIngredient = ingredient.copy(
            amountInGrams = newGrams,
            calories = (ingredient.calories * ratio).toInt(),
            protein = ingredient.protein * ratio,
            carbs = ingredient.carbs * ratio,
            fat = ingredient.fat * ratio,
            fiber = ingredient.fiber * ratio,
            sugar = ingredient.sugar * ratio
        )
        _tempIngredients.value =
            _tempIngredients.value.map { if (it == ingredient) updatedIngredient else it }
    }
}