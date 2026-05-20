package com.example.mealmate2.domain

import kotlin.math.roundToInt

data class MacroPercentGoals(
    val carbPercent: Int = 50,
    val fatPercent: Int = 30,
    val proteinPercent: Int = 20
)

data class MacroGramGoals(
    val carbsG: Int,
    val fatG: Int,
    val proteinG: Int
)

object MacroGoalCalculator {
    fun validate(calorieGoal: Int, goals: MacroPercentGoals): String? {
        return when {
            calorieGoal <= 0 -> "Calorie goal must be greater than 0."
            goals.carbPercent < 0 || goals.fatPercent < 0 || goals.proteinPercent < 0 ->
                "Macro percentages cannot be negative."
            goals.carbPercent + goals.fatPercent + goals.proteinPercent != 100 ->
                "Macro percentages must total 100%."
            else -> null
        }
    }

    fun calculateGrams(calorieGoal: Int, goals: MacroPercentGoals): MacroGramGoals {
        return MacroGramGoals(
            carbsG = gramsFor(calorieGoal, goals.carbPercent, 4),
            fatG = gramsFor(calorieGoal, goals.fatPercent, 9),
            proteinG = gramsFor(calorieGoal, goals.proteinPercent, 4)
        )
    }

    private fun gramsFor(calorieGoal: Int, percent: Int, caloriesPerGram: Int): Int {
        return (calorieGoal * (percent / 100f) / caloriesPerGram).roundToInt()
    }
}
