package com.example.mealmate2.ui.templates

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mealmate2.data.DiaryRepository
import com.example.mealmate2.data.MealMateDatabase
import com.example.mealmate2.data.MealTemplateWithItems
import kotlinx.coroutines.launch

class MealTemplatesViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = DiaryRepository(MealMateDatabase.getInstance(app))

    val templates: LiveData<List<MealTemplateWithItems>> = repo.observeAllTemplates().asLiveData()

    fun delete(id: Long) {
        viewModelScope.launch { repo.deleteTemplate(id) }
    }

    fun logTemplate(templateId: Long, date: Long, mealCategory: String) {
        viewModelScope.launch { repo.logTemplate(templateId, date, mealCategory) }
    }
}
