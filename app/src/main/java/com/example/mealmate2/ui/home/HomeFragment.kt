package com.example.mealmate2.ui.home

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmate2.R
import com.example.mealmate2.ui.widget.CalorieRingView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var textDate: TextView
    private lateinit var textGreeting: TextView
    private lateinit var calorieRing: CalorieRingView
    private lateinit var textCaloriesSummary: TextView
    private lateinit var textCarbsSummary: TextView
    private lateinit var textFatSummary: TextView
    private lateinit var textProteinSummary: TextView
    private lateinit var carbsProgress: LinearProgressIndicator
    private lateinit var fatProgress: LinearProgressIndicator
    private lateinit var proteinProgress: LinearProgressIndicator
    private lateinit var rvBreakfast: RecyclerView
    private lateinit var rvLunch: RecyclerView
    private lateinit var rvDinner: RecyclerView
    private lateinit var rvSnacks: RecyclerView
    private lateinit var btnAddBreakfast: MaterialButton
    private lateinit var btnAddLunch: MaterialButton
    private lateinit var btnAddDinner: MaterialButton
    private lateinit var btnAddSnacks: MaterialButton
    private lateinit var textBreakfastCalories: TextView
    private lateinit var textLunchCalories: TextView
    private lateinit var textDinnerCalories: TextView
    private lateinit var textSnacksCalories: TextView
    private lateinit var editWeight: TextInputEditText
    private lateinit var editNotes: TextInputEditText
    private lateinit var btnSaveWeightNotes: MaterialButton
    private lateinit var textWaterCount: TextView
    private lateinit var waterProgress: LinearProgressIndicator
    private lateinit var btnWaterAdd150: MaterialButton
    private lateinit var btnWaterAdd250: MaterialButton
    private lateinit var btnWaterAdd330: MaterialButton
    private lateinit var btnWaterAdd500: MaterialButton
    private lateinit var btnWaterCustom: MaterialButton
    private lateinit var btnWaterUndo: MaterialButton

    private val breakfastAdapter = DiaryAdapter { viewModel.deleteEntry(it) }
    private val lunchAdapter = DiaryAdapter { viewModel.deleteEntry(it) }
    private val dinnerAdapter = DiaryAdapter { viewModel.deleteEntry(it) }
    private val snacksAdapter = DiaryAdapter { viewModel.deleteEntry(it) }

    private val dateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault())

    private val quickAmountsMl = listOf(150f, 250f, 330f, 500f)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textDate = view.findViewById(R.id.textDate)
        textGreeting = view.findViewById(R.id.textGreeting)
        calorieRing = view.findViewById(R.id.calorieRing)
        textCaloriesSummary = view.findViewById(R.id.textCaloriesSummary)
        textCarbsSummary = view.findViewById(R.id.textCarbsSummary)
        textFatSummary = view.findViewById(R.id.textFatSummary)
        textProteinSummary = view.findViewById(R.id.textProteinSummary)
        carbsProgress = view.findViewById(R.id.carbsProgress)
        fatProgress = view.findViewById(R.id.fatProgress)
        proteinProgress = view.findViewById(R.id.proteinProgress)
        rvBreakfast = view.findViewById(R.id.rvBreakfast)
        rvLunch = view.findViewById(R.id.rvLunch)
        rvDinner = view.findViewById(R.id.rvDinner)
        rvSnacks = view.findViewById(R.id.rvSnacks)
        btnAddBreakfast = view.findViewById(R.id.btnAddBreakfast)
        btnAddLunch = view.findViewById(R.id.btnAddLunch)
        btnAddDinner = view.findViewById(R.id.btnAddDinner)
        btnAddSnacks = view.findViewById(R.id.btnAddSnacks)
        textBreakfastCalories = view.findViewById(R.id.textBreakfastCalories)
        textLunchCalories = view.findViewById(R.id.textLunchCalories)
        textDinnerCalories = view.findViewById(R.id.textDinnerCalories)
        textSnacksCalories = view.findViewById(R.id.textSnacksCalories)
        editWeight = view.findViewById(R.id.editWeight)
        editNotes = view.findViewById(R.id.editNotes)
        btnSaveWeightNotes = view.findViewById(R.id.btnSaveWeightNotes)
        textWaterCount = view.findViewById(R.id.textWaterCount)
        waterProgress = view.findViewById(R.id.waterProgress)
        btnWaterAdd150 = view.findViewById(R.id.btnWaterAdd150)
        btnWaterAdd250 = view.findViewById(R.id.btnWaterAdd250)
        btnWaterAdd330 = view.findViewById(R.id.btnWaterAdd330)
        btnWaterAdd500 = view.findViewById(R.id.btnWaterAdd500)
        btnWaterCustom = view.findViewById(R.id.btnWaterCustom)
        btnWaterUndo = view.findViewById(R.id.btnWaterUndo)

        val hour = LocalTime.now().hour
        textGreeting.text = when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }

        val quickButtons = listOf(btnWaterAdd150, btnWaterAdd250, btnWaterAdd330, btnWaterAdd500)
        quickButtons.zip(quickAmountsMl).forEach { (btn, ml) ->
            btn.setOnClickListener { viewModel.addWater(ml) }
        }
        btnWaterCustom.setOnClickListener { showCustomWaterDialog() }
        btnWaterUndo.setOnClickListener { viewModel.undoLastWater() }

        setupRecyclerViews()
        setupNavButtons(view)
        setupAddButtons()
        setupMealDetailCards(view)
        setupSaveButton()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        listOf(
            rvBreakfast to breakfastAdapter,
            rvLunch to lunchAdapter,
            rvDinner to dinnerAdapter,
            rvSnacks to snacksAdapter
        ).forEach { (rv, adapter) ->
            rv.layoutManager = LinearLayoutManager(requireContext())
            rv.adapter = adapter
            rv.isNestedScrollingEnabled = false
        }
    }

    private fun setupNavButtons(view: View) {
        view.findViewById<ImageButton>(R.id.btnPrevDay).setOnClickListener {
            viewModel.goToPreviousDay()
        }
        view.findViewById<ImageButton>(R.id.btnNextDay).setOnClickListener {
            viewModel.goToNextDay()
        }
        textDate.setOnClickListener { showDatePicker() }
    }

    private fun showDatePicker() {
        val current = viewModel.state.value?.date ?: LocalDate.now()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                viewModel.goToDate(LocalDate.of(year, month + 1, day))
            },
            current.year,
            current.monthValue - 1,
            current.dayOfMonth
        ).show()
    }

    private fun setupAddButtons() {
        btnAddBreakfast.setOnClickListener { openFoodSearch("breakfast") }
        btnAddLunch.setOnClickListener { openFoodSearch("lunch") }
        btnAddDinner.setOnClickListener { openFoodSearch("dinner") }
        btnAddSnacks.setOnClickListener { openFoodSearch("snacks") }
    }

    private fun setupMealDetailCards(view: View) {
        val meals = listOf(
            R.id.cardBreakfast to "breakfast",
            R.id.cardLunch to "lunch",
            R.id.cardDinner to "dinner",
            R.id.cardSnacks to "snacks"
        )
        meals.forEach { (cardId, category) ->
            val card = view.findViewById<MaterialCardView>(cardId)
            card.setOnClickListener { openMealDetails(category) }
            card.setOnLongClickListener {
                val entries = when (category) {
                    "breakfast" -> viewModel.state.value?.breakfastEntries
                    "lunch" -> viewModel.state.value?.lunchEntries
                    "dinner" -> viewModel.state.value?.dinnerEntries
                    else -> viewModel.state.value?.snacksEntries
                } ?: emptyList()
                if (entries.isEmpty()) {
                    Toast.makeText(requireContext(), "No items to save as template", Toast.LENGTH_SHORT).show()
                } else {
                    showSaveTemplateDialog(entries)
                }
                true
            }
        }
    }

    private fun showSaveTemplateDialog(entries: List<com.example.mealmate2.data.FoodLogEntry>) {
        val input = EditText(requireContext()).apply {
            hint = "Template name"
            setPadding(48, 24, 48, 24)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Save as template")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewModel.saveTemplate(name, entries)
                    Toast.makeText(requireContext(), "Template saved", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCustomWaterDialog() {
        val isOz = viewModel.state.value?.waterUnitIsOz == true
        val unit = if (isOz) "oz" else "ml"
        val editText = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            hint = "Amount in $unit"
            setPadding(48, 24, 48, 24)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Add water ($unit)")
            .setView(editText)
            .setPositiveButton("Add") { _, _ ->
                val value = editText.text.toString().toFloatOrNull() ?: return@setPositiveButton
                val ml = if (isOz) value * ML_PER_OZ else value
                viewModel.addWater(ml)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openFoodSearch(category: String) {
        val date = viewModel.state.value?.date?.toEpochDay() ?: LocalDate.now().toEpochDay()
        val bundle = bundleOf(
            "date" to date,
            "mealCategory" to category
        )
        findNavController().navigate(R.id.action_home_to_foodSearch, bundle)
    }

    private fun openMealDetails(category: String) {
        val date = viewModel.state.value?.date?.toEpochDay() ?: LocalDate.now().toEpochDay()
        val bundle = bundleOf(
            "date" to date,
            "initialMealCategory" to category
        )
        findNavController().navigate(R.id.action_home_to_mealDetails, bundle)
    }

    private fun setupSaveButton() {
        btnSaveWeightNotes.setOnClickListener {
            val weightText = editWeight.text?.toString()?.trim()
            val weightKg = weightText?.toFloatOrNull()
            val note = editNotes.text?.toString()?.trim() ?: ""
            viewModel.saveWeightAndNote(weightKg, note)
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            textDate.text = state.date.format(dateFormatter)

            val remaining = (state.calorieGoal - state.totalCalories).coerceAtLeast(0)
            calorieRing.ringProgress = state.totalCalories.toFloat() / state.calorieGoal.coerceAtLeast(1)
            calorieRing.centerText = remaining.toString()
            textCaloriesSummary.text = "Eaten: ${state.totalCalories} / Goal: ${state.calorieGoal} kcal"

            textCarbsSummary.text = "${state.totalCarbsG.toInt()}g / ${state.carbGoalG}g"
            carbsProgress.max = state.carbGoalG.coerceAtLeast(1)
            carbsProgress.progress = state.totalCarbsG.toInt().coerceIn(0, state.carbGoalG)

            textFatSummary.text = "${state.totalFatG.toInt()}g / ${state.fatGoalG}g"
            fatProgress.max = state.fatGoalG.coerceAtLeast(1)
            fatProgress.progress = state.totalFatG.toInt().coerceIn(0, state.fatGoalG)

            textProteinSummary.text = "${state.totalProteinG.toInt()}g / ${state.proteinGoalG}g"
            proteinProgress.max = state.proteinGoalG.coerceAtLeast(1)
            proteinProgress.progress = state.totalProteinG.toInt().coerceIn(0, state.proteinGoalG)

            breakfastAdapter.submitList(state.breakfastEntries)
            lunchAdapter.submitList(state.lunchEntries)
            dinnerAdapter.submitList(state.dinnerEntries)
            snacksAdapter.submitList(state.snacksEntries)

            textBreakfastCalories.text = "${state.breakfastEntries.sumOf { it.calories }} kcal"
            textLunchCalories.text = "${state.lunchEntries.sumOf { it.calories }} kcal"
            textDinnerCalories.text = "${state.dinnerEntries.sumOf { it.calories }} kcal"
            textSnacksCalories.text = "${state.snacksEntries.sumOf { it.calories }} kcal"

            editWeight.setText(state.weightKg?.toString() ?: "")
            editNotes.setText(state.note)

            updateWaterCard(state)
        }
    }

    private fun updateWaterCard(state: HomeUiState) {
        val isOz = state.waterUnitIsOz
        val unit = if (isOz) "oz" else "ml"

        val currentDisplay = if (isOz) "%.1f".format(state.waterMl / ML_PER_OZ)
                             else state.waterMl.toInt().toString()
        val goalDisplay = if (isOz) "%.1f".format(state.waterGoalMl / ML_PER_OZ)
                          else state.waterGoalMl.toInt().toString()
        textWaterCount.text = "$currentDisplay / $goalDisplay $unit"

        waterProgress.max = state.waterGoalMl.toInt().coerceAtLeast(1)
        waterProgress.progress = state.waterMl.toInt().coerceIn(0, state.waterGoalMl.toInt())

        val quickButtons = listOf(btnWaterAdd150, btnWaterAdd250, btnWaterAdd330, btnWaterAdd500)
        quickButtons.zip(quickAmountsMl).forEach { (btn, ml) ->
            btn.text = if (isOz) "+%.0f $unit".format(ml / ML_PER_OZ) else "+${ml.toInt()} $unit"
        }
    }

    companion object {
        private const val ML_PER_OZ = 29.5735f
    }
}
