package com.example.propertymanagementsystem.ui.tenants

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
import com.example.propertymanagementsystem.data.ledger.LedgerEntry
import com.example.propertymanagementsystem.data.ledger.LedgerEntryType
import com.example.propertymanagementsystem.util.Dates
import com.example.propertymanagementsystem.util.Money

class LedgerEntryAdapter(
    private val onPaymentClick: (LedgerEntry) -> Unit,
    private val onDeletePayment: (LedgerEntry) -> Unit
) : ListAdapter<LedgerEntry, LedgerEntryAdapter.Holder>(DIFFERENCE) {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.entryDate)
        val label: TextView = view.findViewById(R.id.entryLabel)
        val meta: TextView = view.findViewById(R.id.entryMeta)
        val amount: TextView = view.findViewById(R.id.entryAmount)
        val balance: TextView = view.findViewById(R.id.entryBalance)
        val delete: ImageButton = view.findViewById(R.id.deleteEntryButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_ledger_entry, parent, false)
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val entry = getItem(position)
        val context = holder.itemView.context

        holder.date.text = Dates.shortDate(entry.date)
        holder.label.text = entry.label
        holder.amount.text = if (entry.type == LedgerEntryType.PAYMENT) {
            "-" + Money.format(entry.amount)
        } else {
            Money.format(entry.amount)
        }
        holder.amount.setTextColor(
            ContextCompat.getColor(
                context,
                if (entry.type == LedgerEntryType.PAYMENT) R.color.success else R.color.ink
            )
        )
        holder.balance.text = "BAL " + Money.format(entry.balanceAfter)
        holder.balance.setTextColor(
            ContextCompat.getColor(
                context,
                if (entry.balanceAfter > 0.005) R.color.danger else R.color.muted
            )
        )

        if (entry.type == LedgerEntryType.PAYMENT) {
            holder.meta.text = listOf(
                entry.method,
                entry.receiptNumber.ifBlank { "No receipt" }
            ).filter { it.isNotBlank() }.joinToString(" · ")
            holder.delete.visibility = View.VISIBLE
            holder.delete.setOnClickListener { onDeletePayment(entry) }
            holder.itemView.setOnClickListener { onPaymentClick(entry) }
        } else {
            holder.meta.text = "Rent charge"
            holder.delete.visibility = View.GONE
            holder.itemView.setOnClickListener(null)
        }
    }

    companion object {
        private val DIFFERENCE = object : DiffUtil.ItemCallback<LedgerEntry>() {
            override fun areItemsTheSame(oldItem: LedgerEntry, newItem: LedgerEntry): Boolean =
                oldItem.type == newItem.type &&
                    oldItem.date == newItem.date &&
                    oldItem.paymentId == newItem.paymentId &&
                    oldItem.label == newItem.label

            override fun areContentsTheSame(oldItem: LedgerEntry, newItem: LedgerEntry): Boolean =
                oldItem == newItem
        }
    }
}
