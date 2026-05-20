package com.example.mealmate2.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.mealmate2.data.DiaryRepository
import com.example.mealmate2.data.MealMateDatabase
import com.example.mealmate2.data.UserPreferences
import com.example.mealmate2.data.WeightEntry
import com.example.mealmate2.domain.MacroGoalCalculator
import com.example.mealmate2.domain.MacroGramGoals
import com.example.mealmate2.domain.MacroPercentGoals
import com.example.mealmate2.domain.ReminderScheduler

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = UserPreferences(app)
    private val scheduler = ReminderScheduler(app)
    private val db = MealMateDatabase.getInstance(app)
    private val repo = DiaryRepository(db)

    val calorieGoal: Int get() = prefs.calorieGoal
    val carbGoalPercent: Int get() = prefs.carbGoalPercent
    val fatGoalPercent: Int get() = prefs.fatGoalPercent
    val proteinGoalPercent: Int get() = prefs.proteinGoalPercent
    val startingWeightKg: Float get() = prefs.startingWeightKg
    val weightReminderEnabled: Boolean get() = prefs.weightReminderEnabled
    val weightReminderTime: String get() = prefs.weightReminderTime
    val dailyWaterGoalMl: Float get() = prefs.dailyWaterGoalMl
    val waterUnitIsOz: Boolean get() = prefs.waterUnitIsOz

    val latestWeight: LiveData<WeightEntry?> = repo.observeLatestWeight().asLiveData()

    fun calculateMacroGrams(
        calorieGoal: Int,
        carbPercent: Int,
        fatPercent: Int,
        proteinPercent: Int
    ): MacroGramGoals {
        return MacroGoalCalculator.calculateGrams(
            calorieGoal,
            MacroPercentGoals(carbPercent, fatPercent, proteinPercent)
        )
    }

    fun validateMacroGoals(
        calorieGoal: Int,
        carbPercent: Int,
        fatPercent: Int,
        proteinPercent: Int
    ): String? {
        return MacroGoalCalculator.validate(
            calorieGoal,
            MacroPercentGoals(carbPercent, fatPercent, proteinPercent)
        )
    }

    fun saveSettings(
        calorieGoal: Int,
        carbGoalPercent: Int,
        fatGoalPercent: Int,
        proteinGoalPercent: Int,
        startingWeightKg: Float,
        weightReminderEnabled: Boolean,
        weightReminderTime: String,
        waterGoalMl: Float,
        waterUnitIsOz: Boolean
    ) {
        prefs.calorieGoal = calorieGoal
        prefs.carbGoalPercent = carbGoalPercent
        prefs.fatGoalPercent = fatGoalPercent
        prefs.proteinGoalPercent = proteinGoalPercent
        prefs.startingWeightKg = startingWeightKg
        prefs.weightReminderEnabled = weightReminderEnabled
        prefs.weightReminderTime = weightReminderTime
        prefs.dailyWaterGoalMl = waterGoalMl
        prefs.waterUnitIsOz = waterUnitIsOz
        scheduler.scheduleWeightReminder(weightReminderEnabled, weightReminderTime)
    }
}
