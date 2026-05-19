package com.example.nutrition.model

import com.google.gson.annotations.SerializedName

data class ProductSearchResponse(
    val count: Int?,
    val products: List<ApiProduct>?
)

data class OpenFoodFactsResponse(
    val status: Int,
    val product: ApiProduct?
)

data class ApiProduct(
    @SerializedName("product_name") val productName: String?,
    val brands: String?,
    val nutriments: ApiNutriments?
)

data class ApiNutriments(
    @SerializedName("energy-kcal_100g") val energyKcal: Double?,
    @SerializedName("proteins_100g") val proteins: Double?,
    @SerializedName("carbohydrates_100g") val carbs: Double?,
    @SerializedName("fat_100g") val fat: Double?
)