package com.example.mealmate2.ui.search

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmate2.R
import com.example.mealmate2.network.FoodProduct
import com.example.mealmate2.network.FoodServingOption
import com.example.mealmate2.network.calculateTotals
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.util.Locale

class FoodSearchBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: FoodSearchViewModel by activityViewModels()
    private lateinit var adapter: FoodSearchAdapter

    private var date: Long = 0L
    private var mealCategory: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        date = arguments?.getLong("date") ?: 0L
        mealCategory = arguments?.getString("mealCategory") ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_food_search, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setCustomOnly(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnUseTemplate = view.findViewById<MaterialButton>(R.id.btnUseTemplate)
        val chipGroupFilter = view.findViewById<ChipGroup>(R.id.chipGroupFilter)
        val editSearch = view.findViewById<TextInputEditText>(R.id.editSearch)
        val btnSearch = view.findViewById<MaterialButton>(R.id.btnSearch)
        val progressSearch = view.findViewById<CircularProgressIndicator>(R.id.progressSearch)
        val rvSearchResults = view.findViewById<RecyclerView>(R.id.rvSearchResults)
        val textNoResults = view.findViewById<TextView>(R.id.textNoResults)

        chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val isMyFoods = checkedIds.contains(R.id.chipMyFoods)
            viewModel.setCustomOnly(isMyFoods)
            btnSearch.visibility = if (isMyFoods) View.GONE else View.VISIBLE
        }

        btnUseTemplate.setOnClickListener {
            dismiss()
            val bundle = Bundle().apply {
                putLong("date", date)
                putString("mealCategory", mealCategory)
            }
            requireActivity().findNavController(R.id.nav_host_fragment)
                .navigate(R.id.mealTemplatesFragment, bundle)
        }

        adapter = FoodSearchAdapter { product ->
            viewLifecycleOwner.lifecycleScope.launch {
                val detailedProduct = viewModel.loadFoodDetails(product)
                if (isAdded) {
                    showQuantityDialog(detailedProduct)
                }
            }
        }

        rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        rvSearchResults.adapter = adapter

        editSearch.addTextChangedListener { text ->
            viewModel.updateSearchQuery(text?.toString() ?: "")
        }

        editSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.search()
                true
            } else {
                false
            }
        }

        btnSearch.setOnClickListener {
            viewModel.search()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.searchResults.collect { results ->
                        adapter.submitList(results)
                        updateStatusText(textNoResults)
                    }
                }

                launch {
                    viewModel.loading.collect { isLoading ->
                        progressSearch.visibility = if (isLoading) View.VISIBLE else View.GONE
                        updateStatusText(textNoResults)
                    }
                }

                launch {
                    viewModel.error.collect {
                        updateStatusText(textNoResults)
                    }
                }
            }
        }
    }

    private fun updateStatusText(textNoResults: TextView) {
        val error = viewModel.error.value
        val isLoading = viewModel.loading.value
        val hasResults = viewModel.searchResults.value.isNotEmpty()
        val isCustomOnly = viewModel.isCustomOnly.value
        val hasQuery = viewModel.searchQuery.value.isNotBlank()

        when {
            error != null -> {
                textNoResults.text = error
                textNoResults.visibility = View.VISIBLE
            }
            isCustomOnly && !hasResults -> {
                textNoResults.text = if (hasQuery) {
                    getString(R.string.no_custom_foods_match, viewModel.searchQuery.value)
                } else {
                    getString(R.string.no_custom_foods_saved)
                }
                textNoResults.visibility = View.VISIBLE
            }
            hasQuery && !isLoading && !hasResults -> {
                textNoResults.text = getString(R.string.no_results)
                textNoResults.visibility = View.VISIBLE
            }
            else -> {
                textNoResults.visibility = View.GONE
            }
        }
    }

    private fun showQuantityDialog(product: FoodProduct) {
        val context = requireContext()
        val servingOptions = product.servingOptions
        if (servingOptions.isEmpty()) {
            Toast.makeText(context, R.string.food_no_macro_data, Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_food_serving_selection, null)
        val textSelectedFoodName = dialogView.findViewById<TextView>(R.id.textSelectedFoodName)
        val textSelectedFoodBrand = dialogView.findViewById<TextView>(R.id.textSelectedFoodBrand)
        val layoutServingsCount = dialogView.findViewById<TextInputLayout>(R.id.layoutServingsCount)
        val dropdownServingSize = dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.dropdownServingSize)
        val editServingsCount = dialogView.findViewById<TextInputEditText>(R.id.editServingsCount)
        val dropdownMealCategory = dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.dropdownMealCategory)
        val macroPieChart = dialogView.findViewById<MacroPieChartView>(R.id.macroPieChart)
        val textCaloriesPreview = dialogView.findViewById<TextView>(R.id.textCaloriesPreview)
        val textCarbsPreview = dialogView.findViewById<TextView>(R.id.textCarbsPreview)
        val textFatPreview = dialogView.findViewById<TextView>(R.id.textFatPreview)
        val textProteinPreview = dialogView.findViewById<TextView>(R.id.textProteinPreview)
        val btnCancelFoodServing = dialogView.findViewById<MaterialButton>(R.id.btnCancelFoodServing)
        val btnAddFoodServing = dialogView.findViewById<MaterialButton>(R.id.btnAddFoodServing)

        textSelectedFoodName.text = product.displayName
        if (product.displayBrand.isNotBlank()) {
            textSelectedFoodBrand.visibility = View.VISIBLE
            textSelectedFoodBrand.text = product.displayBrand
        }

        var selectedServing = product.defaultServingOption ?: servingOptions.first()
        dropdownServingSize.setAdapter(
            ArrayAdapter(context, android.R.layout.simple_list_item_1, servingOptions.map { it.label })
        )
        dropdownServingSize.setText(selectedServing.label, false)
        dropdownServingSize.setOnItemClickListener { _, _, position, _ ->
            selectedServing = servingOptions[position]
            updateServingPreview(
                selectedServing,
                editServingsCount,
                layoutServingsCount,
                macroPieChart,
                textCaloriesPreview,
                textCarbsPreview,
                textFatPreview,
                textProteinPreview
            )
        }

        val mealOptions = listOf(
            "breakfast" to "Breakfast",
            "lunch" to "Lunch",
            "dinner" to "Dinner",
            "snacks" to "Snacks"
        )
        var selectedMeal = mealOptions.firstOrNull { it.first == mealCategory } ?: mealOptions.first()
        dropdownMealCategory.setAdapter(
            ArrayAdapter(context, android.R.layout.simple_list_item_1, mealOptions.map { it.second })
        )
        dropdownMealCategory.setText(selectedMeal.second, false)
        dropdownMealCategory.setOnItemClickListener { _, _, position, _ ->
            selectedMeal = mealOptions[position]
        }

        editServingsCount.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        editServingsCount.setText("1")
        editServingsCount.addTextChangedListener {
            updateServingPreview(
                selectedServing,
                editServingsCount,
                layoutServingsCount,
                macroPieChart,
                textCaloriesPreview,
                textCarbsPreview,
                textFatPreview,
                textProteinPreview
            )
        }

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        btnCancelFoodServing.setOnClickListener { dialog.dismiss() }
        btnAddFoodServing.setOnClickListener {
            val servings = editServingsCount.text?.toString()?.toFloatOrNull()
            if (servings == null || servings <= 0f) {
                layoutServingsCount.error = getString(R.string.servings_count_error)
                return@setOnClickListener
            }
            val totals = selectedServing.calculateTotals(servings)
            viewModel.logFood(date, selectedMeal.first, product, totals)
            dialog.dismiss()
            dismiss()
        }

        updateServingPreview(
            selectedServing,
            editServingsCount,
            layoutServingsCount,
            macroPieChart,
            textCaloriesPreview,
            textCarbsPreview,
            textFatPreview,
            textProteinPreview
        )
        dialog.show()
    }

    private fun updateServingPreview(
        serving: FoodServingOption,
        editServingsCount: TextInputEditText,
        layoutServingsCount: TextInputLayout,
        macroPieChart: MacroPieChartView,
        textCaloriesPreview: TextView,
        textCarbsPreview: TextView,
        textFatPreview: TextView,
        textProteinPreview: TextView
    ) {
        val servings = editServingsCount.text?.toString()?.toFloatOrNull()
        if (servings == null || servings <= 0f) {
            layoutServingsCount.error = getString(R.string.servings_count_error)
            macroPieChart.setMacros(0f, 0f, 0f)
            textCaloriesPreview.text = getString(R.string.kcal_unit, 0)
            textCarbsPreview.text = getString(R.string.carbs_preview, "0")
            textFatPreview.text = getString(R.string.fat_preview, "0")
            textProteinPreview.text = getString(R.string.protein_preview, "0")
            return
        }

        layoutServingsCount.error = null
        val totals = serving.calculateTotals(servings)
        macroPieChart.setMacros(totals.carbsG, totals.fatG, totals.proteinG)
        textCaloriesPreview.text = getString(R.string.kcal_unit, totals.calories)
        textCarbsPreview.text = getString(R.string.carbs_preview, formatGrams(totals.carbsG))
        textFatPreview.text = getString(R.string.fat_preview, formatGrams(totals.fatG))
        textProteinPreview.text = getString(R.string.protein_preview, formatGrams(totals.proteinG))
    }

    private fun formatGrams(value: Float): String {
        return if (value % 1f == 0f) {
            value.toInt().toString()
        } else {
            String.format(Locale.getDefault(), "%.1f", value)
        }
    }
}
