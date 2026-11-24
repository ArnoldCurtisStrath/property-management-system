package com.example.propertymanagementsystem.ui.payments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.SahihiApp
import com.example.propertymanagementsystem.data.ledger.TenantLedger
import com.example.propertymanagementsystem.data.model.Payment
import com.example.propertymanagementsystem.databinding.SheetCollectBinding
import com.example.propertymanagementsystem.util.Dates
import com.example.propertymanagementsystem.util.Money
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

/**
 * The fast collection sheet. Pick a tenant from the list, enter the amount,
 * choose the method, and the receipt is issued immediately.
 */
class CollectSheet : BottomSheetDialogFragment() {

    private var _binding: SheetCollectBinding? = null
    private val binding get() = _binding!!

    private val container by lazy { (requireActivity().application as SahihiApp).container }

    private lateinit var adapter: CollectTenantAdapter
    private var allLedgers: List<TenantLedger> = emptyList()
    private var selected: TenantLedger? = null
    private var preferredTenantId: Long = 0L
    private var autoSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferredTenantId = arguments?.getLong("tenantId", 0L) ?: 0L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SheetCollectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.let { sheet ->
                sheet.setBackgroundResource(R.drawable.bg_bottom_sheet)
                val behavior = BottomSheetBehavior.from(sheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.skipCollapsed = true
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CollectTenantAdapter { ledger -> select(ledger) }
        binding.tenantList.layoutManager = LinearLayoutManager(requireContext())
        binding.tenantList.adapter = adapter

        binding.collectorInput.setText(container.settings.defaultCollector)
        binding.methodGroup.check(R.id.methodCash)
        binding.closeButton.setOnClickListener { dismiss() }
        binding.cancelButton.setOnClickListener { dismiss() }
        binding.changeTenantButton.setOnClickListener { showPicker() }

        binding.fullBalanceButton.setOnClickListener {
            val ledger = selected ?: return@setOnClickListener
            if (ledger.balance > 0) {
                binding.amountInput.setText(Money.plain(ledger.balance))
            }
        }

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filter(s?.toString().orEmpty())
            }
        })

        container.ledger.ledgers.observe(viewLifecycleOwner) { all ->
            allLedgers = all.filter { it.tenant.active }
            adapter.submitList(allLedgers)
            if (!autoSelected && preferredTenantId != 0L) {
                allLedgers.firstOrNull { it.tenant.id == preferredTenantId }?.let {
                    autoSelected = true
                    select(it)
                }
            }
            if (allLedgers.isEmpty()) {
                binding.searchLayout.error = "Add an active tenant first"
                binding.recordButton.isEnabled = false
            }
        }

        binding.recordButton.setOnClickListener { record() }
    }

    private fun filter(text: String) {
        val query = text.trim()
        val rows = if (query.isEmpty()) {
            allLedgers
        } else {
            allLedgers.filter {
                it.tenant.name.contains(query, ignoreCase = true) ||
                    it.unitLabel.contains(query, ignoreCase = true) ||
                    it.buildingLabel.contains(query, ignoreCase = true)
            }
        }
        adapter.submitList(rows)
    }

    private fun select(ledger: TenantLedger) {
        selected = ledger
        binding.searchLayout.visibility = View.GONE
        binding.tenantList.visibility = View.GONE
        binding.selectedTenantCard.visibility = View.VISIBLE
        binding.selectedName.text = ledger.tenant.name
        binding.selectedUnit.text = listOf(ledger.buildingLabel, ledger.unitLabel)
            .filter { it.isNotBlank() && !it.startsWith("No ") }
            .joinToString(" · ")
        binding.tenantBalance.text = Money.format(ledger.balance)
        binding.amountInput.setText(
            if (ledger.balance > 0) Money.plain(ledger.balance) else ""
        )
        binding.searchLayout.error = null
    }

    private fun showPicker() {
        selected = null
        binding.searchLayout.visibility = View.VISIBLE
        binding.tenantList.visibility = View.VISIBLE
        binding.selectedTenantCard.visibility = View.GONE
        binding.searchInput.setText("")
        adapter.submitList(allLedgers)
        binding.tenantBalance.text = "Ksh 0"
        binding.amountInput.setText("")
    }

    private fun record() {
        val ledger = selected
        if (ledger == null) {
            Toast.makeText(requireContext(), "Choose a tenant first", Toast.LENGTH_SHORT).show()
            return
        }
        val amount = Money.parse(binding.amountInput.text?.toString().orEmpty())
        if (amount == null || amount <= 0.0) {
            binding.amountLayout.error = "Enter an amount greater than zero"
            return
        }
        binding.amountLayout.error = null

        val method = when (binding.methodGroup.checkedButtonId) {
            R.id.methodMpesa -> Payment.METHOD_MPESA
            R.id.methodBank -> Payment.METHOD_BANK
            else -> Payment.METHOD_CASH
        }
        val reference = binding.referenceInput.text?.toString()?.trim().orEmpty()
        val collector = binding.collectorInput.text?.toString()?.trim().orEmpty()
            .ifBlank { container.settings.defaultCollector }

        binding.recordButton.isEnabled = false
        binding.recordButton.text = "Recording…"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val paymentId = container.ledger.recordPayment(
                    tenantId = ledger.tenant.id,
                    amount = amount,
                    date = Dates.today(),
                    method = method,
                    reference = reference.ifBlank { null },
                    note = null,
                    recordedBy = collector
                )
                val nav = Navigation.findNavController(requireActivity(), R.id.navHostFragment)
                dismiss()
                nav.navigate(
                    R.id.receiptFragment,
                    bundleOf("paymentId" to paymentId)
                )
            } catch (error: Exception) {
                binding.recordButton.isEnabled = true
                binding.recordButton.text = "Record & issue receipt"
                Toast.makeText(
                    requireContext(),
                    "The payment could not be saved: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(tenantId: Long = 0L): CollectSheet {
            val sheet = CollectSheet()
            sheet.arguments = bundleOf("tenantId" to tenantId)
            return sheet
        }
    }
}
