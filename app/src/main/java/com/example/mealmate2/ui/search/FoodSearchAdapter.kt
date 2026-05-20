package com.example.mealmate2.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmate2.R
import com.example.mealmate2.network.FoodProduct

class FoodSearchAdapter(
    private val onItemClick: (FoodProduct) -> Unit
) : ListAdapter<FoodProduct, FoodSearchAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textFoodName: TextView = view.findViewById(R.id.textFoodName)
        val textBrand: TextView = view.findViewById(R.id.textBrand)
        val textCaloriesPer100g: TextView = view.findViewById(R.id.textCaloriesPer100g)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food_search_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = getItem(position)
        holder.textFoodName.text = product.displayName
        if (product.displayBrand.isNotBlank()) {
            holder.textBrand.visibility = View.VISIBLE
            holder.textBrand.text = product.displayBrand
        } else {
            holder.textBrand.visibility = View.GONE
        }
        holder.textCaloriesPer100g.text = "${product.caloriesPer100g} kcal/100g"
        holder.itemView.setOnClickListener { onItemClick(product) }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<FoodProduct>() {
            override fun areItemsTheSame(a: FoodProduct, b: FoodProduct) =
                a.id == b.id || (a.name == b.name && a.brand == b.brand)

            override fun areContentsTheSame(a: FoodProduct, b: FoodProduct) = a == b
        }
    }
}
