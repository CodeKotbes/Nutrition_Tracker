package com.example.nutrition.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nutrition.model.Recipe
import com.example.nutrition.model.RecipeIngredient
import kotlinx.coroutines.flow.Flow
import kotlin.jvm.JvmSuppressWildcards

@Dao
interface RecipeDao {
    @JvmSuppressWildcards
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @JvmSuppressWildcards
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredients(ingredients: List<RecipeIngredient>)

    @Query("SELECT * FROM recipes ORDER BY name ASC")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun getIngredientsForRecipe(recipeId: Int): List<RecipeIngredient>

    @androidx.room.Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsForRecipe(recipeId: Int)
}