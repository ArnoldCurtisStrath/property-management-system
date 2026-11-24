package com.example.propertymanagementsystem.ui.payments

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.propertymanagementsystem.SahihiApp
import com.example.propertymanagementsystem.data.ledger.ReceiptData
import com.example.propertymanagementsystem.databinding.FragmentReceiptBinding
import com.example.propertymanagementsystem.util.Dates
import com.example.propertymanagementsystem.util.Money
import com.example.propertymanagementsystem.util.ReceiptGenerator
import kotlinx.coroutines.launch

/**
 * The receipt the tenant keeps. It is the moment the ledger becomes proof.
 */
class ReceiptFragment : Fragment() {

    private var _binding: FragmentReceiptBinding? = null
    private val binding get() = _binding!!

    private val container by lazy { (requireActivity().application as SahihiApp).container }
    private var data: ReceiptData? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.stamp.alpha = 0f
        binding.stamp.scaleX = 1.7f
        binding.stamp.scaleY = 1.7f
        binding.stamp.rotation = -25f

        binding.shareButton.setOnClickListener {
            val receipt = data
            if (receipt == null) {
                Toast.makeText(requireContext(), "The receipt is still loading", Toast.LENGTH_SHORT)
                    .show()
            } else {
                ReceiptGenerator.share(requireContext(), receipt)
            }
        }
        binding.doneButton.setOnClickListener { findNavController().popBackStack() }

        val paymentId = arguments?.getLong("paymentId", 0L) ?: 0L
        if (paymentId == 0L) {
            findNavController().popBackStack()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val receipt = container.ledger.receiptData(paymentId)
            if (receipt == null) {
                Toast.makeText(
                    requireContext(),
                    "That receipt could not be found",
                    Toast.LENGTH_LONG
                ).show()
                findNavController().popBackStack()
                return@launch
            }
            data = receipt
            render(receipt)
        }
    }

    private fun render(receipt: ReceiptData) {
        val payment = receipt.payment
        val number = payment.receiptNumber.ifBlank {
            "SH-" + payment.date.take(4) + "-" + payment.id.toString().padStart(4, '0')
        }

        binding.receiptNumber.text = number
        binding.tenantName.text = receipt.tenant.name
        binding.unitLine.text = listOf(
            receipt.building?.name.orEmpty(),
            receipt.unit?.name.orEmpty()
        ).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { "No unit" }
        binding.periodValue.text = Dates.monthLabel(payment.month)
        binding.dateValue.text = payment.date
        binding.methodValue.text = payment.method
        binding.referenceValue.text = payment.reference?.ifBlank { "—" } ?: "—"
        binding.collectorValue.text = payment.recordedBy
        binding.amountValue.text = Money.format(payment.amount)

        val balance = payment.balanceAfter
        binding.balanceValue.text = when {
            balance == null -> "Balance after: —"
            balance > 0.005 -> "Balance after: " + Money.format(balance)
            balance < -0.005 -> "Credit after: " + Money.format(-balance)
            else -> "Balance after: Ksh 0"
        }

        binding.stamp.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .rotation(-8f)
            .setDuration(520)
            .setInterpolator(OvershootInterpolator(1.2f))
            .start()
        binding.stamp.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
