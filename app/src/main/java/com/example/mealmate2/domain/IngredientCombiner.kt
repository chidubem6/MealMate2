package com.example.mealmate2.domain

import com.example.mealmate2.data.Ingredient
import com.example.mealmate2.data.ShoppingItem
import java.util.Locale

object IngredientCombiner {
    fun combine(ingredients: List<Ingredient>, manualItems: List<ShoppingItem> = emptyList()): List<ShoppingItem> {
        val generated = ingredients
            .groupBy { IngredientKey(it.name.trim().lowercase(Locale.getDefault()), it.unit.trim()) }
            .map { (key, matchingIngredients) ->
                val displayName = matchingIngredients.first().name.trim()
                val totalQuantity = matchingIngredients.sumOf { it.quantity }
                ShoppingItem(
                    name = displayName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    quantity = totalQuantity,
                    unit = key.unit,
                    isChecked = false,
                    isManual = false
                )
            }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        return generated + manualItems.filter { it.isManual }
    }

    private data class IngredientKey(
        val normalizedName: String,
        val unit: String
    )
}
