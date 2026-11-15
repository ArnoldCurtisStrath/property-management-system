package com.example.propertymanagementsystem.ui.payments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.data.model.Payment
import com.example.propertymanagementsystem.data.model.Tenant
import com.example.propertymanagementsystem.databinding.FragmentPaymentHistoryBinding
import com.example.propertymanagementsystem.util.Dates
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/**
 * Shows the payments that have been recorded.
 *
 * When a tenant is passed to the screen only the payments of that tenant are
 * shown. When there is no tenant the payments of the whole portfolio are
 * shown instead. The numbers at the top are for the current month.
 */
class PaymentHistoryFragment : Fragment() {

    private var _binding: FragmentPaymentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PaymentViewModel by viewModels()
    private lateinit var adapter: PaymentAdapter

    // The tenant the screen is about. Zero means the whole portfolio.
    private var tenantId: Long = 0

    private var tenants: List<Tenant> = emptyList()
    private var tenantNames: Map<Long, String> = emptyMap()
    private var propertyNames: Map<Long, String> = emptyMap()
    private var collected: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tenantId = arguments?.getLong("tenantId", 0L) ?: 0L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PaymentAdapter(onDelete = { row -> askToDelete(row.payment) })

        binding.paymentList.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.paymentList.adapter = adapter

        binding.monthLabel.text = Dates.monthLabel(Dates.currentMonth())

        // The button at the bottom opens the screen where a payment is
        // recorded.
        binding.addPaymentButton.setOnClickListener {
            findNavController().navigate(R.id.action_payment_history_to_payment_form)
        }

        viewModel.tenants.observe(viewLifecycleOwner) { list ->
            tenants = list
            tenantNames = list.associate { it.id to it.name }
            // The rent that was expected this month is the rent of every
            // tenant that is still active.
            val expected = list.filter { it.active }.sumOf { it.rent }
            binding.expectedAmount.text = getString(R.string.rent_amount, expected)
            binding.outstandingAmount.text = getString(
                R.string.rent_amount, (expected - collected).coerceAtLeast(0.0)
            )
        }

        viewModel.properties.observe(viewLifecycleOwner) { list ->
            propertyNames = list.associate { it.id to it.name }
        }

        viewModel.totalCollected(Dates.currentMonth()).observe(viewLifecycleOwner) { total ->
            collected = total
            binding.collectedAmount.text = getString(R.string.rent_amount, total)
            val expected = tenants.filter { it.active }.sumOf { it.rent }
            binding.outstandingAmount.text = getString(
                R.string.rent_amount, (expected - total).coerceAtLeast(0.0)
            )
        }

        // The list of payments is watched. It depends on whether the screen
        // is showing one tenant or all of them.
        if (tenantId != 0L) {
            viewModel.forTenant(tenantId).observe(viewLifecycleOwner) { showPayments(it) }
        } else {
            viewModel.payments.observe(viewLifecycleOwner) { showPayments(it) }
        }
    }

    // Builds the rows of the list.
    private fun showPayments(payments: List<Payment>) {
        val rows = payments.map { payment ->
            PaymentRow(
                payment = payment,
                tenantName = tenantNames[payment.tenantId] ?: getString(R.string.unknown_tenant),
                propertyName = propertyNames[payment.propertyId]
                    ?: getString(R.string.not_assigned)
            )
        }
        adapter.submitList(rows)
        binding.emptyMessage.visibility = if (rows.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun askToDelete(payment: Payment) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_payment)
            .setMessage(getString(R.string.delete_payment_message, payment.amount))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.delete(payment)
                Snackbar.make(binding.root, R.string.payment_deleted, Snackbar.LENGTH_SHORT).show()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
