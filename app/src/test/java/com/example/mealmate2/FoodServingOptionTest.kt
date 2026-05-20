package com.example.mealmate2

import com.example.mealmate2.network.FatSecretServing
import com.example.mealmate2.network.FatSecretServings
import com.example.mealmate2.network.FatSecretServingsDeserializer
import com.example.mealmate2.network.FoodProduct
import com.example.mealmate2.network.calculateTotals
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FoodServingOptionTest {

    @Test
    fun servingOptions_includeLoadedServingsAndOneGramOption() {
        val product = FoodProduct(
            1,
            "Egg",
            null,
            null,
            FatSecretServings(
                listOf(
                    FatSecretServing("1 large", 50f, "g", "1", 72f, 0.4f, 4.8f, 6.3f),
                    FatSecretServing("100 g", 100f, "g", "0", 143f, 0.7f, 9.5f, 12.6f)
                )
            )
        )

        val options = product.servingOptions

        assertEquals(listOf("1 large", "100 g", "1 gram"), options.map { it.label })
        assertEquals("1 large", product.defaultServingOption?.label)
    }

    @Test
    fun servingsDeserializer_handlesSingleServingObject() {
        val gson = GsonBuilder()
            .registerTypeAdapter(FatSecretServings::class.java, FatSecretServingsDeserializer())
            .create()
        val json = """
            {
              "serving": {
                "serving_description": "1 cup",
                "metric_serving_amount": "244.000",
                "metric_serving_unit": "g",
                "is_default": "1",
                "calories": "149",
                "carbohydrate": "11.7",
                "fat": "7.9",
                "protein": "7.7"
              }
            }
        """.trimIndent()

        val servings = gson.fromJson(json, FatSecretServings::class.java)

        assertEquals(1, servings.serving.size)
        assertEquals("1 cup", servings.serving.first().servingDescription)
    }

    @Test
    fun missingServings_useDescriptionMacrosForOneGramOption() {
        val product = FoodProduct(
            2,
            "Egg",
            null,
            "Per 100g - Calories: 155kcal | Fat: 11g | Carbs: 1.1g | Protein: 13g",
            null
        )

        val totals = product.defaultServingOption!!.calculateTotals(100f)

        assertEquals("1 gram", product.defaultServingOption?.label)
        assertEquals(155, totals.calories)
        assertEquals(1.1f, totals.carbsG, 0.01f)
        assertEquals(11f, totals.fatG, 0.01f)
        assertEquals(13f, totals.proteinG, 0.01f)
        assertEquals(100f, totals.quantityG, 0.01f)
    }

    @Test
    fun calculateTotals_supportsFractionalServings() {
        val serving = FoodProduct(
            3,
            "Milk",
            null,
            null,
            FatSecretServings(
                listOf(FatSecretServing("1 cup", 244f, "g", "1", 149f, 11.7f, 7.9f, 7.7f))
            )
        ).defaultServingOption!!

        val totals = serving.calculateTotals(0.5f)

        assertEquals(75, totals.calories)
        assertEquals(5.85f, totals.carbsG, 0.01f)
        assertEquals(3.95f, totals.fatG, 0.01f)
        assertEquals(3.85f, totals.proteinG, 0.01f)
        assertEquals(122f, totals.quantityG, 0.01f)
    }

    @Test
    fun calculateTotals_rejectsInvalidServingCounts() {
        val serving = FoodProduct(
            4,
            "Rice",
            null,
            "Per 100g - Calories: 130kcal | Fat: 0.3g | Carbs: 28g | Protein: 2.7g",
            null
        ).defaultServingOption!!

        assertThrows(IllegalArgumentException::class.java) {
            serving.calculateTotals(0f)
        }
    }
}
