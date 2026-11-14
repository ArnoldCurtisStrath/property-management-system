package com.example.propertymanagementsystem.ui.properties

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.data.model.Property

/**
 * Fills in the rows of the property list.
 *
 * ListAdapter works out which rows have changed, so the list does not have to
 * be built again every time something is added or removed.
 */
class PropertyAdapter(
    private val onClick: (Property) -> Unit,
    private val onEdit: (Property) -> Unit,
    private val onDelete: (Property) -> Unit
) : ListAdapter<Property, PropertyAdapter.PropertyViewHolder>(DIFFERENCE) {

    class PropertyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.propertyName)
        val address: TextView = view.findViewById(R.id.propertyAddress)
        val type: TextView = view.findViewById(R.id.propertyType)
        val rent: TextView = view.findViewById(R.id.propertyRent)
        val status: TextView = view.findViewById(R.id.propertyStatus)
        val editButton: ImageButton = view.findViewById(R.id.editButton)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_property, parent, false)
        return PropertyViewHolder(view)
    }

    // Puts the values of one property into the row.
    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = getItem(position)

        holder.name.text = property.name
        holder.address.text = property.address
        holder.type.text = property.type
        holder.rent.text = holder.itemView.context.getString(
            R.string.rent_amount, property.rent
        )
        holder.status.text = property.status

        // Clicking the row opens the details of the property.
        holder.itemView.setOnClickListener { onClick(property) }
        holder.editButton.setOnClickListener { onEdit(property) }
        holder.deleteButton.setOnClickListener { onDelete(property) }
    }

    companion object {
        // Works out if a row has changed so only that row is redrawn.
        private val DIFFERENCE = object : DiffUtil.ItemCallback<Property>() {
            override fun areItemsTheSame(oldItem: Property, newItem: Property): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Property, newItem: Property): Boolean =
                oldItem == newItem
        }
    }
}
