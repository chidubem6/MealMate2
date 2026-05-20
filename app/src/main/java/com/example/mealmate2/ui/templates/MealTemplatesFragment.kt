package com.example.mealmate2.ui.templates

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MealTemplatesFragment : Fragment() {

    private val viewModel: MealTemplatesViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_meal_templates, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val date = arguments?.getLong("date") ?: 0L
        val mealCategory = arguments?.getString("mealCategory") ?: ""
        val isLogMode = date != 0L && mealCategory.isNotBlank()

        view.findViewById<ImageButton>(R.id.btnCloseTemplates).setOnClickListener {
            findNavController().navigateUp()
        }

        val rvTemplates = view.findViewById<RecyclerView>(R.id.rvTemplates)
        val textEmptyTemplates = view.findViewById<TextView>(R.id.textEmptyTemplates)

        val adapter = MealTemplateAdapter(
            onLog = { templateWithItems ->
                if (isLogMode) {
                    viewModel.logTemplate(templateWithItems.template.id, date, mealCategory)
                    findNavController().navigateUp()
                }
            },
            onDelete = { templateWithItems ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Delete \"${templateWithItems.template.name}\"?")
                    .setMessage("This will permanently remove this template.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete") { _, _ -> viewModel.delete(templateWithItems.template.id) }
                    .show()
            }
        )

        rvTemplates.layoutManager = LinearLayoutManager(requireContext())
        rvTemplates.adapter = adapter

        viewModel.templates.observe(viewLifecycleOwner) { templates ->
            adapter.submitList(templates)
            textEmptyTemplates.visibility = if (templates.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
