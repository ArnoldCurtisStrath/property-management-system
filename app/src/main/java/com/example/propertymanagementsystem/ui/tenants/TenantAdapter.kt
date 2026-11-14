package com.example.propertymanagementsystem.ui.tenants

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.data.model.Tenant

/**
 * A tenant together with the name of the property they live in.
 *
 * The tenants table only keeps the number of the property, so the name is
 * looked up from the properties table and put next to the tenant. This is
 * only used for the list, it is never saved in the database.
 */
data class TenantRow(
    val tenant: Tenant,
    val propertyName: String
)

/**
 * Fills in the rows of the tenant list.
 */
class TenantAdapter(
    private val onClick: (TenantRow) -> Unit,
    private val onEdit: (TenantRow) -> Unit,
    private val onDelete: (TenantRow) -> Unit
) : ListAdapter<TenantRow, TenantAdapter.TenantViewHolder>(DIFFERENCE) {

    class TenantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tenantName)
        val phone: TextView = view.findViewById(R.id.tenantPhone)
        val property: TextView = view.findViewById(R.id.tenantProperty)
        val rent: TextView = view.findViewById(R.id.tenantRent)
        val editButton: ImageButton = view.findViewById(R.id.editButton)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TenantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tenant, parent, false)
        return TenantViewHolder(view)
    }

    override fun onBindViewHolder(holder: TenantViewHolder, position: Int) {
        val row = getItem(position)
        val tenant = row.tenant

        holder.name.text = tenant.name
        holder.phone.text = tenant.phone
        holder.property.text = row.propertyName
        holder.rent.text = holder.itemView.context.getString(
            R.string.rent_amount, tenant.rent
        )

        holder.itemView.setOnClickListener { onClick(row) }
        holder.editButton.setOnClickListener { onEdit(row) }
        holder.deleteButton.setOnClickListener { onDelete(row) }
    }

    companion object {
        private val DIFFERENCE = object : DiffUtil.ItemCallback<TenantRow>() {
            override fun areItemsTheSame(oldItem: TenantRow, newItem: TenantRow): Boolean =
                oldItem.tenant.id == newItem.tenant.id

            override fun areContentsTheSame(oldItem: TenantRow, newItem: TenantRow): Boolean =
                oldItem == newItem
        }
    }
}
