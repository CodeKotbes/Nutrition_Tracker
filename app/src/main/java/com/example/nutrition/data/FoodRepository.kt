package com.example.nutrition.data

import android.content.SharedPreferences
import com.example.nutrition.model.DiaryEntry
import com.example.nutrition.model.FoodItem
import com.example.nutrition.model.Recipe
import com.example.nutrition.model.RecipeIngredient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Calendar
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class FoodRepository(
    private val foodItemDao: FoodItemDao,
    private val diaryDao: DiaryDao,
    private val recipeDao: RecipeDao,
    private val prefs: SharedPreferences

) {
    private val customHttpClient: OkHttpClient = try {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(
                chain: Array<out X509Certificate>?,
                authType: String?
            ) {
            }

            override fun checkServerTrusted(
                chain: Array<out X509Certificate>?,
                authType: String?
            ) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory = sslContext.socketFactory

        OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "NutritionTrackerApp - Android - Version 1.0")
                    .build()
                chain.proceed(request)
            }
            .build()
    } catch (e: Exception) {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "NutritionTrackerApp - Android - Version 1.0")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    suspend fun deleteRecipe(recipe: Recipe) {
        recipeDao.deleteIngredientsForRecipe(recipe.id)
        recipeDao.deleteRecipe(recipe)
    }

    suspend fun createRecipeWithIngredients(recipe: Recipe, ingredients: List<RecipeIngredient>) {
        val recipeId = recipeDao.insertRecipe(recipe).toInt()
        recipeDao.deleteIngredientsForRecipe(recipeId)
        val ingredientsWithRecipeId = ingredients.map { it.copy(recipeId = recipeId) }
        recipeDao.insertRecipeIngredients(ingredientsWithRecipeId)
    }

    fun getSavedGoal(): Int {
        return prefs.getInt("goal_kcal", 2500)
    }

    fun saveGoal(kcal: Int) {
        prefs.edit().putInt("goal_kcal", kcal).apply()
    }

    fun getSavedDarkMode(): Boolean {
        return prefs.getBoolean("dark_mode", false)
    }

    fun saveDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean("dark_mode", isDark).apply()
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://world.openfoodfacts.org/")
        .client(customHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(OpenFoodFactsApi::class.java)

    val allFoods: Flow<List<FoodItem>> = foodItemDao.getAllFoods()

    suspend fun insertFood(foodItem: FoodItem) {
        foodItemDao.insertFood(foodItem)
    }

    suspend fun insertDiaryEntry(entry: DiaryEntry) {
        diaryDao.insertDiaryEntry(entry)
    }

    suspend fun deleteDiaryEntry(entry: DiaryEntry) {
        diaryDao.deleteDiaryEntry(entry)
    }

    fun getDiaryEntriesByDate(date: String): Flow<List<DiaryEntry>> {
        return diaryDao.getEntriesByDate(date)
    }

    suspend fun searchFoodByName(query: String): List<FoodItem> {
        return try {
            val response = api.searchProductByName(query)
            response.products?.map { apiProduct ->
                val nutriments = apiProduct.nutriments
                FoodItem(
                    name = apiProduct.productName ?: "Unbekanntes Produkt",
                    brand = apiProduct.brands,
                    barcode = null,
                    calories = nutriments?.energyKcal?.toInt() ?: 0,
                    protein = nutriments?.proteins ?: 0.0,
                    carbs = nutriments?.carbs ?: 0.0,
                    fat = nutriments?.fat ?: 0.0,
                    isCustom = false
                )
            } ?: emptyList()
        } catch (e: Exception) {
            android.util.Log.e("API_FEHLER", "Suche kaputt wegen: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    fun getEntriesForRange(start: String, end: String): Flow<List<DiaryEntry>> {
        return diaryDao.getEntriesForDateRange(start, end)
    }

    val allRecipes: Flow<List<Recipe>> = recipeDao.getAllRecipes()

    suspend fun getIngredientsForRecipe(recipeId: Int): List<RecipeIngredient> {
        return recipeDao.getIngredientsForRecipe(recipeId)
    }

    suspend fun getFoodByBarcode(barcode: String): FoodItem? {
        val localFood = foodItemDao.getFoodByBarcode(barcode)
        if (localFood != null) return localFood

        return try {
            val response = api.getProductByBarcode(barcode)
            if (response.status == 1 && response.product != null) {
                val apiProduct = response.product
                val nutriments = apiProduct.nutriments

                val newFood = FoodItem(
                    name = apiProduct.productName ?: "Unbekanntes Produkt",
                    brand = apiProduct.brands,
                    barcode = barcode,
                    calories = nutriments?.energyKcal?.toInt() ?: 0,
                    protein = nutriments?.proteins ?: 0.0,
                    carbs = nutriments?.carbs ?: 0.0,
                    fat = nutriments?.fat ?: 0.0,
                    isCustom = false
                )
                insertFood(newFood)
                newFood
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("API_FEHLER", "Barcode-Suche kaputt wegen: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}