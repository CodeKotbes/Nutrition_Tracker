package com.example.nutrition.data

import com.example.nutrition.model.OpenFoodFactsResponse
import com.example.nutrition.model.ProductSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenFoodFactsApi {
    @GET("api/v2/product/{barcode}.json")
    suspend fun getProductByBarcode(@Path("barcode") barcode: String): OpenFoodFactsResponse

    @GET("cgi/search.pl?search_simple=1&action=process&json=1&page_size=20")
    suspend fun searchProductByName(@Query("search_terms") name: String): ProductSearchResponse
}