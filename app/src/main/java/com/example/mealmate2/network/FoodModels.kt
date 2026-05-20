package com.example.mealmate2.network

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type
import kotlin.math.roundToInt

data class FoodSearchResponse(
    @SerializedName("foods_search") val foodsSearch: FatSecretFoodsSearch? = null,
    @SerializedName("foods") val foods: FatSecretFoods? = null,
    @SerializedName("food") val food: FoodProduct? = null,
    @SerializedName("error") val error: FatSecretError? = null
) {
    val products: List<FoodProduct>
        get() = foodsSearch?.results?.foods ?: foods?.food.orEmpty()
}

data class FatSecretError(
    @SerializedName("code") val code: Int? = null,
    @SerializedName("message") val message: String = "FatSecret returned an error."
)

data class FatSecretFoodsSearch(
    @SerializedName("results") val results: FatSecretFoodResults? = null
)

data class FatSecretFoodResults(
    @SerializedName("food") val foods: List<FoodProduct> = emptyList()
)

data class FatSecretFoods(
    @SerializedName("food") val food: List<FoodProduct> = emptyList()
)

data class FoodServingOption(
    val label: String,
    val gramsPerServing: Float?,
    val calories: Float,
    val carbsG: Float,
    val fatG: Float,
    val proteinG: Float,
    val isGramBased: Boolean
) {
    val hasMacros: Boolean
        get() = calories > 0f || carbsG > 0f || fatG > 0f || proteinG > 0f
}

data class FoodServingTotals(
    val servingLabel: String,
    val numberOfServings: Float,
    val quantityG: Float,
    val calories: Int,
    val carbsG: Float,
    val fatG: Float,
    val proteinG: Float
)

fun FoodServingOption.calculateTotals(numberOfServings: Float): FoodServingTotals {
    require(numberOfServings > 0f) { "Number of servings must be greater than 0." }
    val quantityG = gramsPerServing?.times(numberOfServings)
        ?: if (isGramBased) numberOfServings else 100f * numberOfServings
    return FoodServingTotals(
        servingLabel = label,
        numberOfServings = numberOfServings,
        quantityG = quantityG,
        calories = (calories * numberOfServings).roundToInt(),
        carbsG = carbsG * numberOfServings,
        fatG = fatG * numberOfServings,
        proteinG = proteinG * numberOfServings
    )
}

data class FoodProduct(
    @SerializedName("food_id") val id: Long?,
    @SerializedName("food_name") val name: String?,
    @SerializedName("brand_name") val brand: String?,
    @SerializedName("food_description") private val foodDescription: String?,
    @SerializedName("servings") val servings: FatSecretServings?
) {
    val displayName: String get() = name?.takeIf { it.isNotBlank() } ?: "Unknown food"
    val displayBrand: String get() = brand?.takeIf { it.isNotBlank() } ?: ""
    val servingSize: String? get() = serving?.servingDescription
    val hasMacros: Boolean
        get() = caloriesPer100g > 0 || carbsPer100g > 0f || fatPer100g > 0f || proteinPer100g > 0f
    val caloriesPer100g: Int get() = serving.toPer100g { calories }?.toInt() ?: descriptionMacros?.calories ?: 0
    val carbsPer100g: Float get() = serving.toPer100g { carbohydrate } ?: descriptionMacros?.carbsG ?: 0f
    val fatPer100g: Float get() = serving.toPer100g { fat } ?: descriptionMacros?.fatG ?: 0f
    val proteinPer100g: Float get() = serving.toPer100g { protein } ?: descriptionMacros?.proteinG ?: 0f
    val oneGramOption: FoodServingOption?
        get() = if (hasMacros) {
            FoodServingOption(
                label = "1 gram",
                gramsPerServing = 1f,
                calories = caloriesPer100g / 100f,
                carbsG = carbsPer100g / 100f,
                fatG = fatPer100g / 100f,
                proteinG = proteinPer100g / 100f,
                isGramBased = true
            )
        } else {
            null
        }
    val servingOptions: List<FoodServingOption>
        get() {
            val loadedOptions = servings?.serving.orEmpty()
                .mapNotNull { it.toServingOption() }
            return (loadedOptions + listOfNotNull(oneGramOption))
                .distinctBy { it.label.lowercase() }
        }
    val defaultServingOption: FoodServingOption?
        get() {
            val options = servingOptions
            return options.firstOrNull { option ->
                val matchingServing = servings?.serving.orEmpty().firstOrNull {
                    it.servingDescription == option.label
                }
                matchingServing?.isDefault == "1"
            } ?: options.firstOrNull { it.gramsPerServing == 100f }
                ?: options.firstOrNull { !it.isGramBased }
                ?: options.firstOrNull()
        }

    private val serving: FatSecretServing?
        get() {
            val allServings = servings?.serving.orEmpty()
            return allServings.firstOrNull { it.metricServingUnit == "g" && it.metricServingAmount == 100f }
                ?: allServings.firstOrNull { it.isDefault == "1" && it.metricServingUnit == "g" }
                ?: allServings.firstOrNull { it.metricServingUnit == "g" }
                ?: allServings.firstOrNull()
        }

    private val descriptionMacros: ParsedDescriptionMacros?
        get() = foodDescription?.let(::parseDescriptionMacros)
}

private fun FatSecretServing.toServingOption(): FoodServingOption? {
    val label = servingDescription?.takeIf { it.isNotBlank() } ?: return null
    val option = FoodServingOption(
        label = label,
        gramsPerServing = metricServingAmount?.takeIf { metricServingUnit == "g" && it > 0f },
        calories = calories ?: 0f,
        carbsG = carbohydrate ?: 0f,
        fatG = fat ?: 0f,
        proteinG = protein ?: 0f,
        isGramBased = metricServingUnit == "g" && metricServingAmount == 1f
    )
    return option.takeIf { it.hasMacros }
}

class FatSecretFoodsDeserializer : JsonDeserializer<FatSecretFoods> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): FatSecretFoods {
        if (!json.isJsonObject) return FatSecretFoods()
        val foodElement = json.asJsonObject.get("food") ?: return FatSecretFoods()
        if (!foodElement.isJsonArray && !foodElement.isJsonObject) return FatSecretFoods()
        val foods = if (foodElement.isJsonArray) {
            foodElement.asJsonArray.map { context.deserialize<FoodProduct>(it, FoodProduct::class.java) }
        } else {
            listOf(context.deserialize(foodElement, FoodProduct::class.java))
        }
        return FatSecretFoods(foods)
    }
}

data class FatSecretServings(
    @SerializedName("serving") val serving: List<FatSecretServing> = emptyList()
)

data class FatSecretServing(
    @SerializedName("serving_description") val servingDescription: String?,
    @SerializedName("metric_serving_amount") val metricServingAmount: Float?,
    @SerializedName("metric_serving_unit") val metricServingUnit: String?,
    @SerializedName("is_default") val isDefault: String?,
    @SerializedName("calories") val calories: Float?,
    @SerializedName("carbohydrate") val carbohydrate: Float?,
    @SerializedName("fat") val fat: Float?,
    @SerializedName("protein") val protein: Float?
)

class FatSecretFoodResultsDeserializer : JsonDeserializer<FatSecretFoodResults> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): FatSecretFoodResults {
        if (!json.isJsonObject) return FatSecretFoodResults()
        val foodElement = json.asJsonObject.get("food") ?: return FatSecretFoodResults()
        if (!foodElement.isJsonArray && !foodElement.isJsonObject) return FatSecretFoodResults()
        val foods = if (foodElement.isJsonArray) {
            foodElement.asJsonArray.map { context.deserialize<FoodProduct>(it, FoodProduct::class.java) }
        } else {
            listOf(context.deserialize(foodElement, FoodProduct::class.java))
        }
        return FatSecretFoodResults(foods)
    }
}

class FatSecretServingsDeserializer : JsonDeserializer<FatSecretServings> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): FatSecretServings {
        if (!json.isJsonObject) return FatSecretServings()
        val servingElement = json.asJsonObject.get("serving") ?: return FatSecretServings()
        if (!servingElement.isJsonArray && !servingElement.isJsonObject) return FatSecretServings()
        val servings = if (servingElement.isJsonArray) {
            servingElement.asJsonArray.map { context.deserialize<FatSecretServing>(it, FatSecretServing::class.java) }
        } else {
            listOf(context.deserialize(servingElement, FatSecretServing::class.java))
        }
        return FatSecretServings(servings)
    }
}

private fun FatSecretServing?.toPer100g(value: FatSecretServing.() -> Float?): Float? {
    if (this == null) return null
    val nutrient = value() ?: return null
    val amount = metricServingAmount
    return if (amount != null && amount > 0f && metricServingUnit == "g") {
        nutrient / amount * 100f
    } else {
        nutrient
    }
}

private data class ParsedDescriptionMacros(
    val calories: Int,
    val fatG: Float,
    val carbsG: Float,
    val proteinG: Float
)

private fun parseDescriptionMacros(description: String): ParsedDescriptionMacros? {
    val calories = Regex("""Calories:\s*([0-9.]+)kcal""")
        .find(description)
        ?.groupValues
        ?.getOrNull(1)
        ?.toFloatOrNull()
        ?.toInt()
        ?: return null

    val fatG = Regex("""Fat:\s*([0-9.]+)g""")
        .find(description)
        ?.groupValues
        ?.getOrNull(1)
        ?.toFloatOrNull()
        ?: 0f
    val carbsG = Regex("""Carbs:\s*([0-9.]+)g""")
        .find(description)
        ?.groupValues
        ?.getOrNull(1)
        ?.toFloatOrNull()
        ?: 0f
    val proteinG = Regex("""Protein:\s*([0-9.]+)g""")
        .find(description)
        ?.groupValues
        ?.getOrNull(1)
        ?.toFloatOrNull()
        ?: 0f

    return ParsedDescriptionMacros(calories, fatG, carbsG, proteinG)
}
