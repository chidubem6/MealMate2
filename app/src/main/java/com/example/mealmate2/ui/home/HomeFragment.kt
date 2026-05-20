package com.example.mealmate2.ui.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
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
import com.google.android.material.textfield.TextInputLayout
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("NewApi")
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
    private lateinit var textBreakfastCalories: TextView
    private lateinit var textLunchCalories: TextView
    private lateinit var textDinnerCalories: TextView
    private lateinit var textSnacksCalories: TextView
    private lateinit var editWeight: TextInputEditText
    private lateinit var editNotes: TextInputEditText
    private lateinit var btnSaveWeightNotes: MaterialButton
    private lateinit var layoutWaterInput: TextInputLayout
    private lateinit var editWaterAmount: TextInputEditText
    private lateinit var waterProgress: LinearProgressIndicator
    private lateinit var textWaterGoal: TextView
    private lateinit var btnWaterAdd150: MaterialButton
    private lateinit var btnWaterAdd250: MaterialButton
    private lateinit var btnWaterAdd330: MaterialButton
    private lateinit var btnWaterAdd500: MaterialButton

    private val quickAmountsMl = listOf(150f, 250f, 330f, 500f)

    private val breakfastAdapter = DiaryAdapter { viewModel.deleteEntry(it) }
    private val lunchAdapter = DiaryAdapter { viewModel.deleteEntry(it) }
    private val dinnerAdapter = DiaryAdapter { viewModel.deleteEntry(it) }
    private val snacksAdapter = DiaryAdapter { viewModel.deleteEntry(it) }

    private val dateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault())

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
        textBreakfastCalories = view.findViewById(R.id.textBreakfastCalories)
        textLunchCalories = view.findViewById(R.id.textLunchCalories)
        textDinnerCalories = view.findViewById(R.id.textDinnerCalories)
        textSnacksCalories = view.findViewById(R.id.textSnacksCalories)
        editWeight = view.findViewById(R.id.editWeight)
        editNotes = view.findViewById(R.id.editNotes)
        btnSaveWeightNotes = view.findViewById(R.id.btnSaveWeightNotes)
        layoutWaterInput = view.findViewById(R.id.layoutWaterInput)
        editWaterAmount = view.findViewById(R.id.editWaterAmount)
        waterProgress = view.findViewById(R.id.waterProgress)
        textWaterGoal = view.findViewById(R.id.textWaterGoal)
        btnWaterAdd150 = view.findViewById(R.id.btnWaterAdd150)
        btnWaterAdd250 = view.findViewById(R.id.btnWaterAdd250)
        btnWaterAdd330 = view.findViewById(R.id.btnWaterAdd330)
        btnWaterAdd500 = view.findViewById(R.id.btnWaterAdd500)

        val hour = LocalTime.now().hour
        textGreeting.text = when {
            hour < 12 -> getString(R.string.good_morning)
            hour < 17 -> getString(R.string.good_afternoon)
            else -> getString(R.string.good_evening)
        }

        setupWaterInput()
        setupQuickAddButtons()
        setupRecyclerViews()
        setupNavButtons(view)
        setupMealCards(view)
        setupSaveButton()
        observeViewModel()
    }

    private fun setupQuickAddButtons() {
        val buttons = listOf(btnWaterAdd150, btnWaterAdd250, btnWaterAdd330, btnWaterAdd500)
        buttons.zip(quickAmountsMl).forEach { (btn, ml) ->
            btn.setOnClickListener { viewModel.addWater(ml) }
        }
    }

    private fun setupWaterInput() {
        editWaterAmount.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                commitWaterInput()
                true
            } else {
                false
            }
        }
        editWaterAmount.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) commitWaterInput()
        }
    }

    private fun commitWaterInput() {
        val isOz = viewModel.state.value?.waterUnitIsOz == true
        val text = editWaterAmount.text?.toString()?.trim() ?: return
        val value = text.toFloatOrNull() ?: return
        val ml = if (isOz) value * ML_PER_OZ else value
        viewModel.setWaterTotal(ml)
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

    private fun setupMealCards(view: View) {
        val meals = listOf(
            R.id.cardBreakfast to "breakfast",
            R.id.cardLunch to "lunch",
            R.id.cardDinner to "dinner",
            R.id.cardSnacks to "snacks"
        )
        meals.forEach { (cardId, category) ->
            val card = view.findViewById<MaterialCardView>(cardId)
            card.setOnClickListener { openFoodSearch(category) }
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

    private fun openFoodSearch(category: String) {
        val date = viewModel.state.value?.date?.toEpochDay() ?: LocalDate.now().toEpochDay()
        val bundle = Bundle().apply {
            putLong("date", date)
            putString("mealCategory", category)
        }
        findNavController().navigate(R.id.action_home_to_foodSearch, bundle)
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
            textCaloriesSummary.text = getString(R.string.calories_summary, state.totalCalories, state.calorieGoal)

            textCarbsSummary.text = getString(R.string.macro_summary, state.totalCarbsG.toInt(), state.carbGoalG)
            carbsProgress.max = state.carbGoalG.coerceAtLeast(1)
            carbsProgress.progress = state.totalCarbsG.toInt().coerceIn(0, state.carbGoalG)

            textFatSummary.text = getString(R.string.macro_summary, state.totalFatG.toInt(), state.fatGoalG)
            fatProgress.max = state.fatGoalG.coerceAtLeast(1)
            fatProgress.progress = state.totalFatG.toInt().coerceIn(0, state.fatGoalG)

            textProteinSummary.text = getString(R.string.macro_summary, state.totalProteinG.toInt(), state.proteinGoalG)
            proteinProgress.max = state.proteinGoalG.coerceAtLeast(1)
            proteinProgress.progress = state.totalProteinG.toInt().coerceIn(0, state.proteinGoalG)

            breakfastAdapter.submitList(state.breakfastEntries)
            lunchAdapter.submitList(state.lunchEntries)
            dinnerAdapter.submitList(state.dinnerEntries)
            snacksAdapter.submitList(state.snacksEntries)

            textBreakfastCalories.text = getString(R.string.kcal_unit, state.breakfastEntries.sumOf { it.calories })
            textLunchCalories.text = getString(R.string.kcal_unit, state.lunchEntries.sumOf { it.calories })
            textDinnerCalories.text = getString(R.string.kcal_unit, state.dinnerEntries.sumOf { it.calories })
            textSnacksCalories.text = getString(R.string.kcal_unit, state.snacksEntries.sumOf { it.calories })

            editWeight.setText(state.weightKg?.toString() ?: "")
            editNotes.setText(state.note)

            updateWaterCard(state)
        }
    }

    private fun updateWaterCard(state: HomeUiState) {
        val isOz = state.waterUnitIsOz
        val unit = if (isOz) "oz" else "ml"

        val goalDisplay = if (isOz) "%.1f".format(state.waterGoalMl / ML_PER_OZ)
                          else state.waterGoalMl.toInt().toString()
        textWaterGoal.text = getString(R.string.water_goal, goalDisplay, unit)
        layoutWaterInput.hint = getString(R.string.total_water_amount_with_unit, unit)

        waterProgress.max = state.waterGoalMl.toInt().coerceAtLeast(1)
        waterProgress.progress = state.waterMl.toInt().coerceIn(0, state.waterGoalMl.toInt())

        if (!editWaterAmount.isFocused) {
            val currentDisplay = if (isOz) "%.1f".format(state.waterMl / ML_PER_OZ)
                                 else state.waterMl.toInt().toString()
            editWaterAmount.setText(currentDisplay)
        }

        val buttons = listOf(btnWaterAdd150, btnWaterAdd250, btnWaterAdd330, btnWaterAdd500)
        buttons.zip(quickAmountsMl).forEach { (btn, ml) ->
            val amount = if (isOz) {
                String.format(Locale.getDefault(), "%.0f", ml / ML_PER_OZ)
            } else {
                ml.toInt().toString()
            }
            btn.text = getString(R.string.water_quick_add, amount, unit)
        }
    }

    companion object {
        private const val ML_PER_OZ = 29.5735f
    }
}
