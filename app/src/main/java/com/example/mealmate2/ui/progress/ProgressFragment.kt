package com.example.mealmate2.ui.progress

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.mealmate2.R
import com.example.mealmate2.ui.widget.WeightLineChartView
import com.google.android.material.progressindicator.LinearProgressIndicator

class ProgressFragment : Fragment() {

    private val viewModel: ProgressViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textAvgCalories = view.findViewById<TextView>(R.id.textAvgCalories)
        val calorieAvgProgress = view.findViewById<LinearProgressIndicator>(R.id.calorieAvgProgress)
        val textCalorieGoalHint = view.findViewById<TextView>(R.id.textCalorieGoalHint)
        val textCurrentWeight = view.findViewById<TextView>(R.id.textCurrentWeight)
        val textWeightChange = view.findViewById<TextView>(R.id.textWeightChange)
        val textStartingWeight = view.findViewById<TextView>(R.id.textStartingWeight)
        val weightChart = view.findViewById<WeightLineChartView>(R.id.weightChart)
        val textAvgCarbs = view.findViewById<TextView>(R.id.textAvgCarbs)
        val textAvgFat = view.findViewById<TextView>(R.id.textAvgFat)
        val textAvgProtein = view.findViewById<TextView>(R.id.textAvgProtein)
        val carbsAvgProgress = view.findViewById<LinearProgressIndicator>(R.id.carbsAvgProgress)
        val fatAvgProgress = view.findViewById<LinearProgressIndicator>(R.id.fatAvgProgress)
        val proteinAvgProgress = view.findViewById<LinearProgressIndicator>(R.id.proteinAvgProgress)

        val colorGreen = ContextCompat.getColor(requireContext(), R.color.green_700)
        val colorError = resolveThemeColor(android.R.attr.colorError)

        viewModel.state.observe(viewLifecycleOwner) { state ->
            // Calories
            textAvgCalories.text = state.weeklyAverageCalories.toInt().toString()
            textCalorieGoalHint.text = "Goal: ${state.calorieGoal} kcal"
            val calPct = ((state.weeklyAverageCalories / state.calorieGoal.coerceAtLeast(1)) * 100)
                .toInt().coerceIn(0, 100)
            calorieAvgProgress.progress = calPct

            // Weight
            textCurrentWeight.text = state.currentWeightKg?.let { "%.1f kg".format(it) } ?: "—"
            textStartingWeight.text = "Starting: ${state.startingWeightKg?.let { "%.1f kg".format(it) } ?: "—"}"
            val change = state.weightChangeKg
            if (change != null) {
                val sign = if (change >= 0) "+" else ""
                textWeightChange.text = "$sign%.1f kg".format(change)
                textWeightChange.setTextColor(if (change <= 0) colorGreen else colorError)
            } else {
                textWeightChange.text = ""
            }

            // Macros
            textAvgCarbs.text = "${state.weeklyAvgCarbsG.toInt()} g/day"
            textAvgFat.text = "${state.weeklyAvgFatG.toInt()} g/day"
            textAvgProtein.text = "${state.weeklyAvgProteinG.toInt()} g/day"

            carbsAvgProgress.progress = ((state.weeklyAvgCarbsG / state.carbGoalG.coerceAtLeast(1)) * 100)
                .toInt().coerceIn(0, 100)
            fatAvgProgress.progress = ((state.weeklyAvgFatG / state.fatGoalG.coerceAtLeast(1)) * 100)
                .toInt().coerceIn(0, 100)
            proteinAvgProgress.progress = ((state.weeklyAvgProteinG / state.proteinGoalG.coerceAtLeast(1)) * 100)
                .toInt().coerceIn(0, 100)

            weightChart.entries = state.weightHistory.map { it.date to it.weightKg }
        }
    }

    private fun resolveThemeColor(attr: Int): Int {
        val tv = TypedValue()
        requireContext().theme.resolveAttribute(attr, tv, true)
        return tv.data
    }
}
