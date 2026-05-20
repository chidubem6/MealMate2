package com.example.mealmate2

import com.example.mealmate2.domain.MacroGoalCalculator
import com.example.mealmate2.domain.MacroPercentGoals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MacroGoalCalculatorTest {

    @Test
    fun calculateGrams_usesMfpStyleDefaults() {
        val grams = MacroGoalCalculator.calculateGrams(
            2000,
            MacroPercentGoals(carbPercent = 50, fatPercent = 30, proteinPercent = 20)
        )

        assertEquals(250, grams.carbsG)
        assertEquals(67, grams.fatG)
        assertEquals(100, grams.proteinG)
    }

    @Test
    fun calculateGrams_updatesWhenCaloriesChange() {
        val grams = MacroGoalCalculator.calculateGrams(
            1800,
            MacroPercentGoals(carbPercent = 50, fatPercent = 30, proteinPercent = 20)
        )

        assertEquals(225, grams.carbsG)
        assertEquals(60, grams.fatG)
        assertEquals(90, grams.proteinG)
    }

    @Test
    fun validate_rejectsPercentagesThatDoNotTotalOneHundred() {
        val error = MacroGoalCalculator.validate(
            2000,
            MacroPercentGoals(carbPercent = 40, fatPercent = 30, proteinPercent = 20)
        )

        assertEquals("Macro percentages must total 100%.", error)
    }

    @Test
    fun validate_acceptsValidGoals() {
        val error = MacroGoalCalculator.validate(
            2000,
            MacroPercentGoals(carbPercent = 50, fatPercent = 30, proteinPercent = 20)
        )

        assertNull(error)
    }
}
