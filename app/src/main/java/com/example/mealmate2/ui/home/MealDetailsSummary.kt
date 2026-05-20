package com.example.mealmate2.ui.home

import com.example.mealmate2.data.FoodLogEntry

data class MealMacroTotals(
    val calories: Int = 0,
    val carbsG: Float = 0f,
    val fatG: Float = 0f,
    val proteinG: Float = 0f
)

object MealDetailsSummary {
    const val CATEGORY_ALL = "all"
    val categories = listOf("breakfast", "lunch", "dinner", "snacks", CATEGORY_ALL)

    fun displayName(category: String): String = when (category) {
        "breakfast" -> "Breakfast"
        "lunch" -> "Lunch"
        "dinner" -> "Dinner"
        "snacks" -> "Snacks"
        CATEGORY_ALL -> "All Food"
        else -> "Meal"
    }

    fun normalizeCategory(category: String): String {
        return categories.firstOrNull { it == category } ?: "breakfast"
    }

    fun filterEntries(entries: List<FoodLogEntry>, category: String): List<FoodLogEntry> {
        val normalizedCategory = normalizeCategory(category)
        return if (normalizedCategory == CATEGORY_ALL) {
            entries
        } else {
            entries.filter { it.mealCategory == normalizedCategory }
        }
    }

    fun calculateTotals(entries: List<FoodLogEntry>): MealMacroTotals {
        return MealMacroTotals(
            calories = entries.sumOf { it.calories },
            carbsG = entries.sumOf { it.carbsG.toDouble() }.toFloat(),
            fatG = entries.sumOf { it.fatG.toDouble() }.toFloat(),
            proteinG = entries.sumOf { it.proteinG.toDouble() }.toFloat()
        )
    }
}
