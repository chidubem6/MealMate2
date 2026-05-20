package com.example.mealmate2.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mealmate2.data.DiaryRepository
import com.example.mealmate2.data.FoodLogEntry
import com.example.mealmate2.data.MealMateDatabase
import com.example.mealmate2.data.MealTemplateItem
import com.example.mealmate2.data.UserPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val date: LocalDate = LocalDate.now(),
    val calorieGoal: Int = 2000,
    val carbGoalG: Int = 250,
    val fatGoalG: Int = 65,
    val proteinGoalG: Int = 50,
    val breakfastEntries: List<FoodLogEntry> = emptyList(),
    val lunchEntries: List<FoodLogEntry> = emptyList(),
    val dinnerEntries: List<FoodLogEntry> = emptyList(),
    val snacksEntries: List<FoodLogEntry> = emptyList(),
    val totalCalories: Int = 0,
    val totalCarbsG: Float = 0f,
    val totalFatG: Float = 0f,
    val totalProteinG: Float = 0f,
    val weightKg: Float? = null,
    val note: String = "",
    val waterMl: Float = 0f,
    val waterGoalMl: Float = 2000f,
    val waterUnitIsOz: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val db = MealMateDatabase.getInstance(app)
    private val repo = DiaryRepository(db)
    private val prefs = UserPreferences(app)

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private var lastAddedMl: Float = 0f

    val state: LiveData<HomeUiState> = _selectedDate.flatMapLatest { date ->
        val epochDay = date.toEpochDay()
        combine(
            repo.observeByDate(epochDay),
            repo.observeWeightByDate(epochDay),
            repo.observeNote(epochDay),
            repo.observeWater(epochDay)
        ) { entries, weight, note, water ->
            val breakfast = entries.filter { it.mealCategory == "breakfast" }
            val lunch = entries.filter { it.mealCategory == "lunch" }
            val dinner = entries.filter { it.mealCategory == "dinner" }
            val snacks = entries.filter { it.mealCategory == "snacks" }
            HomeUiState(
                date = date,
                calorieGoal = prefs.calorieGoal,
                carbGoalG = prefs.carbGoalG,
                fatGoalG = prefs.fatGoalG,
                proteinGoalG = prefs.proteinGoalG,
                breakfastEntries = breakfast,
                lunchEntries = lunch,
                dinnerEntries = dinner,
                snacksEntries = snacks,
                totalCalories = entries.sumOf { it.calories },
                totalCarbsG = entries.sumOf { it.carbsG.toDouble() }.toFloat(),
                totalFatG = entries.sumOf { it.fatG.toDouble() }.toFloat(),
                totalProteinG = entries.sumOf { it.proteinG.toDouble() }.toFloat(),
                weightKg = weight?.weightKg,
                note = note?.note ?: "",
                waterMl = water?.milliliters ?: 0f,
                waterGoalMl = prefs.dailyWaterGoalMl,
                waterUnitIsOz = prefs.waterUnitIsOz
            )
        }
    }.asLiveData()

    fun goToPreviousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun goToNextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }

    fun goToDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun deleteEntry(entry: FoodLogEntry) {
        viewModelScope.launch { repo.deleteFoodLog(entry.id) }
    }

    fun saveWeightAndNote(weightKg: Float?, note: String) {
        viewModelScope.launch {
            val epochDay = _selectedDate.value.toEpochDay()
            weightKg?.let { repo.insertWeight(epochDay, it) }
            if (note.isNotBlank()) {
                repo.upsertNote(epochDay, note)
            }
        }
    }

    fun addWater(ml: Float) {
        viewModelScope.launch {
            val current = state.value ?: return@launch
            val newTotal = current.waterMl + ml
            lastAddedMl = ml
            repo.setWater(_selectedDate.value.toEpochDay(), newTotal)
        }
    }

    fun undoLastWater() {
        viewModelScope.launch {
            val current = state.value ?: return@launch
            val newTotal = (current.waterMl - lastAddedMl).coerceAtLeast(0f)
            repo.setWater(_selectedDate.value.toEpochDay(), newTotal)
            lastAddedMl = 0f
        }
    }

    fun setWaterTotal(ml: Float) {
        viewModelScope.launch {
            repo.setWater(_selectedDate.value.toEpochDay(), ml.coerceAtLeast(0f))
        }
    }

    fun saveTemplate(name: String, entries: List<FoodLogEntry>) {
        viewModelScope.launch {
            val items = entries.map { e ->
                MealTemplateItem(
                    templateId = 0,
                    foodName = e.foodName,
                    brand = e.brand,
                    calories = e.calories,
                    carbsG = e.carbsG,
                    fatG = e.fatG,
                    proteinG = e.proteinG,
                    quantityG = e.quantityG,
                    servingSize = e.servingSize
                )
            }
            repo.saveTemplate(name, items)
        }
    }
}
