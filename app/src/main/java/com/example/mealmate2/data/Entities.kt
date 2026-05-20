package com.example.mealmate2.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "food_log_entries")
data class FoodLogEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val mealCategory: String,
    val foodName: String,
    val brand: String = "",
    val calories: Int,
    val carbsG: Float,
    val fatG: Float,
    val proteinG: Float,
    val quantityG: Float = 100f,
    val servingSize: String = ""
)

@Entity(tableName = "weight_entries")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val weightKg: Float
)

@Entity(tableName = "daily_notes")
data class DailyNote(
    @PrimaryKey val date: Long,
    val note: String
)

@Entity(tableName = "water_entries")
data class WaterEntry(
    @PrimaryKey val date: Long,
    val milliliters: Float = 0f
)

@Entity(tableName = "custom_foods")
data class CustomFood(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String = "",
    val calories: Int,
    val carbsG: Float,
    val fatG: Float,
    val proteinG: Float,
    val servingSize: String = "100g"
)

@Entity(tableName = "meal_templates")
data class MealTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdDate: Long
)

@Entity(tableName = "meal_template_items")
data class MealTemplateItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long,
    val foodName: String,
    val brand: String = "",
    val calories: Int,
    val carbsG: Float,
    val fatG: Float,
    val proteinG: Float,
    val quantityG: Float,
    val servingSize: String = ""
)

data class MealTemplateWithItems(
    @Embedded val template: MealTemplate,
    @Relation(parentColumn = "id", entityColumn = "templateId")
    val items: List<MealTemplateItem>
)

@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val instructions: String = "",
    val servings: Int = 1,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "ingredients",
    indices = [Index("mealId")]
)
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealId: Long,
    val name: String,
    val quantity: Double,
    val unit: String
)

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: Double,
    val unit: String,
    val isChecked: Boolean = false,
    val isManual: Boolean = false
)
