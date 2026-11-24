package com.example.propertymanagementsystem.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.data.model.Payment
import com.example.propertymanagementsystem.util.Dates
import com.example.propertymanagementsystem.util.Money

data class ReceiptMiniRow(
    val payment: Payment,
    val tenantName: String
)

class ReceiptMiniAdapter(
    private val onClick: (Payment) -> Unit
) : ListAdapter<ReceiptMiniRow, ReceiptMiniAdapter.Holder>(DIFFERENCE) {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val tenant: TextView = view.findViewById(R.id.receiptTenant)
        val meta: TextView = view.findViewById(R.id.receiptMeta)
        val amount: TextView = view.findViewById(R.id.receiptAmount)
        val number: TextView = view.findViewById(R.id.receiptNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_receipt_mini, parent, false)
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val row = getItem(position)
        holder.tenant.text = row.tenantName
        holder.meta.text = row.payment.method + " · " + Dates.shortDate(row.payment.date)
        holder.amount.text = Money.format(row.payment.amount)
        holder.number.text = row.payment.receiptNumber.ifBlank { "Receipt" }
        holder.itemView.setOnClickListener { onClick(row.payment) }
    }

    companion object {
        private val DIFFERENCE = object : DiffUtil.ItemCallback<ReceiptMiniRow>() {
            override fun areItemsTheSame(oldItem: ReceiptMiniRow, newItem: ReceiptMiniRow): Boolean =
                oldItem.payment.id == newItem.payment.id

            override fun areContentsTheSame(oldItem: ReceiptMiniRow, newItem: ReceiptMiniRow): Boolean =
                oldItem == newItem
        }
    }
}
