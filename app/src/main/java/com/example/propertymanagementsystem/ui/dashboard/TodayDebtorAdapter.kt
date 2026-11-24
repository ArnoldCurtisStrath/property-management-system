package com.example.propertymanagementsystem.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.data.ledger.LedgerCalculator
import com.example.propertymanagementsystem.data.ledger.TenantLedger
import com.example.propertymanagementsystem.util.Money

class TodayDebtorAdapter(
    private val onClick: (TenantLedger) -> Unit
) : ListAdapter<TenantLedger, TodayDebtorAdapter.Holder>(DIFFERENCE) {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val initials: TextView = view.findViewById(R.id.debtorInitials)
        val name: TextView = view.findViewById(R.id.debtorName)
        val unit: TextView = view.findViewById(R.id.debtorUnit)
        val balance: TextView = view.findViewById(R.id.debtorBalance)
        val status: TextView = view.findViewById(R.id.debtorStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_today_debtor, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val ledger = getItem(position)
        val context = holder.itemView.context

        holder.initials.text = initials(ledger.tenant.name)
        holder.name.text = ledger.tenant.name
        holder.unit.text = listOf(ledger.buildingLabel, ledger.unitLabel)
            .filter { it.isNotBlank() && it != "No building" }
            .joinToString(" · ")
            .ifBlank { "No unit" }
        holder.balance.text = Money.format(ledger.balance)
        holder.balance.setTextColor(
            ContextCompat.getColor(
                context,
                if (ledger.owes) R.color.danger else R.color.success
            )
        )

        holder.status.text = ledger.statusLabel.uppercase()
        when {
            ledger.isCredit || ledger.isSettled -> {
                holder.status.setBackgroundResource(R.drawable.bg_chip_paid)
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.success))
            }
            ledger.agingBucket == LedgerCalculator.AGING_CURRENT -> {
                holder.status.setBackgroundResource(R.drawable.bg_chip_current)
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.gold_dark))
            }
            else -> {
                holder.status.setBackgroundResource(R.drawable.bg_chip_overdue)
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.danger))
            }
        }

        holder.itemView.setOnClickListener { onClick(ledger) }
    }

    private fun initials(name: String): String {
        return name.trim().split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifBlank { "?" }
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
