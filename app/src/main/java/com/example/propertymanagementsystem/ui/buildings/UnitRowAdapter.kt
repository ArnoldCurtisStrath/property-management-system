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
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.util.Money

data class UnitRow(
    val unit: Property,
    val tenantId: Long,
    val tenantName: String,
    val amount: Double,
    val statusLabel: String,
    val hasTenant: Boolean,
    val isOverdue: Boolean,
    val isVacant: Boolean
)

class UnitRowAdapter(
    private val onClick: (UnitRow) -> Unit
) : ListAdapter<UnitRow, UnitRowAdapter.Holder>(DIFFERENCE) {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val initials: TextView = view.findViewById(R.id.unitInitials)
        val name: TextView = view.findViewById(R.id.unitName)
        val tenant: TextView = view.findViewById(R.id.unitTenant)
        val balance: TextView = view.findViewById(R.id.unitBalance)
        val status: TextView = view.findViewById(R.id.unitStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_unit_row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val row = getItem(position)
        val context = holder.itemView.context

        holder.initials.text = row.unit.name.take(2).uppercase()
        holder.name.text = row.unit.name
        holder.tenant.text = if (row.hasTenant) row.tenantName else row.unit.type
        holder.balance.text = Money.format(row.amount)
        holder.status.text = row.statusLabel.uppercase()

        when {
            row.isVacant -> {
                holder.status.setBackgroundResource(R.drawable.bg_chip_current)
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.gold_dark))
                holder.balance.setTextColor(ContextCompat.getColor(context, R.color.muted))
            }
            row.isOverdue -> {
                holder.status.setBackgroundResource(R.drawable.bg_chip_overdue)
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.danger))
                holder.balance.setTextColor(ContextCompat.getColor(context, R.color.danger))
            }
            else -> {
                holder.status.setBackgroundResource(R.drawable.bg_chip_paid)
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.success))
                holder.balance.setTextColor(ContextCompat.getColor(context, R.color.success))
            }
        }

        holder.itemView.setOnClickListener { onClick(row) }
    }

    companion object {
        private val DIFFERENCE = object : DiffUtil.ItemCallback<UnitRow>() {
            override fun areItemsTheSame(oldItem: UnitRow, newItem: UnitRow): Boolean =
                oldItem.unit.id == newItem.unit.id

            override fun areContentsTheSame(oldItem: UnitRow, newItem: UnitRow): Boolean =
                oldItem == newItem
        }
    }
}
