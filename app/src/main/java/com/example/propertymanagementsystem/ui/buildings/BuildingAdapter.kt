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
import com.google.android.material.progressindicator.LinearProgressIndicator

class BuildingAdapter(
    private val onClick: (BuildingSummary) -> Unit
) : ListAdapter<BuildingSummary, BuildingAdapter.Holder>(DIFFERENCE) {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.buildingName)
        val owner: TextView = view.findViewById(R.id.buildingOwner)
        val type: TextView = view.findViewById(R.id.buildingType)
        val address: TextView = view.findViewById(R.id.buildingAddress)
        val occupancy: LinearProgressIndicator = view.findViewById(R.id.occupancyProgress)
        val occupancyLabel: TextView = view.findViewById(R.id.occupancyLabel)
        val collected: TextView = view.findViewById(R.id.collectedValue)
        val arrears: TextView = view.findViewById(R.id.arrearsValue)
        val net: TextView = view.findViewById(R.id.netValue)
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
        holder.type.text = building.type.uppercase()
        holder.address.text = building.address.ifBlank { "No address" }
        holder.occupancy.max = 100
        holder.occupancy.setProgressCompat((summary.occupancyRate * 100).toInt(), true)
        holder.occupancyLabel.text = "${(summary.occupancyRate * 100).toInt()}% · " +
            "${summary.occupiedCount}/${summary.unitCount}"
        holder.collected.text = Money.format(summary.collected)
        holder.arrears.text = Money.format(summary.arrears)
        holder.net.text = Money.format(summary.netToOwner)

        holder.arrears.setTextColor(
            ContextCompat.getColor(
                context,
                if (summary.arrears > 0) R.color.danger else R.color.success
            )
        )
        holder.net.setTextColor(
            ContextCompat.getColor(
                context,
                if (summary.netToOwner < 0) R.color.danger else R.color.success
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
