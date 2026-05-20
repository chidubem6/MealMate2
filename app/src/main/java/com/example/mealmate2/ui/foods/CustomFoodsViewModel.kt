package com.example.mealmate2.ui.foods

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mealmate2.data.CustomFood
import com.example.mealmate2.data.DiaryRepository
import com.example.mealmate2.data.MealMateDatabase
import kotlinx.coroutines.launch

class CustomFoodsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = DiaryRepository(MealMateDatabase.getInstance(app))

    val foods: LiveData<List<CustomFood>> = repo.observeAllCustomFoods().asLiveData()

    fun delete(id: Long) {
        viewModelScope.launch { repo.deleteCustomFood(id) }
    }

    fun save(food: CustomFood) {
        viewModelScope.launch { repo.saveCustomFood(food) }
    }
}
