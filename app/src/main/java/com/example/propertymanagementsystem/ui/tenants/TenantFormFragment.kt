package com.example.propertymanagementsystem.ui.tenants

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
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.data.model.Tenant
import com.example.propertymanagementsystem.databinding.FragmentTenantFormBinding
import com.example.propertymanagementsystem.util.Validation
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * The form that is used to add a tenant and to change one.
 *
 * The tenant has to be linked to a property, so the properties are shown in
 * a drop down and the one that is picked is saved with the tenant.
 */
class TenantFormFragment : Fragment() {

    private var _binding: FragmentTenantFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TenantViewModel by viewModels()

    // The id of the tenant being changed. Zero means a new tenant.
    private var tenantId: Long = 0

    private var existing: Tenant? = null
    private var formFilled = false

    // The properties that are shown in the drop down.
    private var properties: List<Property> = emptyList()

    // Used to pick the right property in the drop down when a tenant that is
    // already saved is opened.
    private var propertySpinnerReady = false
    private var pendingPropertyId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tenantId = arguments?.getLong("tenantId", 0L) ?: 0L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTenantFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // The drop down of properties is filled from the database.
        viewModel.properties.observe(viewLifecycleOwner) { list ->
            properties = list
            binding.propertySpinner.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                list.map { it.name }
            )
            propertySpinnerReady = true
            selectProperty()
        }

        // When a tenant that is already saved is opened its details are put
        // in the form.
        if (tenantId != 0L) {
            viewModel.tenant(tenantId).observe(viewLifecycleOwner) { tenant ->
                if (tenant != null && !formFilled) {
                    existing = tenant
                    binding.nameInput.setText(tenant.name)
                    binding.phoneInput.setText(tenant.phone)
                    binding.emailInput.setText(tenant.email)
                    binding.rentInput.setText(tenant.rent.toInt().toString())
                    binding.startDateInput.setText(tenant.startDate)
                    pendingPropertyId = tenant.propertyId
                    formFilled = true
                    selectProperty()
                }
            }
        }

        // The day the tenant moved in is picked from a calendar.
        binding.startDateInput.setOnClickListener { openDatePicker() }

        binding.saveButton.setOnClickListener { save() }
        binding.cancelButton.setOnClickListener { findNavController().popBackStack() }
    }

    // Puts the right property in the drop down.
    private fun selectProperty() {
        if (!propertySpinnerReady || pendingPropertyId == 0L) return
        val index = properties.indexOfFirst { it.id == pendingPropertyId }
        if (index >= 0) {
            binding.propertySpinner.setSelection(index)
        }
        propertySpinnerReady = false
    }

    private fun openDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                binding.startDateInput.setText(
                    String.format("%04d-%02d-%02d", year, month + 1, day)
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Checks what was typed and saves the tenant.
    private fun save() {
        val name = binding.nameInput.text.toString().trim()
        val phone = binding.phoneInput.text.toString().trim()
        val email = binding.emailInput.text.toString().trim()
        val rentText = binding.rentInput.text.toString().trim()
        val startDate = binding.startDateInput.text.toString().trim()

        if (name.isEmpty()) {
            binding.nameLayout.error = getString(R.string.name_empty)
            return
        }
        binding.nameLayout.error = null

        if (!Validation.isValidPhone(phone)) {
            binding.phoneLayout.error = getString(R.string.phone_invalid)
            return
        }
        binding.phoneLayout.error = null

        if (!Validation.isValidEmail(email)) {
            binding.emailLayout.error = getString(R.string.email_invalid)
            return
        }
        binding.emailLayout.error = null

        val rent = rentText.toDoubleOrNull()
        if (rent == null || !Validation.isValidAmount(rent)) {
            binding.rentLayout.error = getString(R.string.rent_invalid)
            return
        }
        binding.rentLayout.error = null

        if (!Validation.isValidDate(startDate)) {
            binding.startDateLayout.error = getString(R.string.date_invalid)
            return
        }
        binding.startDateLayout.error = null

        // The tenant has to live in one of the properties.
        val property = properties.getOrNull(binding.propertySpinner.selectedItemPosition)
        if (property == null) {
            com.google.android.material.snackbar.Snackbar.make(
                binding.root, R.string.property_missing,
                com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        val id = existing?.id ?: tenantId

        viewLifecycleOwner.lifecycleScope.launch {
            // The same phone number cannot be used by two tenants.
            if (viewModel.isDuplicatePhone(phone, id)) {
                binding.phoneLayout.error = getString(R.string.phone_exists)
                return@launch
            }

            val tenant = Tenant(
                id = id,
                name = name,
                phone = phone,
                email = email,
                propertyId = property.id,
                rent = rent,
                startDate = startDate,
                active = existing?.active ?: true
            )

            if (id == 0L) {
                viewModel.add(tenant)
            } else {
                viewModel.update(tenant)
            }
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
