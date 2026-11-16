package com.example.propertymanagementsystem.ui.maintenance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.data.model.MaintenanceRequest

/**
 * A request together with the name of the property it belongs to. It is only
 * used for the list.
 */
data class MaintenanceRow(
    val request: MaintenanceRequest,
    val propertyName: String
)

/**
 * Fills in the rows of the maintenance list.
 */
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
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaintenanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_maintenance, parent, false)
        return MaintenanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: MaintenanceViewHolder, position: Int) {
        val row = getItem(position)

        holder.title.text = row.request.title
        holder.description.text = row.request.description
        holder.property.text = row.propertyName
        holder.category.text = row.request.category
        holder.status.text = row.request.status

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
