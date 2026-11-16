package com.example.propertymanagementsystem.ui.payments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.data.model.Payment

/**
 * A payment together with the names of the tenant and the property it was
 * made for. It is only used for the list.
 */
data class PaymentRow(
    val payment: Payment,
    val tenantName: String,
    val propertyName: String
)

/**
 * Fills in the rows of the payment list.
 */
class PaymentAdapter(
    private val onDelete: (PaymentRow) -> Unit
) : ListAdapter<PaymentRow, PaymentAdapter.PaymentViewHolder>(DIFFERENCE) {

    class PaymentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tenant: TextView = view.findViewById(R.id.paymentTenant)
        val property: TextView = view.findViewById(R.id.paymentProperty)
        val amount: TextView = view.findViewById(R.id.paymentAmount)
        val method: TextView = view.findViewById(R.id.paymentMethod)
        val date: TextView = view.findViewById(R.id.paymentDate)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val row = getItem(position)

        holder.tenant.text = row.tenantName
        holder.property.text = row.propertyName
        holder.amount.text = holder.itemView.context.getString(
            R.string.rent_amount, row.payment.amount
        )
        holder.method.text = row.payment.method
        holder.date.text = row.payment.date

        holder.deleteButton.setOnClickListener { onDelete(row) }
    }

    companion object {
        private val DIFFERENCE = object : DiffUtil.ItemCallback<PaymentRow>() {
            override fun areItemsTheSame(oldItem: PaymentRow, newItem: PaymentRow): Boolean =
                oldItem.payment.id == newItem.payment.id

            override fun areContentsTheSame(oldItem: PaymentRow, newItem: PaymentRow): Boolean =
                oldItem == newItem
        }
    }
}
