package com.example.propertymanagementsystem.ui.payments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.SahihiApp
import com.example.propertymanagementsystem.data.ledger.TenantLedger
import com.example.propertymanagementsystem.data.model.Payment
import com.example.propertymanagementsystem.databinding.FragmentMpesaBinding
import com.example.propertymanagementsystem.util.Money
import com.example.propertymanagementsystem.util.MpesaMessage
import com.example.propertymanagementsystem.util.MpesaParser
import kotlinx.coroutines.launch

/**
 * The M-Pesa bridge.
 *
 * The agent pastes the message they already have. The app reads the amount,
 * phone and date, matches the phone to a tenant, and shows the balance before
 * anything is saved. The confirmation is the agent's, not the parser's.
 */
class MpesaFragment : Fragment() {

    private var _binding: FragmentMpesaBinding? = null
    private val binding get() = _binding!!

    private val container by lazy { (requireActivity().application as SahihiApp).container }

    private var parsed: MpesaMessage? = null
    private var matched: TenantLedger? = null
    private var phoneToLedger: Map<String, TenantLedger> = emptyMap()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMpesaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        container.ledger.ledgers.observe(viewLifecycleOwner) { ledgers ->
            phoneToLedger = ledgers
                .filter { it.tenant.active }
                .associateBy { MpesaParser.normalizePhone(it.tenant.phone) }
            if (parsed != null) match()
        }

        binding.pasteButton.setOnClickListener { pasteFromClipboard() }
        binding.readButton.setOnClickListener { readMessage() }
        binding.recordButton.setOnClickListener { record() }
        binding.cancelButton.setOnClickListener { findNavController().popBackStack() }
    }

    private fun pasteFromClipboard() {
        val manager = requireContext()
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = manager.primaryClip
        val text = if (clip != null && clip.itemCount > 0) {
            clip.getItemAt(0).coerceToText(requireContext()).toString()
        } else {
            ""
        }
        if (text.isBlank()) {
            Toast.makeText(requireContext(), "The clipboard is empty", Toast.LENGTH_SHORT).show()
            return
        }
        binding.messageInput.setText(text)
    }

    private fun readMessage() {
        val message = MpesaParser.parse(binding.messageInput.text?.toString().orEmpty())
        if (message == null) {
            binding.previewCard.visibility = View.GONE
            binding.recordButton.visibility = View.GONE
            Toast.makeText(
                requireContext(),
                "That does not look like an M-Pesa received message",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        parsed = message
        render(message)
        match()
    }

    private fun render(message: MpesaMessage) {
        binding.previewCard.visibility = View.VISIBLE
        binding.previewAmount.text = Money.format(message.amount)
        binding.previewMeta.text = listOf(
            message.phone,
            message.date,
            message.reference.ifBlank { "No reference" },
            message.senderName.ifBlank { "Sender unknown" }
        ).joinToString(" · ")
    }

    private fun match() {
        val message = parsed ?: return
        val ledger = phoneToLedger[MpesaParser.normalizePhone(message.phone)]
        matched = ledger

        if (ledger == null) {
            binding.matchName.text = "No tenant found"
            binding.matchUnit.text = "The phone " + message.phone + " is not on an active tenancy."
            binding.matchBalance.text = ""
            binding.matchError.visibility = View.VISIBLE
            binding.recordButton.visibility = View.GONE
            return
        }

        binding.matchError.visibility = View.GONE
        binding.matchName.text = ledger.tenant.name
        binding.matchUnit.text = listOf(ledger.buildingLabel, ledger.unitLabel)
            .filter { it.isNotBlank() && !it.startsWith("No ") }
            .joinToString(" · ")
        binding.matchBalance.text = "Balance " + Money.format(ledger.balance)
        binding.recordButton.visibility = View.VISIBLE
    }

    private fun record() {
        val message = parsed ?: return
        val ledger = matched ?: return

        binding.recordButton.isEnabled = false
        binding.recordButton.text = "Recording…"

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val paymentId = container.ledger.recordPayment(
                    tenantId = ledger.tenant.id,
                    amount = message.amount,
                    date = message.date,
                    method = Payment.METHOD_MPESA,
                    reference = message.reference.ifBlank { null },
                    note = "M-Pesa from " + message.senderName.ifBlank { message.phone },
                    recordedBy = container.settings.defaultCollector
                )
                findNavController().navigate(
                    R.id.receiptFragment,
                    bundleOf("paymentId" to paymentId)
                )
            } catch (error: Exception) {
                binding.recordButton.isEnabled = true
                binding.recordButton.text = "Record M-Pesa & issue receipt"
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
}
