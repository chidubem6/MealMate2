package com.example.mealmate2

import com.example.mealmate2.data.FoodLogEntry
import com.example.mealmate2.ui.home.MealDetailsSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class MealDetailsSummaryTest {

    @Test
    fun filterEntries_returnsOnlySelectedMeal() {
        val entries = sampleEntries()

        val breakfast = MealDetailsSummary.filterEntries(entries, "breakfast")

        assertEquals(1, breakfast.size)
        assertEquals("Eggs", breakfast.first().foodName)
    }

    @Test
    fun filterEntries_allFoodReturnsEveryEntry() {
        val entries = sampleEntries()

        val allFood = MealDetailsSummary.filterEntries(entries, MealDetailsSummary.CATEGORY_ALL)

        assertEquals(entries, allFood)
    }

    @Test
    fun calculateTotals_sumsVisibleEntries() {
        val totals = MealDetailsSummary.calculateTotals(sampleEntries())

        assertEquals(450, totals.calories)
        assertEquals(42f, totals.carbsG, 0.01f)
        assertEquals(17f, totals.fatG, 0.01f)
        assertEquals(32f, totals.proteinG, 0.01f)
    }

    private fun sampleEntries(): List<FoodLogEntry> = listOf(
        FoodLogEntry(
            id = 1,
            date = 1,
            mealCategory = "breakfast",
            foodName = "Eggs",
            calories = 150,
            carbsG = 2f,
            fatG = 10f,
            proteinG = 14f
        ),
        FoodLogEntry(
            id = 2,
            date = 1,
            mealCategory = "lunch",
            foodName = "Rice",
            calories = 300,
            carbsG = 40f,
            fatG = 7f,
            proteinG = 18f
        )
    )
}
