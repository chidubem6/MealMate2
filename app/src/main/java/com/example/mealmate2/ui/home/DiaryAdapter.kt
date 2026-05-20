package com.example.mealmate2.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmate2.R
import com.example.mealmate2.data.FoodLogEntry

class DiaryAdapter(
    private val onDelete: (FoodLogEntry) -> Unit
) : ListAdapter<FoodLogEntry, DiaryAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textFoodName: TextView = view.findViewById(R.id.textFoodName)
        val textBrand: TextView = view.findViewById(R.id.textBrand)
        val textCalories: TextView = view.findViewById(R.id.textCalories)
        val textMacros: TextView = view.findViewById(R.id.textMacros)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food_log_entry, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = getItem(position)
        holder.textFoodName.text = entry.foodName
        if (entry.brand.isNotBlank()) {
            holder.textBrand.visibility = View.VISIBLE
            holder.textBrand.text = entry.brand
        } else {
            holder.textBrand.visibility = View.GONE
        }
        holder.textCalories.text = "${entry.calories} kcal"
        holder.textMacros.text = "C:${entry.carbsG.toInt()}g  F:${entry.fatG.toInt()}g  P:${entry.proteinG.toInt()}g"
        holder.btnDelete.setOnClickListener { onDelete(entry) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<FoodLogEntry>() {
            override fun areItemsTheSame(a: FoodLogEntry, b: FoodLogEntry) = a.id == b.id
            override fun areContentsTheSame(a: FoodLogEntry, b: FoodLogEntry) = a == b
        }
    }
}
