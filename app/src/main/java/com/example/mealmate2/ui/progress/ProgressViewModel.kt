package com.example.mealmate2.ui.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mealmate2.data.DiaryRepository
import com.example.mealmate2.data.FoodLogEntry
import com.example.mealmate2.data.MealMateDatabase
import com.example.mealmate2.data.UserPreferences
import com.example.mealmate2.data.WeightEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ProgressUiState(
    val weeklyAverageCalories: Float = 0f,
    val weeklyAvgCarbsG: Float = 0f,
    val weeklyAvgFatG: Float = 0f,
    val weeklyAvgProteinG: Float = 0f,
    val currentWeightKg: Float? = null,
    val startingWeightKg: Float? = null,
    val weightChangeKg: Float? = null,
    val calorieGoal: Int = 2000,
    val carbGoalG: Int = 250,
    val fatGoalG: Int = 65,
    val proteinGoalG: Int = 50,
    val weightHistory: List<WeightEntry> = emptyList()
)

class ProgressViewModel(app: Application) : AndroidViewModel(app) {
    private val db = MealMateDatabase.getInstance(app)
    private val repo = DiaryRepository(db)
    private val prefs = UserPreferences(app)

    private val _weeklyEntries = MutableStateFlow<List<FoodLogEntry>>(emptyList())
    private val _weightHistory = MutableStateFlow<List<WeightEntry>>(emptyList())

    init {
        viewModelScope.launch {
            val today = LocalDate.now()
            val startDay = today.minusDays(6).toEpochDay()
            val endDay = today.toEpochDay()
            _weeklyEntries.value = repo.getEntriesInRange(startDay, endDay)
            _weightHistory.value = repo.getWeightHistory(today.minusDays(29).toEpochDay(), endDay)
        }
    }

    val state: LiveData<ProgressUiState> = combine(
        repo.observeLatestWeight(),
        _weeklyEntries,
        _weightHistory
    ) { latestWeight: WeightEntry?, entries: List<FoodLogEntry>, history: List<WeightEntry> ->
        val daysWithEntries = entries.groupBy { it.date }
        val numDays = daysWithEntries.size.coerceAtLeast(1)
        val avgCalories = if (daysWithEntries.isEmpty()) 0f
            else entries.sumOf { it.calories }.toFloat() / numDays
        val avgCarbs = if (daysWithEntries.isEmpty()) 0f
            else entries.sumOf { it.carbsG.toDouble() }.toFloat() / numDays
        val avgFat = if (daysWithEntries.isEmpty()) 0f
            else entries.sumOf { it.fatG.toDouble() }.toFloat() / numDays
        val avgProtein = if (daysWithEntries.isEmpty()) 0f
            else entries.sumOf { it.proteinG.toDouble() }.toFloat() / numDays

        val currentKg = latestWeight?.weightKg
        val startingKg = prefs.startingWeightKg.takeIf { it > 0f }
        val changeKg = if (currentKg != null && startingKg != null) currentKg - startingKg else null

        ProgressUiState(
            weeklyAverageCalories = avgCalories,
            weeklyAvgCarbsG = avgCarbs,
            weeklyAvgFatG = avgFat,
            weeklyAvgProteinG = avgProtein,
            currentWeightKg = currentKg,
            startingWeightKg = startingKg,
            weightChangeKg = changeKg,
            calorieGoal = prefs.calorieGoal,
            carbGoalG = prefs.carbGoalG,
            fatGoalG = prefs.fatGoalG,
            proteinGoalG = prefs.proteinGoalG,
            weightHistory = history
        )
    }.asLiveData()
}
