package com.example.propertymanagementsystem.ui.properties

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
import com.example.propertymanagementsystem.data.model.Building
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.databinding.FragmentPropertyFormBinding
import com.example.propertymanagementsystem.util.Validation
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Adds or changes a unit. A unit always belongs to a building; the building
 * supplies the address, so the agent does not type the same address for every
 * bedsitter in a block.
 */
class PropertyFormFragment : Fragment() {

    private var _binding: FragmentPropertyFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PropertyViewModel by viewModels()

    private var propertyId: Long = 0
    private var preferredBuildingId: Long = 0
    private var existing: Property? = null
    private var formFilled = false

    private var buildings: List<Building> = emptyList()
    private var spinnerReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        propertyId = arguments?.getLong("propertyId", 0L) ?: 0L
        preferredBuildingId = arguments?.getLong("buildingId", 0L) ?: 0L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPropertyFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.typeSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            Property.allTypes
        )
        binding.statusSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            Property.allStatuses
        )

        viewModel.buildings.observe(viewLifecycleOwner) { list ->
            buildings = list
            binding.buildingSpinner.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                list.map { it.name }
            )
            spinnerReady = true
            selectBuilding()
        }

        if (propertyId != 0L) {
            binding.formTitle.setText(R.string.edit_unit)
            viewModel.property(propertyId).observe(viewLifecycleOwner) { property ->
                if (property != null && !formFilled) {
                    existing = property
                    binding.nameInput.setText(property.name)
                    binding.addressInput.setText(property.address)
                    binding.rentInput.setText(property.rent.toInt().toString())
                    binding.typeSpinner.setSelection(
                        Property.allTypes.indexOf(property.type).coerceAtLeast(0)
                    )
                    binding.statusSpinner.setSelection(
                        Property.allStatuses.indexOf(property.status).coerceAtLeast(0)
                    )
                    preferredBuildingId = property.buildingId
                    formFilled = true
                    selectBuilding()
                }
            }
        }

        binding.saveButton.setOnClickListener { save() }
        binding.cancelButton.setOnClickListener { findNavController().popBackStack() }
    }

    private fun selectBuilding() {
        if (!spinnerReady || preferredBuildingId == 0L) return
        val index = buildings.indexOfFirst { it.id == preferredBuildingId }
        if (index >= 0) binding.buildingSpinner.setSelection(index)
    }

    private fun save() {
        val building = buildings.getOrNull(binding.buildingSpinner.selectedItemPosition)
        if (building == null) {
            Snackbar.make(binding.root, R.string.add_building_first, Snackbar.LENGTH_LONG).show()
            return
        }

        val name = binding.nameInput.text.toString().trim()
        val rentText = binding.rentInput.text.toString().trim()

        if (name.isEmpty()) {
            binding.nameLayout.error = getString(R.string.name_empty)
            return
        }
        binding.nameLayout.error = null

        val rent = rentText.toDoubleOrNull()
        if (rent == null || !Validation.isValidAmount(rent)) {
            binding.rentLayout.error = getString(R.string.rent_invalid)
            return
        }
        binding.rentLayout.error = null

        val type = binding.typeSpinner.selectedItem.toString()
        val status = binding.statusSpinner.selectedItem.toString()
        val id = existing?.id ?: propertyId

        viewLifecycleOwner.lifecycleScope.launch {
            if (viewModel.isDuplicate(name, building.id, id)) {
                binding.nameLayout.error = getString(R.string.property_exists)
                return@launch
            }

            val unit = Property(
                id = id,
                buildingId = building.id,
                name = name,
                address = building.address,
                type = type,
                rent = rent,
                status = status,
                latitude = existing?.latitude ?: 0.0,
                longitude = existing?.longitude ?: 0.0,
                createdAt = existing?.createdAt ?: System.currentTimeMillis()
            )

            if (id == 0L) viewModel.add(unit) else viewModel.update(unit)
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
