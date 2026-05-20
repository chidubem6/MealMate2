package com.example.mealmate2.ui.templates

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmate2.R
import com.example.mealmate2.data.MealTemplateWithItems

class MealTemplateAdapter(
    private val onLog: (MealTemplateWithItems) -> Unit,
    private val onDelete: (MealTemplateWithItems) -> Unit
) : ListAdapter<MealTemplateWithItems, MealTemplateAdapter.VH>(Diff) {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.textTemplateName)
        val summary: TextView = view.findViewById(R.id.textTemplateSummary)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteTemplate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        LayoutInflater.from(parent.context).inflate(R.layout.item_meal_template, parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.name.text = item.template.name
        val totalKcal = item.items.sumOf { it.calories }
        holder.summary.text = "${item.items.size} items · $totalKcal kcal"
        holder.itemView.setOnClickListener { onLog(item) }
        holder.btnDelete.setOnClickListener { onDelete(item) }
    }

    object Diff : DiffUtil.ItemCallback<MealTemplateWithItems>() {
        override fun areItemsTheSame(a: MealTemplateWithItems, b: MealTemplateWithItems) =
            a.template.id == b.template.id
        override fun areContentsTheSame(a: MealTemplateWithItems, b: MealTemplateWithItems) =
            a == b
    }
}
