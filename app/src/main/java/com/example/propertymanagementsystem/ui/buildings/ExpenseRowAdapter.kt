package com.example.propertymanagementsystem.ui.buildings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.data.model.Expense
import com.example.propertymanagementsystem.util.Dates
import com.example.propertymanagementsystem.util.Money

class ExpenseRowAdapter : ListAdapter<Expense, ExpenseRowAdapter.Holder>(DIFFERENCE) {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val category: TextView = view.findViewById(R.id.expenseCategory)
        val meta: TextView = view.findViewById(R.id.expenseMeta)
        val amount: TextView = view.findViewById(R.id.expenseAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_expense_row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val expense = getItem(position)
        holder.category.text = expense.category
        holder.meta.text = listOf(
            Dates.shortDate(expense.date),
            expense.vendor.ifBlank { "No vendor" }
        ).joinToString(" · ")
        holder.amount.text = Money.format(expense.amount)
    }

    companion object {
        private val DIFFERENCE = object : DiffUtil.ItemCallback<Expense>() {
            override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean =
                oldItem == newItem
        }
    }
}
