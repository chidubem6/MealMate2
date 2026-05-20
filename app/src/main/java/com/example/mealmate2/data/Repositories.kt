package com.example.mealmate2.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class DiaryRepository(private val db: MealMateDatabase) {
    private val foodLogDao = db.foodLogDao()
    private val noteDao = db.noteDao()
    private val weightDao = db.weightDao()
    private val waterDao = db.waterDao()
    private val customFoodDao = db.customFoodDao()
    private val templateDao = db.mealTemplateDao()

    // ── Food log ─────────────────────────────────────────────────────────────

    fun observeByDate(date: Long): Flow<List<FoodLogEntry>> = foodLogDao.observeByDate(date)

    fun observeByDateAndCategory(date: Long, category: String): Flow<List<FoodLogEntry>> =
        foodLogDao.observeByDateAndCategory(date, category)

    suspend fun getEntriesInRange(startDay: Long, endDay: Long): List<FoodLogEntry> =
        foodLogDao.getEntriesInRange(startDay, endDay)

    suspend fun insertFoodLog(entry: FoodLogEntry) = foodLogDao.insert(entry)

    suspend fun deleteFoodLog(id: Long) = foodLogDao.deleteById(id)

    // ── Notes ─────────────────────────────────────────────────────────────────

    fun observeNote(date: Long): Flow<DailyNote?> = noteDao.observeByDate(date)

    suspend fun upsertNote(date: Long, note: String) = noteDao.upsert(DailyNote(date, note))

    // ── Weight ────────────────────────────────────────────────────────────────

    fun observeLatestWeight(): Flow<WeightEntry?> = weightDao.observeLatest()

    suspend fun getLatestWeight(): WeightEntry? = weightDao.getLatest()

    fun observeWeightByDate(date: Long): Flow<WeightEntry?> = weightDao.observeByDate(date)

    suspend fun insertWeight(date: Long, weightKg: Float) {
        weightDao.insert(WeightEntry(date = date, weightKg = weightKg))
    }

    suspend fun getWeightHistory(startDay: Long, endDay: Long): List<WeightEntry> =
        weightDao.getInRange(startDay, endDay)

    // ── Water ─────────────────────────────────────────────────────────────────

    fun observeWater(date: Long): Flow<WaterEntry?> = waterDao.observeByDate(date)

    suspend fun setWater(date: Long, milliliters: Float) =
        waterDao.upsert(WaterEntry(date = date, milliliters = milliliters))

    // ── Custom foods ──────────────────────────────────────────────────────────

    fun observeAllCustomFoods(): Flow<List<CustomFood>> = customFoodDao.observeAll()

    suspend fun searchCustomFoods(query: String): List<CustomFood> = customFoodDao.search(query)

    suspend fun saveCustomFood(food: CustomFood) = customFoodDao.insert(food)

    suspend fun deleteCustomFood(id: Long) = customFoodDao.deleteById(id)

    // ── Meal templates ────────────────────────────────────────────────────────

    fun observeAllTemplates(): Flow<List<MealTemplateWithItems>> = templateDao.observeAll()

    suspend fun saveTemplate(name: String, items: List<MealTemplateItem>) {
        val templateId = templateDao.insertTemplate(
            MealTemplate(name = name, createdDate = LocalDate.now().toEpochDay())
        )
        templateDao.insertItems(items.map { it.copy(templateId = templateId) })
    }

    suspend fun deleteTemplate(id: Long) {
        templateDao.deleteItemsForTemplate(id)
        templateDao.deleteTemplate(id)
    }

    suspend fun logTemplate(templateId: Long, date: Long, mealCategory: String) {
        val withItems = templateDao.getWithItems(templateId) ?: return
        withItems.items.forEach { item ->
            foodLogDao.insert(
                FoodLogEntry(
                    date = date,
                    mealCategory = mealCategory,
                    foodName = item.foodName,
                    brand = item.brand,
                    calories = item.calories,
                    carbsG = item.carbsG,
                    fatG = item.fatG,
                    proteinG = item.proteinG,
                    quantityG = item.quantityG,
                    servingSize = item.servingSize
                )
            )
        }
    }
}
