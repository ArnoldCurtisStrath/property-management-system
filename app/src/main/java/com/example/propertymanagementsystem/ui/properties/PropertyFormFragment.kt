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
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.databinding.FragmentPropertyFormBinding
import com.example.propertymanagementsystem.util.Validation
import kotlinx.coroutines.launch

/**
 * The form that is used to add a property and to change one.
 *
 * The same screen does both. When the screen is opened without an id a new
 * property is added, and when it is opened with an id the details of that
 * property are filled in and saved back with the changes.
 */
class PropertyFormFragment : Fragment() {

    private var _binding: FragmentPropertyFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PropertyViewModel by viewModels()

    // The id of the property being changed. Zero means a new property.
    private var propertyId: Long = 0

    // The property that was loaded when the screen was opened.
    private var existing: Property? = null

    // Makes sure the details are only put in the fields once.
    private var formFilled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        propertyId = arguments?.getLong("propertyId", 0L) ?: 0L
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

        // The drop downs are filled with the types and the states that the
        // Property class allows.
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

        // When a property is being changed its details are shown in the form.
        if (propertyId != 0L) {
            viewModel.property(propertyId).observe(viewLifecycleOwner) { property ->
                if (property != null && !formFilled) {
                    existing = property
                    binding.nameInput.setText(property.name)
                    binding.addressInput.setText(property.address)
                    binding.rentInput.setText(property.rent.toInt().toString())
                    binding.typeSpinner.setSelection(Property.allTypes.indexOf(property.type))
                    binding.statusSpinner.setSelection(
                        Property.allStatuses.indexOf(property.status)
                    )
                    formFilled = true
                }
            }
        }

        binding.saveButton.setOnClickListener { save() }
        binding.cancelButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    // Checks what was typed and saves the property.
    private fun save() {
        val name = binding.nameInput.text.toString().trim()
        val address = binding.addressInput.text.toString().trim()
        val rentText = binding.rentInput.text.toString().trim()

        if (name.isEmpty()) {
            binding.nameLayout.error = getString(R.string.name_empty)
            return
        }
        binding.nameLayout.error = null

        if (address.isEmpty()) {
            binding.addressLayout.error = getString(R.string.address_empty)
            return
        }
        binding.addressLayout.error = null

        val rent = rentText.toDoubleOrNull()
        if (rent == null || !Validation.isValidAmount(rent)) {
            binding.rentLayout.error = getString(R.string.rent_invalid)
            return
        }
        binding.rentLayout.error = null

        val type = binding.typeSpinner.selectedItem.toString()
        val status = binding.statusSpinner.selectedItem.toString()
        val id = existing?.id ?: propertyId

        // The saving itself is done in a coroutine so the screen is not held
        // up while the database writes the record.
        viewLifecycleOwner.lifecycleScope.launch {
            val property = Property(
                id = id,
                name = name,
                address = address,
                type = type,
                rent = rent,
                status = status,
                latitude = existing?.latitude ?: 0.0,
                longitude = existing?.longitude ?: 0.0,
                createdAt = existing?.createdAt ?: System.currentTimeMillis()
            )

            if (id == 0L) {
                viewModel.add(property)
            } else {
                viewModel.update(property)
            }
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
