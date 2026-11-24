package com.example.propertymanagementsystem.ui.maintenance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.data.model.MaintenanceRequest
import com.example.propertymanagementsystem.util.Money

data class MaintenanceRow(
    val request: MaintenanceRequest,
    val propertyName: String
)

class MaintenanceAdapter(
    private val onClick: (MaintenanceRow) -> Unit,
    private val onDelete: (MaintenanceRow) -> Unit
) : ListAdapter<MaintenanceRow, MaintenanceAdapter.MaintenanceViewHolder>(DIFFERENCE) {

    class MaintenanceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.requestTitle)
        val description: TextView = view.findViewById(R.id.requestDescription)
        val property: TextView = view.findViewById(R.id.requestProperty)
        val category: TextView = view.findViewById(R.id.requestCategory)
        val status: TextView = view.findViewById(R.id.requestStatus)
        val priority: TextView = view.findViewById(R.id.requestPriority)
        val cost: TextView = view.findViewById(R.id.requestCost)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaintenanceViewHolder {
        return MaintenanceViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_maintenance, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MaintenanceViewHolder, position: Int) {
        val row = getItem(position)
        val request = row.request
        val context = holder.itemView.context

        holder.title.text = request.title
        holder.description.text = request.description
        holder.property.text = row.propertyName
        holder.category.text = request.category
        holder.priority.text = request.priority.uppercase()
        holder.status.text = request.status.uppercase()

        when (request.status) {
            MaintenanceRequest.STATUS_DONE -> {
                holder.status.setBackgroundResource(R.drawable.bg_chip_paid)
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.success))
            }
            MaintenanceRequest.STATUS_IN_PROGRESS -> {
                holder.status.setBackgroundResource(R.drawable.bg_chip_current)
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.gold_dark))
            }
            else -> {
                holder.status.setBackgroundResource(R.drawable.bg_chip_overdue)
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.danger))
            }
        }

        if (request.cost > 0) {
            holder.cost.visibility = View.VISIBLE
            holder.cost.text = Money.format(request.cost)
        } else {
            holder.cost.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onClick(row) }
        holder.deleteButton.setOnClickListener { onDelete(row) }
    }

    companion object {
        private val DIFFERENCE = object : DiffUtil.ItemCallback<MaintenanceRow>() {
            override fun areItemsTheSame(oldItem: MaintenanceRow, newItem: MaintenanceRow): Boolean =
                oldItem.request.id == newItem.request.id

            override fun areContentsTheSame(oldItem: MaintenanceRow, newItem: MaintenanceRow): Boolean =
                oldItem == newItem
        }
    }
}
