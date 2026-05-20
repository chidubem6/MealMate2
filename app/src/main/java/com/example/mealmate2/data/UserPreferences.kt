package com.example.mealmate2.data

import android.content.Context
import com.example.mealmate2.domain.MacroGoalCalculator
import com.example.mealmate2.domain.MacroPercentGoals

class UserPreferences(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("calorie_prefs", Context.MODE_PRIVATE)

    var calorieGoal: Int
        get() = prefs.getInt(KEY_CALORIE_GOAL, 2000)
        set(value) = prefs.edit().putInt(KEY_CALORIE_GOAL, value).apply()

    val carbGoalG: Int
        get() = MacroGoalCalculator.calculateGrams(calorieGoal, macroPercentGoals).carbsG

    val fatGoalG: Int
        get() = MacroGoalCalculator.calculateGrams(calorieGoal, macroPercentGoals).fatG

    val proteinGoalG: Int
        get() = MacroGoalCalculator.calculateGrams(calorieGoal, macroPercentGoals).proteinG

    var carbGoalPercent: Int
        get() = prefs.getInt(KEY_CARB_PERCENT, 50)
        set(value) = prefs.edit().putInt(KEY_CARB_PERCENT, value).apply()

    var fatGoalPercent: Int
        get() = prefs.getInt(KEY_FAT_PERCENT, 30)
        set(value) = prefs.edit().putInt(KEY_FAT_PERCENT, value).apply()

    var proteinGoalPercent: Int
        get() = prefs.getInt(KEY_PROTEIN_PERCENT, 20)
        set(value) = prefs.edit().putInt(KEY_PROTEIN_PERCENT, value).apply()

    val macroPercentGoals: MacroPercentGoals
        get() = MacroPercentGoals(carbGoalPercent, fatGoalPercent, proteinGoalPercent)

    var startingWeightKg: Float
        get() = prefs.getFloat(KEY_STARTING_WEIGHT, 0f)
        set(value) = prefs.edit().putFloat(KEY_STARTING_WEIGHT, value).apply()

    var weightReminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_WEIGHT_REMINDER_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_WEIGHT_REMINDER_ENABLED, value).apply()

    var weightReminderTime: String
        get() = prefs.getString(KEY_WEIGHT_REMINDER_TIME, "08:00") ?: "08:00"
        set(value) = prefs.edit().putString(KEY_WEIGHT_REMINDER_TIME, value).apply()

    var dailyWaterGoalMl: Float
        get() = prefs.getFloat(KEY_WATER_GOAL_ML, 2000f)
        set(value) = prefs.edit().putFloat(KEY_WATER_GOAL_ML, value).apply()

    var waterUnitIsOz: Boolean
        get() = prefs.getBoolean(KEY_WATER_UNIT_OZ, false)
        set(value) = prefs.edit().putBoolean(KEY_WATER_UNIT_OZ, value).apply()

    companion object {
        private const val KEY_CALORIE_GOAL = "calorieGoal"
        private const val KEY_CARB_PERCENT = "carbGoalPercent"
        private const val KEY_FAT_PERCENT = "fatGoalPercent"
        private const val KEY_PROTEIN_PERCENT = "proteinGoalPercent"
        private const val KEY_STARTING_WEIGHT = "startingWeightKg"
        private const val KEY_WEIGHT_REMINDER_ENABLED = "weightReminderEnabled"
        private const val KEY_WEIGHT_REMINDER_TIME = "weightReminderTime"
        private const val KEY_WATER_GOAL_ML = "waterGoalMl"
        private const val KEY_WATER_UNIT_OZ = "waterUnitOz"
    }
}
