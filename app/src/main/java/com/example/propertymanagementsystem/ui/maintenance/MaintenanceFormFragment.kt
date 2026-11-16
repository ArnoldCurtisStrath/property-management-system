package com.example.propertymanagementsystem.ui.maintenance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.data.model.MaintenanceRequest
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.databinding.FragmentMaintenanceFormBinding
import com.example.propertymanagementsystem.util.Dates
import com.google.android.material.snackbar.Snackbar

/**
 * The screen where a maintenance request is raised.
 *
 * The problem that has to be fixed is described, the category is picked and
 * the property where the problem is found is chosen from a drop down.
 */
class MaintenanceFormFragment : Fragment() {

    private var _binding: FragmentMaintenanceFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MaintenanceViewModel by viewModels()

    // The properties a request can be raised against.
    private var properties: List<Property> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMaintenanceFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.properties.observe(viewLifecycleOwner) { list ->
            properties = list
            binding.propertySpinner.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                list.map { it.name }
            )
        }

        // The kinds of problem that can be reported.
        binding.categorySpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            MaintenanceRequest.allCategories
        )

        binding.saveButton.setOnClickListener { save() }
        binding.cancelButton.setOnClickListener { findNavController().popBackStack() }
    }

    // Checks what was typed and saves the request.
    private fun save() {
        val title = binding.titleInput.text.toString().trim()
        val description = binding.descriptionInput.text.toString().trim()

        if (title.isEmpty()) {
            binding.titleLayout.error = getString(R.string.request_title_empty)
            return
        }
        binding.titleLayout.error = null

        if (description.isEmpty()) {
            binding.descriptionLayout.error = getString(R.string.request_description_empty)
            return
        }
        binding.descriptionLayout.error = null

        val property = properties.getOrNull(binding.propertySpinner.selectedItemPosition)
        if (property == null) {
            Snackbar.make(
                binding.root, R.string.property_missing, Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        val category = binding.categorySpinner.selectedItem.toString()

        val request = MaintenanceRequest(
            propertyId = property.id,
            title = title,
            description = description,
            category = category,
            status = MaintenanceRequest.STATUS_OPEN,
            reportedAt = Dates.today()
        )

        viewModel.add(request)
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
