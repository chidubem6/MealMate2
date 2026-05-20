package com.example.mealmate2.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.mealmate2.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

private const val ML_PER_OZ = 29.5735f

class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            view?.findViewById<MaterialSwitch>(R.id.switchWeightReminder)?.isChecked = false
            Toast.makeText(requireContext(), "Notification permission needed for reminders", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editCalorieGoal = view.findViewById<TextInputEditText>(R.id.editCalorieGoal)
        val editCarbGoal = view.findViewById<TextInputEditText>(R.id.editCarbGoal)
        val editFatGoal = view.findViewById<TextInputEditText>(R.id.editFatGoal)
        val editProteinGoal = view.findViewById<TextInputEditText>(R.id.editProteinGoal)
        val layoutCalorieGoal = view.findViewById<TextInputLayout>(R.id.layoutCalorieGoal)
        val layoutCarbGoal = view.findViewById<TextInputLayout>(R.id.layoutCarbGoal)
        val layoutFatGoal = view.findViewById<TextInputLayout>(R.id.layoutFatGoal)
        val layoutProteinGoal = view.findViewById<TextInputLayout>(R.id.layoutProteinGoal)
        val textCarbGoalGrams = view.findViewById<TextView>(R.id.textCarbGoalGrams)
        val textFatGoalGrams = view.findViewById<TextView>(R.id.textFatGoalGrams)
        val textProteinGoalGrams = view.findViewById<TextView>(R.id.textProteinGoalGrams)
        val editStartingWeight = view.findViewById<TextInputEditText>(R.id.editStartingWeight)
        val switchWeightReminder = view.findViewById<MaterialSwitch>(R.id.switchWeightReminder)
        val editReminderTime = view.findViewById<TextInputEditText>(R.id.editReminderTime)
        val btnSaveSettings = view.findViewById<MaterialButton>(R.id.btnSaveSettings)
        val btnMyCustomFoods = view.findViewById<MaterialButton>(R.id.btnMyCustomFoods)
        val btnMealTemplates = view.findViewById<MaterialButton>(R.id.btnMealTemplates)
        val textKeyMetrics = view.findViewById<TextView>(R.id.textKeyMetrics)
        val switchWaterUnit = view.findViewById<MaterialSwitch>(R.id.switchWaterUnit)
        val editWaterGoal = view.findViewById<TextInputEditText>(R.id.editWaterGoal)
        val layoutWaterGoal = view.findViewById<TextInputLayout>(R.id.layoutWaterGoal)

        btnMyCustomFoods.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_customFoods)
        }
        btnMealTemplates.setOnClickListener {
            findNavController().navigate(R.id.action_settings_to_templates)
        }
        view.findViewById<MaterialButton>(R.id.btnNhsEatwellGuide).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.nhs.uk/live-well/eat-well/food-guidelines-and-food-labels/the-eatwell-guide/"))
            startActivity(intent)
        }

        editCalorieGoal.setText(viewModel.calorieGoal.toString())
        editCarbGoal.setText(viewModel.carbGoalPercent.toString())
        editFatGoal.setText(viewModel.fatGoalPercent.toString())
        editProteinGoal.setText(viewModel.proteinGoalPercent.toString())
        if (viewModel.startingWeightKg > 0f) {
            editStartingWeight.setText(viewModel.startingWeightKg.toString())
        }
        switchWeightReminder.isChecked = viewModel.weightReminderEnabled
        editReminderTime.setText(viewModel.weightReminderTime)

        // Water settings — populate first, attach listener after to avoid spurious conversion
        val isOzInitial = viewModel.waterUnitIsOz
        val goalMl = viewModel.dailyWaterGoalMl
        editWaterGoal.setText(
            if (isOzInitial) "%.1f".format(goalMl / ML_PER_OZ) else goalMl.toInt().toString()
        )
        layoutWaterGoal.hint = if (isOzInitial) "Daily water goal (oz)" else "Daily water goal (ml)"
        switchWaterUnit.isChecked = isOzInitial

        switchWaterUnit.setOnCheckedChangeListener { _, nowOz ->
            val current = editWaterGoal.text?.toString()?.toFloatOrNull() ?: return@setOnCheckedChangeListener
            // nowOz=true means we just switched TO oz, so current value was in ml
            val newValue = if (nowOz) current / ML_PER_OZ else current * ML_PER_OZ
            editWaterGoal.setText(if (nowOz) "%.1f".format(newValue) else newValue.toInt().toString())
            layoutWaterGoal.hint = if (nowOz) "Daily water goal (oz)" else "Daily water goal (ml)"
        }

        switchWeightReminder.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        fun updateMacroGramLabels() {
            val calorieGoal = editCalorieGoal.text?.toString()?.toIntOrNull()
            val carbPercent = editCarbGoal.text?.toString()?.toIntOrNull()
            val fatPercent = editFatGoal.text?.toString()?.toIntOrNull()
            val proteinPercent = editProteinGoal.text?.toString()?.toIntOrNull()

            if (calorieGoal == null || calorieGoal <= 0 ||
                carbPercent == null || fatPercent == null || proteinPercent == null
            ) {
                textCarbGoalGrams.text = "—"
                textFatGoalGrams.text = "—"
                textProteinGoalGrams.text = "—"
                return
            }

            val grams = viewModel.calculateMacroGrams(
                calorieGoal,
                carbPercent,
                fatPercent,
                proteinPercent
            )
            textCarbGoalGrams.text = "${grams.carbsG}g"
            textFatGoalGrams.text = "${grams.fatG}g"
            textProteinGoalGrams.text = "${grams.proteinG}g"
        }

        listOf(editCalorieGoal, editCarbGoal, editFatGoal, editProteinGoal).forEach { input ->
            input.addTextChangedListener { updateMacroGramLabels() }
        }
        updateMacroGramLabels()

        viewModel.latestWeight.observe(viewLifecycleOwner) { latest ->
            val startKg = viewModel.startingWeightKg.takeIf { it > 0f }
            val currentKg = latest?.weightKg
            if (startKg != null && currentKg != null) {
                val change = currentKg - startKg
                val sign = if (change >= 0) "+" else ""
                textKeyMetrics.text = "$sign%.1f kg".format(change)
            } else {
                textKeyMetrics.text = "—"
            }
        }

        btnSaveSettings.setOnClickListener {
            val calorieGoal = editCalorieGoal.text?.toString()?.toIntOrNull()
            val carbGoal = editCarbGoal.text?.toString()?.toIntOrNull()
            val fatGoal = editFatGoal.text?.toString()?.toIntOrNull()
            val proteinGoal = editProteinGoal.text?.toString()?.toIntOrNull()
            val startingWeight = editStartingWeight.text?.toString()?.toFloatOrNull() ?: 0f
            val reminderEnabled = switchWeightReminder.isChecked
            val reminderTime = editReminderTime.text?.toString()?.trim() ?: "08:00"

            layoutCalorieGoal.error = null
            layoutCarbGoal.error = null
            layoutFatGoal.error = null
            layoutProteinGoal.error = null

            var hasInputError = false
            if (calorieGoal == null) {
                layoutCalorieGoal.error = "Enter a calorie goal"
                hasInputError = true
            }
            if (carbGoal == null) {
                layoutCarbGoal.error = "Enter carbs percentage"
                hasInputError = true
            }
            if (fatGoal == null) {
                layoutFatGoal.error = "Enter fat percentage"
                hasInputError = true
            }
            if (proteinGoal == null) {
                layoutProteinGoal.error = "Enter protein percentage"
                hasInputError = true
            }
            if (hasInputError) return@setOnClickListener

            val parsedCalorieGoal = calorieGoal!!
            val parsedCarbGoal = carbGoal!!
            val parsedFatGoal = fatGoal!!
            val parsedProteinGoal = proteinGoal!!
            val validationError = viewModel.validateMacroGoals(
                parsedCalorieGoal,
                parsedCarbGoal,
                parsedFatGoal,
                parsedProteinGoal
            )
            if (validationError != null) {
                when {
                    parsedCalorieGoal <= 0 -> layoutCalorieGoal.error = validationError
                    parsedCarbGoal < 0 -> layoutCarbGoal.error = validationError
                    parsedFatGoal < 0 -> layoutFatGoal.error = validationError
                    parsedProteinGoal < 0 -> layoutProteinGoal.error = validationError
                    else -> {
                        layoutCarbGoal.error = validationError
                        layoutFatGoal.error = validationError
                        layoutProteinGoal.error = validationError
                    }
                }
                return@setOnClickListener
            }

            val isOz = switchWaterUnit.isChecked
            val waterGoalInput = editWaterGoal.text?.toString()?.toFloatOrNull() ?: 0f
            val waterGoalMl = if (isOz) waterGoalInput * ML_PER_OZ else waterGoalInput

            viewModel.saveSettings(
                calorieGoal = parsedCalorieGoal,
                carbGoalPercent = parsedCarbGoal,
                fatGoalPercent = parsedFatGoal,
                proteinGoalPercent = parsedProteinGoal,
                startingWeightKg = startingWeight,
                weightReminderEnabled = reminderEnabled,
                weightReminderTime = reminderTime,
                waterGoalMl = waterGoalMl.coerceAtLeast(1f),
                waterUnitIsOz = isOz
            )
            Toast.makeText(requireContext(), "Settings saved", Toast.LENGTH_SHORT).show()
        }
    }
}
