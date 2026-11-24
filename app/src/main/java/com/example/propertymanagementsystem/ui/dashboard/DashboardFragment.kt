package com.example.propertymanagementsystem.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.data.ledger.TodayState
import com.example.propertymanagementsystem.databinding.FragmentDashboardBinding
import com.example.propertymanagementsystem.ui.payments.CollectSheet
import com.example.propertymanagementsystem.util.Money

/**
 * The Today screen: the agent's day book.
 *
 * It opens on the month's position, then the few rows that need a decision,
 * then the receipts posted today. No cards, no duplicated counts.
 */
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    private lateinit var debtorAdapter: TodayDebtorAdapter
    private lateinit var receiptAdapter: ReceiptMiniAdapter

    private var tenantNames: Map<Long, String> = emptyMap()
    private var lastToday: TodayState? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.greeting.text = "Habari, ${viewModel.greetingName}"
        binding.dateLabel.text = viewModel.dateLabel
        binding.monthLabel.text = viewModel.monthLabel
        binding.daysLeft.text = viewModel.daysLeftLabel

        debtorAdapter = TodayDebtorAdapter { ledger ->
            findNavController().navigate(
                R.id.tenantDetailsFragment,
                Bundle().apply { putLong("tenantId", ledger.tenant.id) }
            )
        }
        binding.debtorList.layoutManager = LinearLayoutManager(requireContext())
        binding.debtorList.adapter = debtorAdapter

        receiptAdapter = ReceiptMiniAdapter { payment ->
            findNavController().navigate(
                R.id.receiptFragment,
                Bundle().apply { putLong("paymentId", payment.id) }
            )
        }
        binding.receiptList.layoutManager = LinearLayoutManager(requireContext())
        binding.receiptList.adapter = receiptAdapter

        binding.collectButton.setOnClickListener { openCollect() }
        binding.collectStamp.setOnClickListener { openCollect() }
        binding.seeAllDebtors.setOnClickListener { openTenants() }
        binding.repairsRow.setOnClickListener {
            findNavController().navigate(R.id.maintenanceListFragment)
        }
        binding.vacantRow.setOnClickListener {
            findNavController().navigate(R.id.buildingListFragment)
        }
        binding.seeAllReceipts.setOnClickListener {
            findNavController().navigate(R.id.paymentHistoryFragment)
        }

        viewModel.today.observe(viewLifecycleOwner) { today ->
            lastToday = today
            render(today)
            renderReceipts(today)
        }

        viewModel.tenants.observe(viewLifecycleOwner) { tenants ->
            tenantNames = tenants.associate { it.id to it.name }
            lastToday?.let { renderReceipts(it) }
        }
    }

    private fun render(today: TodayState) {
        binding.outstandingValue.text = Money.format(today.outstanding)
        binding.outstandingCaption.text = when {
            today.arrearsCount == 0 -> "everyone is paid up"
            today.arrearsCount == 1 -> "1 tenant · all months"
            else -> "${today.arrearsCount} tenants · all months"
        }
        binding.statementProgress.max = 100
        binding.statementProgress.setProgressCompat(today.collectedPercent, true)
        binding.collectedCaption.text = "${Money.format(today.collected)} of " +
            "${Money.format(today.expected)} collected · ${today.collectedPercent}%"
        binding.collectedValue.text = Money.format(today.collected)
        binding.expectedValue.text = Money.format(today.expected)
        binding.receiptCountValue.text = today.receiptCount.toString()

        binding.repairsValue.text = when {
            today.openRepairs == 0 -> "Nothing open"
            today.urgentRepairs > 0 -> "${today.openRepairs} open · ${today.urgentRepairs} urgent"
            today.openRepairs == 1 -> "1 open"
            else -> "${today.openRepairs} open"
        }
        binding.repairsStamp.text = when {
            today.urgentRepairs > 0 -> "URGENT"
            today.openRepairs > 0 -> "OPEN"
            else -> "CLEAR"
        }
        binding.repairsStamp.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (today.openRepairs == 0) R.color.paid else R.color.stamp
            )
        )

        binding.vacantValue.text = when (today.vacantCount) {
            0 -> "No vacancy"
            1 -> "1 unit empty"
            else -> "${today.vacantCount} units empty"
        }
        binding.vacancyLossValue.text = Money.format(today.vacancyLoss)

        debtorAdapter.submitList(today.topDebtors)
        binding.emptyDebtors.visibility =
            if (today.topDebtors.isEmpty()) View.VISIBLE else View.GONE

        binding.portfolioLine.text = "${today.buildingCount} buildings · " +
            "${today.unitCount} units · ${today.occupiedCount} occupied"
    }

    private fun renderReceipts(today: TodayState) {
        val rows = today.recentReceipts.map { payment ->
            ReceiptMiniRow(
                payment = payment,
                tenantName = tenantNames[payment.tenantId] ?: "Tenant"
            )
        }
        receiptAdapter.submitList(rows)
        binding.emptyReceipts.visibility = if (rows.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openCollect() {
        CollectSheet.newInstance().show(parentFragmentManager, "collect")
    }

    private fun openTenants() {
        findNavController().navigate(R.id.tenantListFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
