package com.example.mealmate2.ui.search

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mealmate2.data.CustomFood
import com.example.mealmate2.data.DiaryRepository
import com.example.mealmate2.data.FoodLogEntry
import com.example.mealmate2.data.MealMateDatabase
import com.example.mealmate2.network.FatSecretApiException
import com.example.mealmate2.network.FatSecretClient
import com.example.mealmate2.network.FatSecretServing
import com.example.mealmate2.network.FatSecretServings
import com.example.mealmate2.network.FoodProduct
import com.example.mealmate2.network.FoodServingTotals
import com.example.mealmate2.network.MissingFatSecretCredentialsException
import com.google.gson.JsonParseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class FoodSearchViewModel(app: Application) : AndroidViewModel(app) {
    private companion object {
        const val TAG = "FoodSearchViewModel"
    }

    private val db = MealMateDatabase.getInstance(app)
    private val diaryRepo = DiaryRepository(db)
    private val service = FatSecretClient.create()

    val searchQuery = MutableStateFlow("")

    private val _searchResults = MutableStateFlow<List<FoodProduct>>(emptyList())
    val searchResults: StateFlow<List<FoodProduct>> = _searchResults.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val isCustomOnly = MutableStateFlow(false)

    fun setCustomOnly(enabled: Boolean) {
        isCustomOnly.value = enabled
        _error.value = null
        if (enabled) {
            searchCustomOnly()
        } else {
            _searchResults.value = emptyList()
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
        if (isCustomOnly.value) {
            searchCustomOnly()
        } else if (query.isBlank()) {
            _loading.value = false
            _error.value = null
            _searchResults.value = emptyList()
        }
    }

    private fun searchCustomOnly() {
        viewModelScope.launch {
            val results = diaryRepo.searchCustomFoods(searchQuery.value.trim())
                .map { it.toFoodProduct() }
            _searchResults.value = results
        }
    }

    fun search() {
        val query = searchQuery.value.trim()
        if (query.isBlank()) {
            _loading.value = false
            _error.value = null
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val customResults = diaryRepo.searchCustomFoods(query).map { it.toFoodProduct() }
                val response = service.searchFood(query)
                val apiResults = response.products.filter { it.displayName != "Unknown food" }
                _searchResults.value = customResults + apiResults
            } catch (e: MissingFatSecretCredentialsException) {
                Log.w(TAG, "FatSecret credentials missing", e)
                _error.value = "FatSecret API credentials are missing. Add the Consumer Key and Secret to local.properties."
                _searchResults.value = emptyList()
            } catch (e: FatSecretApiException) {
                Log.w(TAG, "FatSecret API error", e)
                _error.value = e.code?.let { code ->
                    "${e.message ?: "FatSecret returned an error."} ($code)"
                } ?: (e.message ?: "FatSecret returned an error.")
                _searchResults.value = emptyList()
            } catch (e: HttpException) {
                Log.w(TAG, "FatSecret HTTP error", e)
                _error.value = when (e.code()) {
                    400 -> "FatSecret rejected the request. Check the Consumer Key and Secret in local.properties, then rebuild the app."
                    401 -> "FatSecret credentials were rejected. Check your Consumer Key and Secret."
                    429 -> "Too many searches. Wait a minute and try again."
                    503 -> "FatSecret is temporarily unavailable. Try again shortly."
                    else -> "FatSecret search failed (${e.code()}). Try again shortly."
                }
                _searchResults.value = emptyList()
            } catch (e: JsonParseException) {
                Log.w(TAG, "FatSecret parse error", e)
                _error.value = "FatSecret returned an unexpected response. Try again shortly."
                _searchResults.value = emptyList()
            } catch (e: IOException) {
                Log.w(TAG, "FatSecret network error", e)
                _error.value = "Network error: ${e.javaClass.simpleName}. Check device internet and try again."
                _searchResults.value = emptyList()
            } catch (e: Exception) {
                Log.w(TAG, "FatSecret unexpected error", e)
                _error.value = "Search failed. Try again shortly."
                _searchResults.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    suspend fun loadFoodDetails(product: FoodProduct): FoodProduct {
        val foodId = product.id ?: return product
        _loading.value = true
        _error.value = null
        return try {
            service.getFood(foodId)
        } catch (e: MissingFatSecretCredentialsException) {
            Log.w(TAG, "FatSecret credentials missing", e)
            _error.value = "FatSecret API credentials are missing. Add the Consumer Key and Secret to local.properties."
            product
        } catch (e: FatSecretApiException) {
            Log.w(TAG, "FatSecret food detail error", e)
            _error.value = e.code?.let { code ->
                "${e.message ?: "Could not load serving sizes."} ($code)"
            } ?: (e.message ?: "Could not load serving sizes.")
            product
        } catch (e: HttpException) {
            Log.w(TAG, "FatSecret food detail HTTP error", e)
            _error.value = when (e.code()) {
                400 -> "FatSecret rejected the request. Check the Consumer Key and Secret in local.properties, then rebuild the app."
                401 -> "FatSecret credentials were rejected. Check your Consumer Key and Secret."
                else -> "Could not load serving sizes (${e.code()})."
            }
            product
        } catch (e: IOException) {
            Log.w(TAG, "FatSecret food detail network error", e)
            _error.value = "Network error loading serving sizes."
            product
        } catch (e: Exception) {
            Log.w(TAG, "FatSecret food detail unexpected error", e)
            _error.value = "Could not load serving sizes."
            product
        } finally {
            _loading.value = false
        }
    }

    private fun CustomFood.toFoodProduct(): FoodProduct = FoodProduct(
        id = null,
        name = name,
        brand = brand.takeIf { it.isNotBlank() },
        foodDescription = null,
        servings = FatSecretServings(
            listOf(
                FatSecretServing(
                    servingDescription = servingSize.ifBlank { "1 serving" },
                    metricServingAmount = null,
                    metricServingUnit = null,
                    isDefault = "1",
                    calories = calories.toFloat(),
                    carbohydrate = carbsG,
                    fat = fatG,
                    protein = proteinG
                )
            )
        )
    )

    fun logFood(
        date: Long,
        mealCategory: String,
        product: FoodProduct,
        totals: FoodServingTotals
    ) {
        viewModelScope.launch {
            _error.value = null
            if (!product.hasMacros) {
                _error.value = "This FatSecret result has no macro data. Choose another result."
                return@launch
            }
            if (totals.numberOfServings <= 0f) {
                _error.value = "Enter a number of servings greater than 0."
                return@launch
            }
            if (totals.quantityG <= 0f) {
                _error.value = "Could not calculate a valid quantity for this serving."
                return@launch
            }

            try {
                diaryRepo.insertFoodLog(
                    FoodLogEntry(
                        date = date,
                        mealCategory = mealCategory,
                        foodName = product.displayName,
                        brand = product.displayBrand,
                        calories = totals.calories,
                        carbsG = totals.carbsG,
                        fatG = totals.fatG,
                        proteinG = totals.proteinG,
                        quantityG = totals.quantityG,
                        servingSize = totals.servingLabel
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed to save food", e)
                _error.value = "Could not add this food. Try another result."
            }
        }
    }
}
