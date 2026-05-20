package com.example.mealmate2.ui.foods

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
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CustomFoodsFragment : Fragment() {

    private val viewModel: CustomFoodsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_custom_foods, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ImageButton>(R.id.btnCloseCustomFoods).setOnClickListener {
            findNavController().navigateUp()
        }

        val rvCustomFoods = view.findViewById<RecyclerView>(R.id.rvCustomFoods)
        val textEmptyFoods = view.findViewById<TextView>(R.id.textEmptyFoods)
        val fabAddFood = view.findViewById<FloatingActionButton>(R.id.fabAddFood)

        val adapter = CustomFoodAdapter { food ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete ${food.name}?")
                .setMessage("This will permanently remove this custom food.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete") { _, _ -> viewModel.delete(food.id) }
                .show()
        }

        rvCustomFoods.layoutManager = LinearLayoutManager(requireContext())
        rvCustomFoods.adapter = adapter

        viewModel.foods.observe(viewLifecycleOwner) { foods ->
            adapter.submitList(foods)
            textEmptyFoods.visibility = if (foods.isEmpty()) View.VISIBLE else View.GONE
        }

        fabAddFood.setOnClickListener {
            findNavController().navigate(R.id.action_customFoods_to_create)
        }
    }
}
