package com.example.mealmate2.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmate2.R
import com.example.mealmate2.ui.search.MacroPieChartView
import com.google.android.material.button.MaterialButtonToggleGroup
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MealDetailsFragment : Fragment() {

    private val viewModel: MealDetailsViewModel by viewModels()
    private val adapter = DiaryAdapter { viewModel.deleteEntry(it) }
    private val dateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault())

    private lateinit var textMealTitle: TextView
    private lateinit var textMealDate: TextView
    private lateinit var groupMealSelector: MaterialButtonToggleGroup
    private lateinit var macroPieChart: MacroPieChartView
    private lateinit var textCalories: TextView
    private lateinit var textCarbs: TextView
    private lateinit var textFat: TextView
    private lateinit var textProtein: TextView
    private lateinit var textEmpty: TextView

    private var suppressToggleCallback = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_meal_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val date = arguments?.getLong("date") ?: LocalDate.now().toEpochDay()
        val initialMealCategory = arguments?.getString("initialMealCategory") ?: "breakfast"
        viewModel.initialize(date, initialMealCategory)

        textMealTitle = view.findViewById(R.id.textMealTitle)
        textMealDate = view.findViewById(R.id.textMealDate)
        groupMealSelector = view.findViewById(R.id.groupMealSelector)
        macroPieChart = view.findViewById(R.id.macroPieChart)
        textCalories = view.findViewById(R.id.textCalories)
        textCarbs = view.findViewById(R.id.textCarbs)
        textFat = view.findViewById(R.id.textFat)
        textProtein = view.findViewById(R.id.textProtein)
        textEmpty = view.findViewById(R.id.textEmpty)

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            findNavController().navigateUp()
        }

        view.findViewById<RecyclerView>(R.id.rvMealFoods).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MealDetailsFragment.adapter
            isNestedScrollingEnabled = false
        }

        setupMealSelector()
        textMealDate.text = LocalDate.ofEpochDay(date).format(dateFormatter)
        observeViewModel()
    }

    private fun setupMealSelector() {
        groupMealSelector.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (suppressToggleCallback || !isChecked) return@addOnButtonCheckedListener
            val category = when (checkedId) {
                R.id.btnBreakfast -> "breakfast"
                R.id.btnLunch -> "lunch"
                R.id.btnDinner -> "dinner"
                R.id.btnSnacks -> "snacks"
                R.id.btnAllFood -> MealDetailsSummary.CATEGORY_ALL
                else -> return@addOnButtonCheckedListener
            }
            viewModel.selectMeal(category)
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            textMealTitle.text = state.title
            textCalories.text = "${state.totals.calories} kcal"
            textCarbs.text = "Carbs: ${formatGrams(state.totals.carbsG)}g"
            textFat.text = "Fat: ${formatGrams(state.totals.fatG)}g"
            textProtein.text = "Protein: ${formatGrams(state.totals.proteinG)}g"
            macroPieChart.setMacros(state.totals.carbsG, state.totals.fatG, state.totals.proteinG)

            adapter.submitList(state.entries)
            textEmpty.visibility = if (state.entries.isEmpty()) View.VISIBLE else View.GONE

            val selectedButtonId = when (state.selectedCategory) {
                "breakfast" -> R.id.btnBreakfast
                "lunch" -> R.id.btnLunch
                "dinner" -> R.id.btnDinner
                "snacks" -> R.id.btnSnacks
                MealDetailsSummary.CATEGORY_ALL -> R.id.btnAllFood
                else -> R.id.btnBreakfast
            }
            if (groupMealSelector.checkedButtonId != selectedButtonId) {
                suppressToggleCallback = true
                groupMealSelector.check(selectedButtonId)
                suppressToggleCallback = false
            }
        }
    }

    private fun formatGrams(value: Float): String {
        return if (value % 1f == 0f) {
            value.toInt().toString()
        } else {
            String.format(Locale.getDefault(), "%.1f", value)
        }
    }
}
