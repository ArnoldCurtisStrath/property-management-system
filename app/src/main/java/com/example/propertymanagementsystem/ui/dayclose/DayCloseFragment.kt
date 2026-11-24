package com.example.propertymanagementsystem.ui.dayclose

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.SahihiApp
import com.example.propertymanagementsystem.data.model.Payment
import com.example.propertymanagementsystem.databinding.FragmentDayCloseBinding
import com.example.propertymanagementsystem.util.Dates
import com.example.propertymanagementsystem.util.Money
import com.example.propertymanagementsystem.util.Validation
import kotlinx.coroutines.launch

class DayCloseFragment : Fragment() {

    private var _binding: FragmentDayCloseBinding? = null
    private val binding get() = _binding!!

    private val container by lazy { (requireActivity().application as SahihiApp).container }

    private var cash = 0.0
    private var mpesa = 0.0
    private var bank = 0.0
    private var receipts = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDayCloseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.dateLabel.text = Dates.longDate()
        binding.doneButton.setOnClickListener { findNavController().popBackStack() }
        binding.shareButton.setOnClickListener { share() }

        binding.handoverInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) = updateVariance()
        })

        viewLifecycleOwner.lifecycleScope.launch {
            val payments = container.payments.getForDateOnce(Dates.today())
                .filter { it.status == Payment.STATUS_PAID }
            cash = payments.filter { it.method == Payment.METHOD_CASH }.sumOf { it.amount }
            mpesa = payments.filter { it.method == Payment.METHOD_MPESA }.sumOf { it.amount }
            bank = payments.filter { it.method == Payment.METHOD_BANK }.sumOf { it.amount }
            receipts = payments.size

            binding.cashValue.text = Money.format(cash)
            binding.mpesaValue.text = Money.format(mpesa)
            binding.bankValue.text = Money.format(bank)
            binding.receiptsValue.text = receipts.toString()
            binding.handoverInput.setText(Money.plain(cash))
            updateVariance()
        }
    }

    private fun updateVariance() {
        val handedOver = Money.parse(binding.handoverInput.text?.toString().orEmpty()) ?: 0.0
        val variance = handedOver - cash
        binding.varianceValue.text = Money.format(variance)
        binding.varianceValue.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (kotlin.math.abs(variance) < 0.5) R.color.success else R.color.danger
            )
        )
        binding.varianceValue.text = if (kotlin.math.abs(variance) < 0.5) {
            "Ksh 0 · clean"
        } else {
            Money.format(variance)
        }
    }

    private fun share() {
        val handedOver = Money.parse(binding.handoverInput.text?.toString().orEmpty()) ?: 0.0
        val variance = handedOver - cash
        val text = buildString {
            appendLine("SAHIHI · DAY CLOSE")
            appendLine(Dates.longDate())
            appendLine()
            appendLine("Cash collected: ${Money.format(cash)}")
            appendLine("M-Pesa: ${Money.format(mpesa)}")
            appendLine("Bank: ${Money.format(bank)}")
            appendLine("Receipts: $receipts")
            appendLine("Cash handed over: ${Money.format(handedOver)}")
            appendLine("Variance: ${Money.format(variance)}")
            appendLine()
            appendLine(
                if (kotlin.math.abs(variance) < 0.5) {
                    "Every shilling accounted for."
                } else {
                    "There is a variance to explain."
                }
            )
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Day close · ${Dates.today()}")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "Share day close"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
