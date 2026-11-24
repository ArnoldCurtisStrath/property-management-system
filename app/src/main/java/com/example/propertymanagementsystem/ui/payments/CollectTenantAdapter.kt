package com.example.propertymanagementsystem.ui.payments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.data.ledger.TenantLedger
import com.example.propertymanagementsystem.util.Money

class CollectTenantAdapter(
    private val onClick: (TenantLedger) -> Unit
) : ListAdapter<TenantLedger, CollectTenantAdapter.Holder>(DIFFERENCE) {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val initials: TextView = view.findViewById(R.id.collectInitials)
        val name: TextView = view.findViewById(R.id.collectName)
        val unit: TextView = view.findViewById(R.id.collectUnit)
        val balance: TextView = view.findViewById(R.id.collectBalance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_collect_tenant, parent, false)
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val ledger = getItem(position)
        val context = holder.itemView.context

        holder.initials.text = ledger.tenant.name.trim().split(" ")
            .filter { it.isNotBlank() }.take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifBlank { "?" }
        holder.name.text = ledger.tenant.name
        holder.unit.text = listOf(ledger.buildingLabel, ledger.unitLabel)
            .filter { it.isNotBlank() && !it.startsWith("No ") }
            .joinToString(" · ")
        holder.balance.text = Money.format(ledger.balance)
        holder.balance.setTextColor(
            ContextCompat.getColor(
                context,
                if (ledger.owes) R.color.danger else R.color.success
            )
        )
        holder.itemView.setOnClickListener { onClick(ledger) }
    }

    companion object {
        private val DIFFERENCE = object : DiffUtil.ItemCallback<TenantLedger>() {
            override fun areItemsTheSame(oldItem: TenantLedger, newItem: TenantLedger): Boolean =
                oldItem.tenant.id == newItem.tenant.id

            override fun areContentsTheSame(oldItem: TenantLedger, newItem: TenantLedger): Boolean =
                oldItem == newItem
        }
    }
}
