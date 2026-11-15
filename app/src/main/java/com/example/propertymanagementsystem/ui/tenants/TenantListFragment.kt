package com.example.propertymanagementsystem.ui.tenants

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.data.model.Tenant
import com.example.propertymanagementsystem.databinding.FragmentTenantListBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/**
 * The list of all the tenants.
 *
 * The tenants and the properties are watched separately. When either of them
 * changes the list is built again with the name of the property next to each
 * tenant.
 */
class TenantListFragment : Fragment() {

    private var _binding: FragmentTenantListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TenantViewModel by viewModels()
    private lateinit var adapter: TenantAdapter

    // The two lists that the rows are built from.
    private var tenants: List<Tenant> = emptyList()
    private var propertyNames: Map<Long, String> = emptyMap()

    // What has been typed in the search box.
    private var searchText: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTenantListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TenantAdapter(
            onClick = { row -> openDetails(row.tenant) },
            onEdit = { row -> openForm(row.tenant) },
            onDelete = { row -> askToDelete(row.tenant) }
        )

        binding.tenantList.layoutManager = LinearLayoutManager(requireContext())
        binding.tenantList.adapter = adapter

        binding.addTenantButton.setOnClickListener { openForm(null) }

        // The list is filtered every time something is typed in the search box.
        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(text: Editable?) {
                searchText = text?.toString()?.trim() ?: ""
                refreshList()
            }
        })

        viewModel.tenants.observe(viewLifecycleOwner) {
            tenants = it
            refreshList()
        }

        viewModel.properties.observe(viewLifecycleOwner) { properties ->
            // The names are put in a map so the name of a property can be
            // found by its number.
            propertyNames = properties.associate { it.id to it.name }
            refreshList()
        }
    }

    // Builds the rows that are shown in the list. When something has been
    // typed in the search box only the tenants with that name are shown.
    private fun refreshList() {
        val matching = if (searchText.isEmpty()) {
            tenants
        } else {
            tenants.filter { it.name.contains(searchText, ignoreCase = true) }
        }

        val rows = matching.map { tenant ->
            TenantRow(
                tenant = tenant,
                propertyName = propertyNames[tenant.propertyId]
                    ?: getString(R.string.not_assigned)
            )
        }
        adapter.submitList(rows)
        binding.emptyMessage.visibility = if (rows.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openDetails(tenant: Tenant) {
        findNavController().navigate(
            R.id.action_tenant_list_to_tenant_details,
            Bundle().apply { putLong("tenantId", tenant.id) }
        )
    }

    private fun openForm(tenant: Tenant?) {
        val bundle = Bundle()
        if (tenant != null) {
            bundle.putLong("tenantId", tenant.id)
        }
        findNavController().navigate(R.id.action_tenant_list_to_tenant_form, bundle)
    }

    private fun askToDelete(tenant: Tenant) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_tenant)
            .setMessage(getString(R.string.delete_tenant_message, tenant.name))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.delete(tenant)
                Snackbar.make(binding.root, R.string.tenant_deleted, Snackbar.LENGTH_SHORT).show()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
