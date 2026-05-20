package com.example.mealmate2

import com.example.mealmate2.data.Ingredient
import com.example.mealmate2.data.ShoppingItem
import com.example.mealmate2.domain.IngredientCombiner
import org.junit.Assert.assertEquals
import org.junit.Test

class IngredientCombinerTest {
    @Test
    fun combine_mergesMatchingNamesAndUnits() {
        val result = IngredientCombiner.combine(
            listOf(
                ingredient("Beef Mince", 500.0, "g"),
                ingredient(" beef mince ", 500.0, "g"),
                ingredient("Onion", 1.0, "item")
            )
        )

        assertEquals(2, result.size)
        assertEquals("Beef Mince", result[0].name)
        assertEquals(1000.0, result[0].quantity, 0.01)
        assertEquals("g", result[0].unit)
    }

    @Test
    fun combine_keepsDifferentUnitsSeparate() {
        val result = IngredientCombiner.combine(
            listOf(
                ingredient("Milk", 500.0, "ml"),
                ingredient("milk", 1.0, "carton")
            )
        )

        assertEquals(2, result.size)
        assertEquals(listOf("carton", "ml"), result.map { it.unit }.sorted())
    }

    @Test
    fun combine_preservesManualShoppingItems() {
        val result = IngredientCombiner.combine(
            ingredients = listOf(ingredient("Garlic", 2.0, "items")),
            manualItems = listOf(ShoppingItem(name = "Kitchen foil", quantity = 1.0, unit = "roll", isManual = true))
        )

        assertEquals(2, result.size)
        assertEquals("Kitchen foil", result.last().name)
        assertEquals(true, result.last().isManual)
    }

    private fun ingredient(name: String, quantity: Double, unit: String) = Ingredient(
        mealId = 1,
        name = name,
        quantity = quantity,
        unit = unit
    )
}
