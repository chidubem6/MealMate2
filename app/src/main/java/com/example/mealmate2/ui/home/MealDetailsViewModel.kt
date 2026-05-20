package com.example.mealmate2.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mealmate2.data.DiaryRepository
import com.example.mealmate2.data.FoodLogEntry
import com.example.mealmate2.data.MealMateDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

data class MealDetailsUiState(
    val selectedCategory: String = "breakfast",
    val title: String = "Breakfast",
    val entries: List<FoodLogEntry> = emptyList(),
    val totals: MealMacroTotals = MealMacroTotals()
)

@OptIn(ExperimentalCoroutinesApi::class)
class MealDetailsViewModel(app: Application) : AndroidViewModel(app) {
    private val db = MealMateDatabase.getInstance(app)
    private val repo = DiaryRepository(db)

    private val selectedDate = MutableStateFlow(0L)
    private val selectedCategory = MutableStateFlow("breakfast")

    val state: LiveData<MealDetailsUiState> = selectedDate.flatMapLatest { date ->
        combine(
            repo.observeByDate(date),
            selectedCategory
        ) { entries, category ->
            val normalizedCategory = MealDetailsSummary.normalizeCategory(category)
            val filteredEntries = MealDetailsSummary.filterEntries(entries, normalizedCategory)
            MealDetailsUiState(
                selectedCategory = normalizedCategory,
                title = MealDetailsSummary.displayName(normalizedCategory),
                entries = filteredEntries,
                totals = MealDetailsSummary.calculateTotals(filteredEntries)
            )
        }
    }.asLiveData()

    fun initialize(date: Long, initialMealCategory: String) {
        selectedDate.value = date
        selectedCategory.value = MealDetailsSummary.normalizeCategory(initialMealCategory)
    }

    fun selectMeal(category: String) {
        selectedCategory.value = MealDetailsSummary.normalizeCategory(category)
    }

    fun deleteEntry(entry: FoodLogEntry) {
        viewModelScope.launch { repo.deleteFoodLog(entry.id) }
    }
}
