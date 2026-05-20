package com.example.mealmate2.ui.foods

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mealmate2.R
import com.example.mealmate2.data.CustomFood
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class CreateCustomFoodFragment : Fragment() {

    private val viewModel: CustomFoodsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_create_custom_food, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editFoodName = view.findViewById<TextInputEditText>(R.id.editFoodName)
        val editBrand = view.findViewById<TextInputEditText>(R.id.editBrand)
        val editServingSize = view.findViewById<TextInputEditText>(R.id.editServingSize)
        val editCalories = view.findViewById<TextInputEditText>(R.id.editCalories)
        val editCarbs = view.findViewById<TextInputEditText>(R.id.editCarbs)
        val editFat = view.findViewById<TextInputEditText>(R.id.editFat)
        val editProtein = view.findViewById<TextInputEditText>(R.id.editProtein)
        val btnSaveFood = view.findViewById<MaterialButton>(R.id.btnSaveFood)

        btnSaveFood.setOnClickListener {
            val name = editFoodName.text?.toString()?.trim() ?: ""
            val calories = editCalories.text?.toString()?.toIntOrNull()

            if (name.isBlank()) {
                Toast.makeText(requireContext(), "Food name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (calories == null) {
                Toast.makeText(requireContext(), "Calories are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val food = CustomFood(
                name = name,
                brand = editBrand.text?.toString()?.trim() ?: "",
                servingSize = editServingSize.text?.toString()?.trim().takeIf { it?.isNotBlank() == true } ?: "100g",
                calories = calories,
                carbsG = editCarbs.text?.toString()?.toFloatOrNull() ?: 0f,
                fatG = editFat.text?.toString()?.toFloatOrNull() ?: 0f,
                proteinG = editProtein.text?.toString()?.toFloatOrNull() ?: 0f
            )
            viewModel.save(food)
            Toast.makeText(requireContext(), "Food saved", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }
}
