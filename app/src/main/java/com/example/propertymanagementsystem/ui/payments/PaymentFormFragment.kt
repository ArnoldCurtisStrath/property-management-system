package com.example.propertymanagementsystem.ui.payments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.data.model.Payment
import com.example.propertymanagementsystem.databinding.FragmentPaymentFormBinding
import com.example.propertymanagementsystem.util.Dates
import com.example.propertymanagementsystem.util.Validation
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * The screen where a rent payment is recorded.
 *
 * The tenant is picked from a drop down, the amount and the day are typed in
 * and the month the payment was made for is worked out from the day.
 */
class PaymentFormFragment : Fragment() {

    private var _binding: FragmentPaymentFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PaymentViewModel by viewModels()

    // The tenants that a payment can be recorded against.
    private var tenants = emptyList<com.example.propertymanagementsystem.data.model.Tenant>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // The drop down of tenants is filled from the database.
        viewModel.tenants.observe(viewLifecycleOwner) { list ->
            tenants = list
            binding.tenantSpinner.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                list.map { it.name }
            )
        }

        // The way the payment was made.
        binding.methodSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            Payment.allMethods
        )

        // The day starts as today and can be changed from the calendar.
        binding.dateInput.setText(Dates.today())
        binding.dateInput.setOnClickListener { openDatePicker() }

        binding.saveButton.setOnClickListener { save() }
        binding.cancelButton.setOnClickListener { findNavController().popBackStack() }
    }

    private fun openDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                binding.dateInput.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun save() {
        val amountText = binding.amountInput.text.toString().trim()
        val date = binding.dateInput.text.toString().trim()

        if (!Validation.isValidDate(date)) {
            binding.dateLayout.error = getString(R.string.date_invalid)
            return
        }
        binding.dateLayout.error = null

        val amount = amountText.toDoubleOrNull()
        if (amount == null || !Validation.isValidAmount(amount)) {
            binding.amountLayout.error = getString(R.string.amount_invalid)
            return
        }
        binding.amountLayout.error = null

        val tenant = tenants.getOrNull(binding.tenantSpinner.selectedItemPosition)
        if (tenant == null) {
            com.google.android.material.snackbar.Snackbar.make(
                binding.root, R.string.tenant_missing,
                com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        val method = binding.methodSpinner.selectedItem.toString()
        val month = Dates.monthOf(date)

        viewLifecycleOwner.lifecycleScope.launch {
            val payment = Payment(
                tenantId = tenant.id,
                propertyId = tenant.propertyId,
                amount = amount,
                date = date,
                month = month,
                method = method,
                status = Payment.STATUS_PAID
            )
            viewModel.add(payment)
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
