package com.example.propertymanagementsystem.ui.properties

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.databinding.FragmentPropertyListBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * The list of all the properties that have been saved.
 *
 * The list comes from the database through the ViewModel. When there are no
 * properties yet a message is shown instead of an empty list.
 */
class PropertyListFragment : Fragment() {

    private var _binding: FragmentPropertyListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PropertyViewModel by viewModels()
    private lateinit var adapter: PropertyAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPropertyListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PropertyAdapter(
            onClick = { property -> openDetails(property) },
            onEdit = { property -> openForm(property) },
            onDelete = { property -> askToDelete(property) }
        )

        binding.propertyList.layoutManager = LinearLayoutManager(requireContext())
        binding.propertyList.adapter = adapter

        // The button at the bottom opens the form for a new property.
        binding.addPropertyButton.setOnClickListener { openForm(null) }

        // The list is watched, so it fills itself whenever the database changes.
        viewModel.properties.observe(viewLifecycleOwner) { properties ->
            adapter.submitList(properties)
            // The message is only shown when there is nothing in the list.
            binding.emptyMessage.visibility =
                if (properties.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun openDetails(property: Property) {
        findNavController().navigate(
            R.id.action_property_list_to_property_details,
            Bundle().apply { putLong("propertyId", property.id) }
        )
    }

    private fun openForm(property: Property?) {
        val bundle = Bundle()
        if (property != null) {
            bundle.putLong("propertyId", property.id)
        }
        findNavController().navigate(R.id.action_property_list_to_property_form, bundle)
    }

    // The user is asked before a property is removed because the tenants and
    // the payments under it are removed as well.
    private fun askToDelete(property: Property) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_property)
            .setMessage(getString(R.string.delete_property_message, property.name))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.delete(property)
                showMessage(getString(R.string.property_deleted))
            }
            .show()
    }

    private fun showMessage(message: String) {
        com.google.android.material.snackbar.Snackbar
            .make(binding.root, message, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
