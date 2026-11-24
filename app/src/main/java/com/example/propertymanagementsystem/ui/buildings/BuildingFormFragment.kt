package com.example.propertymanagementsystem.ui.buildings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.propertymanagementsystem.data.model.Building
import com.example.propertymanagementsystem.databinding.FragmentBuildingFormBinding
import com.example.propertymanagementsystem.util.Validation

class BuildingFormFragment : Fragment() {

    private var _binding: FragmentBuildingFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BuildingViewModel by viewModels()

    private var buildingId: Long = 0
    private var existing: Building? = null
    private var formFilled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildingId = arguments?.getLong("buildingId", 0L) ?: 0L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuildingFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.typeSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            Building.allTypes
        )

        if (buildingId != 0L) {
            binding.formTitle.setText("Edit building")
            viewModel.building(buildingId).observe(viewLifecycleOwner) { building ->
                if (building != null && !formFilled) {
                    existing = building
                    binding.nameInput.setText(building.name)
                    binding.addressInput.setText(building.address)
                    binding.ownerNameInput.setText(building.ownerName)
                    binding.ownerPhoneInput.setText(building.ownerPhone)
                    binding.ownerEmailInput.setText(building.ownerEmail)
                    binding.feeInput.setText(building.managementFee.toInt().toString())
                    binding.notesInput.setText(building.notes)
                    binding.typeSpinner.setSelection(
                        Building.allTypes.indexOf(building.type).coerceAtLeast(0)
                    )
                    formFilled = true
                }
            }
        } else {
            binding.feeInput.setText("10")
        }

        binding.saveButton.setOnClickListener { save() }
        binding.cancelButton.setOnClickListener { findNavController().popBackStack() }
    }

    private fun save() {
        val name = binding.nameInput.text.toString().trim()
        val address = binding.addressInput.text.toString().trim()
        val ownerName = binding.ownerNameInput.text.toString().trim()
        val ownerPhone = binding.ownerPhoneInput.text.toString().trim()
        val ownerEmail = binding.ownerEmailInput.text.toString().trim()
        val feeText = binding.feeInput.text.toString().trim()
        val notes = binding.notesInput.text.toString().trim()

        if (name.isEmpty()) {
            binding.nameLayout.error = "Give the building a name"
            return
        }
        binding.nameLayout.error = null

        if (address.isEmpty()) {
            binding.addressLayout.error = "Add the area or address"
            return
        }
        binding.addressLayout.error = null

        if (ownerPhone.isNotEmpty() && !Validation.isValidPhone(ownerPhone)) {
            binding.ownerPhoneLayout.error = "Use a 10 digit phone number"
            return
        }
        binding.ownerPhoneLayout.error = null

        if (ownerEmail.isNotEmpty() && !Validation.isValidEmail(ownerEmail)) {
            binding.ownerEmailLayout.error = "Enter a valid email"
            return
        }
        binding.ownerEmailLayout.error = null

        val fee = feeText.toDoubleOrNull() ?: 10.0
        if (fee < 0.0 || fee > 100.0) {
            binding.feeLayout.error = "The fee must be between 0 and 100"
            return
        }
        binding.feeLayout.error = null

        val building = Building(
            id = existing?.id ?: buildingId,
            name = name,
            address = address,
            type = binding.typeSpinner.selectedItem.toString(),
            ownerName = ownerName,
            ownerPhone = ownerPhone,
            ownerEmail = ownerEmail,
            managementFee = fee,
            notes = notes,
            createdAt = existing?.createdAt ?: System.currentTimeMillis()
        )

        if (existing == null) viewModel.add(building) else viewModel.update(building)
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
