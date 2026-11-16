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

/**
 * The screen where a maintenance request is raised and where it is changed.
 *
 * When the screen is opened without an id a new request is raised. When it
 * is opened with an id the request is loaded and the state of it can be
 * moved on, for example from Open to In progress.
 */
class MaintenanceFormFragment : Fragment() {

    private var _binding: FragmentMaintenanceFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MaintenanceViewModel by viewModels()

    // The id of the request being changed. Zero means a new request.
    private var requestId: Long = 0

    // The request that was loaded when the screen was opened.
    private var existing: MaintenanceRequest? = null

    // The properties a request can be raised against.
    private var properties: List<Property> = emptyList()

    // Used to pick the right property in the drop down when a request that
    // is already saved is opened.
    private var propertySpinnerReady = false
    private var pendingPropertyId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestId = arguments?.getLong("requestId", 0L) ?: 0L
    }

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
            propertySpinnerReady = true
            selectProperty()
        }

        // The kinds of problem that can be reported.
        binding.categorySpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            MaintenanceRequest.allCategories
        )

        // The states a request can be in.
        binding.statusSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            MaintenanceRequest.allStatuses
        )

        // When a request is opened again its details are put in the form.
        if (requestId != 0L) {
            binding.formTitle.setText(R.string.update_request)
            viewModel.request(requestId).observe(viewLifecycleOwner) { request ->
                if (request == null) return@observe
                existing = request
                binding.titleInput.setText(request.title)
                binding.descriptionInput.setText(request.description)
                binding.categorySpinner.setSelection(
                    MaintenanceRequest.allCategories.indexOf(request.category)
                )
                binding.statusSpinner.setSelection(
                    MaintenanceRequest.allStatuses.indexOf(request.status)
                )
                pendingPropertyId = request.propertyId
                selectProperty()
            }
        }

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
            com.google.android.material.snackbar.Snackbar.make(
                binding.root, R.string.property_missing,
                com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        val category = binding.categorySpinner.selectedItem.toString()
        val status = binding.statusSpinner.selectedItem.toString()

        // The day it was closed is only kept when the request is done.
        val resolvedAt = if (status == MaintenanceRequest.STATUS_DONE) {
            existing?.resolvedAt ?: Dates.today()
        } else {
            null
        }

        val request = MaintenanceRequest(
            id = existing?.id ?: 0,
            propertyId = property.id,
            title = title,
            description = description,
            category = category,
            status = status,
            reportedAt = existing?.reportedAt ?: Dates.today(),
            resolvedAt = resolvedAt
        )

        if (existing == null) {
            viewModel.add(request)
        } else {
            viewModel.update(request)
        }
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
