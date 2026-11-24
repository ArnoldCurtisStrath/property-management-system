package com.example.propertymanagementsystem.ui.buildings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.data.ledger.BuildingSummary
import com.example.propertymanagementsystem.util.Money

class BuildingAdapter(
    private val onClick: (BuildingSummary) -> Unit
) : ListAdapter<BuildingSummary, BuildingAdapter.Holder>(DIFFERENCE) {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.buildingName)
        val owner: TextView = view.findViewById(R.id.buildingOwner)
        val address: TextView = view.findViewById(R.id.buildingAddress)
        val meta: TextView = view.findViewById(R.id.buildingMeta)
        val arrears: TextView = view.findViewById(R.id.buildingArrears)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_building, parent, false)
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val summary = getItem(position)
        val building = summary.building
        val context = holder.itemView.context

        holder.name.text = building.name
        holder.owner.text = if (building.ownerName.isBlank()) "No owner set"
        else "Owner · ${building.ownerName}"
        holder.address.text = building.address.ifBlank { "No address" }
        holder.meta.text = listOf(
            building.type.ifBlank { "Building" },
            "${summary.occupiedCount} of ${summary.unitCount} occupied"
        ).joinToString(" · ").uppercase()
        holder.arrears.text = Money.format(summary.arrears)
        holder.arrears.setTextColor(
            ContextCompat.getColor(
                context,
                if (summary.arrears > 0) R.color.stamp else R.color.paid
            )
        )

        holder.itemView.setOnClickListener { onClick(summary) }
    }

    companion object {
        private val DIFFERENCE = object : DiffUtil.ItemCallback<BuildingSummary>() {
            override fun areItemsTheSame(oldItem: BuildingSummary, newItem: BuildingSummary): Boolean =
                oldItem.building.id == newItem.building.id

            override fun areContentsTheSame(oldItem: BuildingSummary, newItem: BuildingSummary): Boolean =
                oldItem == newItem
        }
    }
}
