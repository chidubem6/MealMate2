package com.example.mealmate2.provider

import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MealMateContentProviderTest {
    private val resolver = ApplicationProvider.getApplicationContext<android.content.Context>().contentResolver

    @Before
    fun cleanDatabase() {
        resolver.delete(MealMateContentProvider.MEALS_URI, null, null)
        resolver.delete(MealMateContentProvider.SHOPPING_URI, null, null)
    }

    @Test
    fun insertsQueriesUpdatesAndDeletesMeal() {
        val uri = resolver.insert(
            MealMateContentProvider.MEALS_URI,
            ContentValues().apply {
                put("name", "Test Meal")
                put("instructions", "Cook until done.")
                put("servings", 2)
                put("createdAt", 1L)
                put("updatedAt", 1L)
            }
        )
        assertNotNull(uri)
        val mealUri = requireNotNull(uri)

        resolver.query(mealUri, null, null, null, null).use { cursor ->
            assertNotNull(cursor)
            requireNotNull(cursor).moveToFirst()
            assertEquals("Test Meal", cursor.getString(cursor.getColumnIndexOrThrow("name")))
            assertEquals("Cook until done.", cursor.getString(cursor.getColumnIndexOrThrow("instructions")))
            assertEquals(2, cursor.getInt(cursor.getColumnIndexOrThrow("servings")))
        }

        val updated = resolver.update(
            mealUri,
            ContentValues().apply {
                put("name", "Updated Meal")
                put("instructions", "Updated instructions.")
                put("servings", 4)
                put("updatedAt", 2L)
            },
            null,
            null
        )
        assertEquals(1, updated)

        resolver.query(mealUri, null, null, null, null).use { cursor ->
            requireNotNull(cursor).moveToFirst()
            assertEquals("Updated Meal", cursor.getString(cursor.getColumnIndexOrThrow("name")))
            assertEquals("Updated instructions.", cursor.getString(cursor.getColumnIndexOrThrow("instructions")))
            assertEquals(4, cursor.getInt(cursor.getColumnIndexOrThrow("servings")))
        }

        assertEquals(1, resolver.delete(mealUri, null, null))
    }

    @Test
    fun queriesShoppingItems() {
        val uri = resolver.insert(
            MealMateContentProvider.SHOPPING_URI,
            ContentValues().apply {
                put("name", "Garlic")
                put("quantity", 2.0)
                put("unit", "items")
                put("isChecked", false)
                put("isManual", true)
            }
        )
        assertNotNull(uri)

        resolver.query(MealMateContentProvider.SHOPPING_URI, null, null, null, null).use { cursor ->
            assertNotNull(cursor)
            requireNotNull(cursor).moveToFirst()
            assertEquals("Garlic", cursor.getString(cursor.getColumnIndexOrThrow("name")))
        }
    }

    @Test
    fun invalidUriThrowsSafely() {
        assertThrows(IllegalArgumentException::class.java) {
            resolver.query(Uri.parse("content://${MealMateContentProvider.AUTHORITY}/unknown"), null, null, null, null)
        }
    }

    @Test
    fun invalidProjectionThrowsSafely() {
        assertThrows(IllegalArgumentException::class.java) {
            resolver.query(MealMateContentProvider.MEALS_URI, arrayOf("name; DROP TABLE meals"), null, null, null)
        }
    }

    @Test
    fun invalidSortOrderThrowsSafely() {
        assertThrows(IllegalArgumentException::class.java) {
            resolver.query(MealMateContentProvider.MEALS_URI, null, null, null, "name COLLATE NOCASE")
        }
    }

    @Test
    fun itemUriUsesId() {
        val uri = resolver.insert(
            MealMateContentProvider.MEALS_URI,
            ContentValues().apply {
                put("name", "ID Meal")
                put("createdAt", 1L)
                put("updatedAt", 1L)
            }
        )
        val id = ContentUris.parseId(requireNotNull(uri))
        resolver.query(ContentUris.withAppendedId(MealMateContentProvider.MEALS_URI, id), null, null, null, null).use {
            assertNotNull(it)
            requireNotNull(it).moveToFirst()
            assertEquals("ID Meal", it.getString(it.getColumnIndexOrThrow("name")))
        }
    }
}
