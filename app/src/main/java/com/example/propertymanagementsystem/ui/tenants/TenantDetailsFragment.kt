package com.example.propertymanagementsystem.ui.tenants

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.data.ledger.LedgerCalculator
import com.example.propertymanagementsystem.data.ledger.LedgerEntryType
import com.example.propertymanagementsystem.data.ledger.TenantLedger
import com.example.propertymanagementsystem.data.model.Payment
import com.example.propertymanagementsystem.databinding.FragmentTenantDetailsBinding
import com.example.propertymanagementsystem.ui.payments.CollectSheet
import com.example.propertymanagementsystem.util.Dates
import com.example.propertymanagementsystem.util.Money
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * The tenant ledger: every charge, every payment, the running balance and the
 * receipt trail. This is the screen that settles arguments.
 */
class TenantDetailsFragment : Fragment() {

    private var _binding: FragmentTenantDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TenantLedgerViewModel by viewModels()
    private lateinit var adapter: LedgerEntryAdapter

    private var tenantId: Long = 0
    private var ledger: TenantLedger? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tenantId = arguments?.getLong("tenantId", 0L) ?: 0L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTenantDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = LedgerEntryAdapter(
            onPaymentClick = { entry ->
                if (entry.type == LedgerEntryType.PAYMENT && entry.paymentId != 0L) {
                    findNavController().navigate(
                        R.id.receiptFragment,
                        Bundle().apply { putLong("paymentId", entry.paymentId) }
                    )
                }
            },
            onDeletePayment = { entry -> askToDeletePayment(entry.paymentId) }
        )
        binding.ledgerList.layoutManager = LinearLayoutManager(requireContext())
        binding.ledgerList.adapter = adapter

        binding.editButton.setOnClickListener {
            findNavController().navigate(
                R.id.tenantFormFragment,
                Bundle().apply { putLong("tenantId", tenantId) }
            )
        }
        binding.collectButton.setOnClickListener {
            CollectSheet.newInstance(tenantId).show(parentFragmentManager, "collect")
        }
        binding.statementButton.setOnClickListener { shareTenantStatement() }
        binding.remindButton.setOnClickListener { shareReminder() }
        binding.deleteTenantButton.setOnClickListener { askToDeleteTenant() }

        viewModel.ledgerFor(tenantId).observe(viewLifecycleOwner) { loaded ->
            if (loaded == null) return@observe
            ledger = loaded
            render(loaded)
        }
    }

    private fun render(ledger: TenantLedger) {
        val tenant = ledger.tenant
        binding.tenantName.text = tenant.name
        binding.unitLine.text = listOf(ledger.buildingLabel, ledger.unitLabel)
            .filter { it.isNotBlank() && !it.startsWith("No ") }
            .joinToString(" · ")
        binding.contactLine.text = listOf(tenant.phone, tenant.email)
            .filter { it.isNotBlank() }.joinToString(" · ")

        val displayBalance = when {
            ledger.owes -> Money.format(ledger.balance)
            ledger.isCredit -> Money.format(ledger.credit)
            else -> "Ksh 0"
        }
        binding.balanceValue.text = displayBalance
        binding.balanceValue.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                when {
                    ledger.owes -> R.color.stamp
                    ledger.isCredit -> R.color.paid
                    else -> R.color.ink
                }
            )
        )
        binding.agingLine.text = when {
            ledger.isCredit -> "Credit on the account"
            ledger.owes -> "Oldest unpaid: " +
                (ledger.oldestUnpaidMonth?.let { Dates.monthLabel(it) } ?: "—") +
                " · ${ledger.daysLate} days late"
            else -> "No arrears. Every shilling is accounted for."
        }

        binding.statusChip.text = ledger.statusLabel.uppercase()
        when {
            ledger.isCredit || ledger.isSettled -> {
                binding.statusChip.setBackgroundResource(0)
                binding.statusChip.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.paid)
                )
            }
            ledger.agingBucket == LedgerCalculator.AGING_CURRENT -> {
                binding.statusChip.setBackgroundResource(0)
                binding.statusChip.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.muted)
                )
            }
            else -> {
                binding.statusChip.setBackgroundResource(R.drawable.bg_stamp)
                binding.statusChip.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.stamp)
                )
            }
        }

        binding.termsLine.text = "Rent ${Money.format(tenant.rent)} · " +
            "Deposit ${Money.format(tenant.deposit)} · Due day ${tenant.dueDay}"

        adapter.submitList(ledger.entries)
        binding.emptyLedger.visibility =
            if (ledger.entries.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun shareTenantStatement() {
        val current = ledger ?: return
        val lines = buildString {
            appendLine("SAHIHI · TENANT STATEMENT")
            appendLine(current.tenant.name)
            appendLine(listOf(current.buildingLabel, current.unitLabel)
                .filter { it.isNotBlank() && !it.startsWith("No ") }
                .joinToString(" · "))
            appendLine()
            current.entries.forEach { entry ->
                val sign = if (entry.type == LedgerEntryType.PAYMENT) "-" else ""
                appendLine(
                    "${entry.date}  $sign${Money.format(entry.amount)}  ${entry.label}" +
                        (if (entry.receiptNumber.isNotBlank()) "  ${entry.receiptNumber}" else "")
                )
            }
            appendLine()
            appendLine("Balance: ${Money.format(current.balance)}")
            appendLine("Every shilling, accounted for.")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Tenant rent statement")
            putExtra(Intent.EXTRA_TEXT, lines)
        }
        startActivity(Intent.createChooser(intent, "Share tenant statement"))
    }

    private fun shareReminder() {
        val current = ledger ?: return
        val unit = listOf(current.buildingLabel, current.unitLabel)
            .filter { it.isNotBlank() && !it.startsWith("No ") }
            .joinToString(" · ")
        val message = if (current.owes) {
            "Habari ${current.tenant.name}, your rent balance for $unit is " +
                "${Money.format(current.balance)}. Please settle it when you can. " +
                "Thank you."
        } else {
            "Habari ${current.tenant.name}, your rent for $unit is up to date. " +
                "Thank you for keeping the account clear."
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        startActivity(Intent.createChooser(intent, "Send reminder"))
    }

    private fun askToDeletePayment(paymentId: Long) {
        val payment: Payment = ledger?.payments?.firstOrNull { it.id == paymentId } ?: return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete this receipt?")
            .setMessage(
                "This removes ${Money.format(payment.amount)} from the ledger and recalculates " +
                    "the balance. This cannot be undone."
            )
            .setNegativeButton("Keep it", null)
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deletePayment(payment)
                Toast.makeText(requireContext(), "Receipt removed", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun askToDeleteTenant() {
        val current = ledger ?: return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete this tenant?")
            .setMessage(
                "This removes ${current.tenant.name} together with the ledger and receipts. " +
                    "The unit becomes vacant."
            )
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteTenant(current.tenant, current.unit?.id ?: 0L)
                findNavController().popBackStack()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
