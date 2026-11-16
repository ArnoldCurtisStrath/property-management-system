package com.example.propertymanagementsystem.ui.maintenance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.databinding.FragmentMaintenanceListBinding

/**
 * The list of all the maintenance requests.
 *
 * Every request shows what is wrong, which property it is in and the state it
 * is in. Clicking a request opens the form where it can be changed.
 */
class MaintenanceListFragment : Fragment() {

    private var _binding: FragmentMaintenanceListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MaintenanceViewModel by viewModels()
    private lateinit var adapter: MaintenanceAdapter

    // The names of the properties, so the row can show where the problem is.
    private var propertyNames: Map<Long, String> = emptyMap()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMaintenanceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MaintenanceAdapter(
            onClick = { row -> openForm(row.request.id) },
            onDelete = { row -> askToDelete(row.request) }
        )

        binding.requestList.layoutManager = LinearLayoutManager(requireContext())
        binding.requestList.adapter = adapter

        binding.addRequestButton.setOnClickListener { openForm(0L) }

        viewModel.properties.observe(viewLifecycleOwner) { properties ->
            propertyNames = properties.associate { it.id to it.name }
        }

        viewModel.requests.observe(viewLifecycleOwner) { requests ->
            val rows = requests.map { request ->
                MaintenanceRow(
                    request = request,
                    propertyName = propertyNames[request.propertyId]
                        ?: getString(R.string.not_assigned)
                )
            }
            adapter.submitList(rows)
            binding.emptyMessage.visibility = if (rows.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun openForm(requestId: Long) {
        findNavController().navigate(
            R.id.action_maintenance_list_to_maintenance_form,
            Bundle().apply { putLong("requestId", requestId) }
        )
    }

    // The user is asked before a request is removed.
    private fun askToDelete(request: com.example.propertymanagementsystem.data.model.MaintenanceRequest) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_request)
            .setMessage(getString(R.string.delete_request_message, request.title))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.delete(request)
                com.google.android.material.snackbar.Snackbar
                    .make(binding.root, R.string.request_deleted, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
                    .show()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
