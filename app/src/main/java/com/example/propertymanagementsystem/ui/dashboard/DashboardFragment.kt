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
 * It answers three questions before the agent leaves the house: what came
 * in, who still owes, and what needs a visit today.
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
        binding.avatar.text = viewModel.greetingName.firstOrNull()?.uppercase() ?: "S"

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

        binding.collectButton.setOnClickListener {
            CollectSheet.newInstance().show(parentFragmentManager, "collect")
        }
        binding.arrearsCard.setOnClickListener { openTenants() }
        binding.seeAllDebtors.setOnClickListener { openTenants() }
        binding.vacantCard.setOnClickListener {
            findNavController().navigate(R.id.buildingListFragment)
        }
        binding.repairsCard.setOnClickListener {
            findNavController().navigate(R.id.maintenanceListFragment)
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
        binding.heroCollected.text = Money.format(today.collected)
        binding.heroExpected.text = "of ${Money.format(today.expected)} expected · ${today.collectedPercent}%"
        binding.heroProgress.max = 100
        binding.heroProgress.setProgressCompat(today.collectedPercent, true)
        binding.heroCash.text = Money.format(today.cashCollected)
        binding.heroMpesa.text = Money.format(today.mpesaCollected)
        binding.heroReceipts.text = today.receiptCount.toString()

        binding.arrearsValue.text = when {
            today.arrearsCount == 0 -> "Everyone is paid up"
            else -> "${today.arrearsCount} tenant" + (if (today.arrearsCount == 1) "" else "s") +
                " · " + Money.format(today.arrearsAmount)
        }
        binding.vacantValue.text = when {
            today.vacantCount == 0 -> "No vacancy"
            else -> "${today.vacantCount} unit" + (if (today.vacantCount == 1) "" else "s") +
                " · " + Money.format(today.vacancyLoss) + " lost"
        }
        binding.repairsValue.text = when {
            today.openRepairs == 0 -> "Nothing open"
            today.urgentRepairs > 0 -> "${today.openRepairs} open · ${today.urgentRepairs} urgent"
            else -> "${today.openRepairs} open"
        }

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

    private fun openTenants() {
        findNavController().navigate(R.id.tenantListFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
