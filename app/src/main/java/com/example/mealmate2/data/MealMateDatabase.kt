package com.example.mealmate2.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        FoodLogEntry::class,
        WeightEntry::class,
        DailyNote::class,
        WaterEntry::class,
        CustomFood::class,
        MealTemplate::class,
        MealTemplateItem::class,
        Meal::class,
        Ingredient::class,
        ShoppingItem::class
    ],
    version = 5,
    exportSchema = false
)
abstract class MealMateDatabase : RoomDatabase() {
    abstract fun foodLogDao(): FoodLogDao
    abstract fun weightDao(): WeightDao
    abstract fun noteDao(): NoteDao
    abstract fun waterDao(): WaterDao
    abstract fun customFoodDao(): CustomFoodDao
    abstract fun mealTemplateDao(): MealTemplateDao

    companion object {
        @Volatile private var instance: MealMateDatabase? = null

        fun getInstance(context: Context): MealMateDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MealMateDatabase::class.java,
                    "mealmate.db"
                )
                    .fallbackToDestructiveMigration()
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .build().also { instance = it }
            }
        }
    }
}
