package com.example.propertymanagementsystem.ui.payments

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
import com.example.propertymanagementsystem.data.ledger.TodayState
import com.example.propertymanagementsystem.data.model.Building
import com.example.propertymanagementsystem.data.model.Payment
import com.example.propertymanagementsystem.databinding.FragmentPaymentHistoryBinding
import com.example.propertymanagementsystem.ui.buildings.ExpenseRowAdapter
import com.example.propertymanagementsystem.ui.dashboard.ReceiptMiniAdapter
import com.example.propertymanagementsystem.ui.dashboard.ReceiptMiniRow
import com.example.propertymanagementsystem.ui.dashboard.TodayDebtorAdapter
import com.example.propertymanagementsystem.util.Dates
import com.example.propertymanagementsystem.util.Money
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * The Money screen: cash in, cash out, receipts, expenses and arrears.
 */
class PaymentHistoryFragment : Fragment() {

    private var _binding: FragmentPaymentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MoneyViewModel by viewModels()

    private lateinit var receiptAdapter: ReceiptMiniAdapter
    private lateinit var expenseAdapter: ExpenseRowAdapter
    private lateinit var arrearsAdapter: TodayDebtorAdapter

    private var tenantNames: Map<Long, String> = emptyMap()
    private var payments: List<Payment> = emptyList()
    private var buildings: List<Building> = emptyList()
    private var lastToday: TodayState? = null

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

        binding.monthLabel.text = Dates.monthLabel(viewModel.month) +
            " · cash in, cash out, what is left."

        receiptAdapter = ReceiptMiniAdapter { payment ->
            findNavController().navigate(
                R.id.receiptFragment,
                Bundle().apply { putLong("paymentId", payment.id) }
            )
        }
        binding.receiptList.layoutManager = LinearLayoutManager(requireContext())
        binding.receiptList.adapter = receiptAdapter

        expenseAdapter = ExpenseRowAdapter()
        binding.expenseList.layoutManager = LinearLayoutManager(requireContext())
        binding.expenseList.adapter = expenseAdapter

        arrearsAdapter = TodayDebtorAdapter { ledger ->
            findNavController().navigate(
                R.id.tenantDetailsFragment,
                Bundle().apply { putLong("tenantId", ledger.tenant.id) }
            )
        }
        binding.arrearsList.layoutManager = LinearLayoutManager(requireContext())
        binding.arrearsList.adapter = arrearsAdapter

        binding.mpesaButton.setOnClickListener {
            findNavController().navigate(R.id.mpesaFragment)
        }
        binding.expenseButton.setOnClickListener {
            findNavController().navigate(R.id.expenseFormFragment)
        }
        binding.dayCloseButton.setOnClickListener {
            findNavController().navigate(R.id.dayCloseFragment)
        }
        binding.statementButton.setOnClickListener { chooseBuildingForStatement() }
        binding.seeAllArrears.setOnClickListener {
            findNavController().navigate(R.id.tenantListFragment)
        }

        viewModel.today.observe(viewLifecycleOwner) { today ->
            lastToday = today
            renderSummary(today)
            arrearsAdapter.submitList(today.topDebtors)
            binding.emptyArrears.visibility =
                if (today.topDebtors.isEmpty()) View.VISIBLE else View.GONE
            binding.receiptsCount.text = today.receiptCount.toString() + " THIS MONTH"
        }

        viewModel.tenants.observe(viewLifecycleOwner) { tenants ->
            tenantNames = tenants.associate { it.id to it.name }
            renderReceipts()
        }

        viewModel.payments.observe(viewLifecycleOwner) {
            payments = it.sortedWith(
                compareByDescending<Payment> { payment -> payment.date }
                    .thenByDescending { payment -> payment.id }
            ).take(12)
            renderReceipts()
        }

        viewModel.expenses.observe(viewLifecycleOwner) { expenses ->
            expenseAdapter.submitList(expenses)
            binding.expensesTotal.text = Money.format(expenses.sumOf { it.amount })
            binding.emptyExpenses.visibility =
                if (expenses.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.buildings.observe(viewLifecycleOwner) { buildings = it }
    }

    private fun renderSummary(today: TodayState) {
        binding.collectedValue.text = Money.format(today.collected)
        binding.expensesValue.text = Money.format(today.expensesThisMonth)
        binding.netValue.text = Money.format(today.netThisMonth)
        binding.netValue.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (today.netThisMonth < 0) R.color.danger else R.color.success
            )
        )
        binding.arrearsValue.text = Money.format(today.arrearsAmount)
    }

    private fun renderReceipts() {
        val rows = payments.map { payment ->
            ReceiptMiniRow(
                payment = payment,
                tenantName = tenantNames[payment.tenantId] ?: "Tenant"
            )
        }
        receiptAdapter.submitList(rows)
        binding.emptyReceipts.visibility =
            if (rows.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun chooseBuildingForStatement() {
        if (buildings.isEmpty()) {
            Toast.makeText(requireContext(), "Add a building first", Toast.LENGTH_SHORT).show()
            return
        }
        val names = buildings.map { it.name }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Owner statement")
            .setItems(names) { _, which ->
                val building = buildings[which]
                findNavController().navigate(
                    R.id.ownerStatementFragment,
                    Bundle().apply {
                        putLong("buildingId", building.id)
                        putString("month", viewModel.month)
                    }
                )
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
