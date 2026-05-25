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
import com.example.nutrition.model.WorkoutEntry
import kotlinx.coroutines.flow.combine
import com.example.nutrition.nutritionUI.goalsScreen.HealthConnectManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ProjectionMode(val label: String) {
    CURRENT("Aktuell (Tagesbilanz)"),
    GOAL("Ziel"),
    MINUS_500("-500 kcal"),
    MINUS_1000("-1000 kcal"),
    PLUS_500("+500 kcal"),
    PLUS_1000("+1000 kcal")
}

class FoodViewModel(
    private val repository: FoodRepository,
    val healthConnectManager: HealthConnectManager
) : ViewModel() {

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
        viewModelScope.launch {
            weightHistory.collect { history ->
                val latestWeight = history.sortedBy { it.timestamp }.lastOrNull()?.weight
                if (latestWeight != null) {
                    autoRecalculateGoal(latestWeight)
                }
            }
        }
    }

    private fun autoRecalculateGoal(newWeight: Double) {
        val age = repository.getSavedAge().toIntOrNull()
        val height = repository.getSavedHeight().toDoubleOrNull()
        val isMale = repository.getSavedIsMale()
        val activityLevel = repository.getSavedActivityLevel()
        val goalOffset = repository.getSavedGoalOffset()

        if (age != null && height != null && isMale != null && activityLevel != null && goalOffset != null) {
            calculateAndSetGoal(
                isMale = isMale,
                weightKg = newWeight,
                heightCm = height,
                ageYears = age,
                activityLevel = activityLevel,
                goalOffset = goalOffset
            )
        }
    }

    fun saveTargetWeight(weight: String) {
        repository.saveCalculatorInputs(
            age = getSavedAge(),
            height = getSavedHeight(),
            targetWeight = weight,
            isMale = getSavedIsMale() ?: true,
            activityLevel = getSavedActivityLevel() ?: 1.2,
            goalOffset = getSavedGoalOffset() ?: -500
        )
    }

    fun getBmr(): Double {
        val history = weightHistory.value.sortedBy { it.timestamp }
        val latestWeight = history.lastOrNull()?.weight ?: 80.0
        val age = repository.getSavedAge().toIntOrNull() ?: 25
        val height = repository.getSavedHeight().toDoubleOrNull() ?: 180.0
        val isMale = repository.getSavedIsMale() ?: true

        return if (isMale) {
            (10 * latestWeight) + (6.25 * height) - (5 * age) + 5
        } else {
            (10 * latestWeight) + (6.25 * height) - (5 * age) - 161
        }
    }

    fun getProjectedWeightPath(mode: ProjectionMode, days: Int = 30): List<WeightEntry> {
        val history = weightHistory.value.sortedBy { it.timestamp }
        val latestEntry = history.lastOrNull() ?: return emptyList()

        val dailyBalance = when (mode) {
            ProjectionMode.CURRENT -> {
                val bmr = getBmr().toInt()
                val totalBurned =
                    bmr + _activityKcal.value + localWorkouts.value.sumOf { it.calories }
                val eaten = diaryEntries.value.sumOf { it.calories }
                var balance = eaten - totalBurned

                if (eaten == 0) {
                    val pastEntries = analysisEntries.value
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    val yesterdayStr =
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                    val yesterdayEaten =
                        pastEntries.filter { it.date == yesterdayStr }.sumOf { it.calories }
                    if (yesterdayEaten > 0) {
                        balance = yesterdayEaten - totalBurned
                    }
                }
                balance.toDouble()
            }

            ProjectionMode.GOAL -> getSavedGoalOffset()?.toDouble() ?: 0.0
            ProjectionMode.MINUS_500 -> -500.0
            ProjectionMode.MINUS_1000 -> -1000.0
            ProjectionMode.PLUS_500 -> 500.0
            ProjectionMode.PLUS_1000 -> 1000.0
        }

        val dailyChangeKg = dailyBalance / 7000.0

        val projectionList = mutableListOf<WeightEntry>()
        var currentWeight = latestEntry.weight
        var currentTimestamp = latestEntry.timestamp
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (i in 1..days) {
            currentWeight += dailyChangeKg
            currentTimestamp += 24L * 60 * 60 * 1000

            projectionList.add(
                WeightEntry(
                    weight = String.format(Locale.US, "%.1f", currentWeight).toDouble(),
                    date = sdf.format(Date(currentTimestamp)),
                    timestamp = currentTimestamp
                )
            )
        }
        return projectionList
    }


    fun syncHealthData() {
        viewModelScope.launch {
            if (healthConnectManager.isAvailable && healthConnectManager.hasAllPermissions()) {
                val stats = healthConnectManager.getHealthStatsForDate(_currentDate.value)
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

        val protein = (finalKcal * 0.30 / 4).toInt()
        val carbs = (finalKcal * 0.40 / 4).toInt()
        val fat = (finalKcal * 0.30 / 9).toInt()
        val fiber = 30
        val sugar = 50

        repository.saveCalculatorInputs(
            age = ageYears.toString(),
            height = heightCm.toString(),
            targetWeight = getSavedTargetWeight(),
            isMale = isMale,
            activityLevel = activityLevel,
            goalOffset = goalOffset
        )

        updateAllGoals(finalKcal, protein, carbs, fat, fiber, sugar)
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
            val existing = allFoods.value.find {
                (food.barcode != null && it.barcode == food.barcode) ||
                        (it.name == food.name && it.calories == food.calories)
            }
            val finalFood = if (existing != null) food.copy(id = existing.id) else food
            repository.insertFood(finalFood)

            val factor = amountInGrams / 100.0
            val entry = DiaryEntry(
                foodId = finalFood.id,
                foodName = finalFood.name,
                calories = (finalFood.calories * factor).toInt(),
                protein = finalFood.protein * factor,
                carbs = finalFood.carbs * factor,
                fat = finalFood.fat * factor,
                fiber = finalFood.fiber * factor,
                sugar = finalFood.sugar * factor,
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
        viewModelScope.launch {
            val existing = allFoods.value.find {
                (food.barcode != null && it.barcode == food.barcode) ||
                        (it.name == food.name && it.calories == food.calories)
            }
            val finalFood = if (existing != null) food.copy(id = existing.id) else food
            repository.insertFood(finalFood)
        }

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
            val dateStr =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(dateMillis))
            repository.insertWeight(
                WeightEntry(
                    weight = weight,
                    date = dateStr,
                    timestamp = dateMillis
                )
            )
        }
    }

    fun updateWeightEntry(entry: WeightEntry, newWeight: Double, newDateMillis: Long) {
        viewModelScope.launch {
            val dateStr =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(newDateMillis))
            repository.insertWeight(
                entry.copy(
                    weight = newWeight,
                    date = dateStr,
                    timestamp = newDateMillis
                )
            )
        }
    }

    fun deleteWeightEntry(entry: WeightEntry) {
        viewModelScope.launch {
            repository.deleteWeight(entry)
        }
    }

    val localWorkouts: StateFlow<List<WorkoutEntry>> = _currentDate
        .flatMapLatest { date -> repository.getWorkoutsByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalActivityKcal: StateFlow<Int> =
        combine(_activityKcal, localWorkouts) { hcKcal, workouts ->
            hcKcal + workouts.sumOf { it.calories }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addManualWorkout(name: String, calories: Int, duration: Int) {
        viewModelScope.launch {
            val newWorkout = WorkoutEntry(
                name = name,
                calories = calories,
                durationMinutes = duration,
                date = _currentDate.value,
                timestamp = System.currentTimeMillis()
            )
            repository.insertWorkout(newWorkout)
        }
    }

    fun deleteWorkout(workout: WorkoutEntry) {
        viewModelScope.launch {
            repository.deleteWorkout(workout)
        }
    }

    fun updateManualWorkout(
        workout: WorkoutEntry,
        newName: String,
        newCalories: Int,
        newDuration: Int
    ) {
        viewModelScope.launch {
            val updatedWorkout = workout.copy(
                name = newName,
                calories = newCalories,
                durationMinutes = newDuration
            )
            repository.insertWorkout(updatedWorkout)
        }
    }

    fun getLastAmountForFood(foodName: String): Double {
        val lastEntry = analysisEntries.value.lastOrNull { it.foodName == foodName }
        return lastEntry?.amountInGrams ?: 100.0
    }

    fun deleteFoodFromHistory(food: FoodItem) {
        viewModelScope.launch {
            try {
                repository.deleteFood(food)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}