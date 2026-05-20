package com.example.mealmate2.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodLogDao {
    @Query("SELECT * FROM food_log_entries WHERE date = :date ORDER BY id ASC")
    fun observeByDate(date: Long): Flow<List<FoodLogEntry>>

    @Query("SELECT * FROM food_log_entries WHERE date = :date AND mealCategory = :category ORDER BY id ASC")
    fun observeByDateAndCategory(date: Long, category: String): Flow<List<FoodLogEntry>>

    @Query("SELECT * FROM food_log_entries WHERE date >= :startDay AND date <= :endDay ORDER BY date ASC")
    suspend fun getEntriesInRange(startDay: Long, endDay: Long): List<FoodLogEntry>

    @Insert
    suspend fun insert(entry: FoodLogEntry)

    @Query("DELETE FROM food_log_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface WeightDao {
    @Query("SELECT * FROM weight_entries ORDER BY date DESC LIMIT 1")
    fun observeLatest(): Flow<WeightEntry?>

    @Query("SELECT * FROM weight_entries ORDER BY date DESC LIMIT 1")
    suspend fun getLatest(): WeightEntry?

    @Query("SELECT * FROM weight_entries WHERE date = :date LIMIT 1")
    fun observeByDate(date: Long): Flow<WeightEntry?>

    @Query("SELECT * FROM weight_entries WHERE date >= :startDay AND date <= :endDay ORDER BY date ASC")
    suspend fun getInRange(startDay: Long, endDay: Long): List<WeightEntry>

    @Insert
    suspend fun insert(entry: WeightEntry)

    @Query("DELETE FROM weight_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM daily_notes WHERE date = :date LIMIT 1")
    fun observeByDate(date: Long): Flow<DailyNote?>

    @Upsert
    suspend fun upsert(note: DailyNote)
}

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_entries WHERE date = :date LIMIT 1")
    fun observeByDate(date: Long): Flow<WaterEntry?>

    @Upsert
    suspend fun upsert(entry: WaterEntry)
}

@Dao
interface CustomFoodDao {
    @Query("SELECT * FROM custom_foods ORDER BY name ASC")
    fun observeAll(): Flow<List<CustomFood>>

    @Query("SELECT * FROM custom_foods WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    suspend fun search(query: String): List<CustomFood>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(food: CustomFood)

    @Query("DELETE FROM custom_foods WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface MealTemplateDao {
    @Transaction
    @Query("SELECT * FROM meal_templates ORDER BY createdDate DESC")
    fun observeAll(): Flow<List<MealTemplateWithItems>>

    @Transaction
    @Query("SELECT * FROM meal_templates WHERE id = :id")
    suspend fun getWithItems(id: Long): MealTemplateWithItems?

    @Insert
    suspend fun insertTemplate(template: MealTemplate): Long

    @Insert
    suspend fun insertItems(items: List<MealTemplateItem>)

    @Query("DELETE FROM meal_templates WHERE id = :id")
    suspend fun deleteTemplate(id: Long)

    @Query("DELETE FROM meal_template_items WHERE templateId = :templateId")
    suspend fun deleteItemsForTemplate(templateId: Long)
}
