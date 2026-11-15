package com.example.propertymanagementsystem.ui.tenants

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.data.model.Tenant
import com.example.propertymanagementsystem.databinding.FragmentTenantDetailsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/**
 * Shows everything that is known about one tenant.
 *
 * The name of the property the tenant lives in is looked up from the list of
 * properties and shown as well.
 */
class TenantDetailsFragment : Fragment() {

    private var _binding: FragmentTenantDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TenantViewModel by viewModels()
    private var tenantId: Long = 0
    private var tenant: Tenant? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tenantId = arguments?.getLong("tenantId", 0L) ?: 0L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTenantDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.tenant(tenantId).observe(viewLifecycleOwner) { loaded ->
            if (loaded == null) return@observe
            tenant = loaded

            binding.tenantName.text = loaded.name
            binding.tenantPhone.text = loaded.phone
            binding.tenantEmail.text = loaded.email
            binding.tenantRent.text = getString(R.string.rent_amount, loaded.rent)
            binding.tenantStartDate.text = loaded.startDate
        }

        // The property the tenant lives in is found by its number.
        viewModel.properties.observe(viewLifecycleOwner) { properties ->
            val current = tenant ?: return@observe
            val property = properties.find { it.id == current.propertyId }
            binding.tenantProperty.text = property?.name ?: getString(R.string.not_assigned)
        }

        binding.editButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_tenant_details_to_tenant_form,
                Bundle().apply { putLong("tenantId", tenantId) }
            )
        }

        binding.deleteButton.setOnClickListener { askToDelete() }
    }

    private fun askToDelete() {
        val current = tenant ?: return

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_tenant)
            .setMessage(getString(R.string.delete_tenant_message, current.name))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.delete(current)
                Snackbar.make(binding.root, R.string.tenant_deleted, Snackbar.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
