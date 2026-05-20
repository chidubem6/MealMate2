package com.example.mealmate2.ui.foods

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmate2.R
import com.example.mealmate2.data.CustomFood

class CustomFoodAdapter(
    private val onDelete: (CustomFood) -> Unit
) : ListAdapter<CustomFood, CustomFoodAdapter.VH>(Diff) {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.textFoodName)
        val brand: TextView = view.findViewById(R.id.textBrand)
        val calories: TextView = view.findViewById(R.id.textCalories)
        val macros: TextView = view.findViewById(R.id.textMacros)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_food_log_entry, parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val food = getItem(position)
        holder.name.text = food.name
        holder.calories.text = "${food.calories} kcal"
        if (food.brand.isNotBlank()) {
            holder.brand.visibility = View.VISIBLE
            holder.brand.text = food.brand
        } else {
            holder.brand.visibility = View.GONE
        }
        holder.macros.text = "C:${food.carbsG.toInt()}g  F:${food.fatG.toInt()}g  P:${food.proteinG.toInt()}g  · ${food.servingSize}"
        holder.btnDelete.setOnClickListener { onDelete(food) }
    }

    object Diff : DiffUtil.ItemCallback<CustomFood>() {
        override fun areItemsTheSame(a: CustomFood, b: CustomFood) = a.id == b.id
        override fun areContentsTheSame(a: CustomFood, b: CustomFood) = a == b
    }
}
