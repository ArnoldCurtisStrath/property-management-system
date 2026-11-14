package com.example.propertymanagementsystem.ui.properties

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.databinding.FragmentPropertyDetailsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/**
 * Shows everything that is known about one property.
 *
 * The property is loaded by its id, which is passed to the screen when it is
 * opened. From here the property can also be changed or removed.
 */
class PropertyDetailsFragment : Fragment() {

    private var _binding: FragmentPropertyDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PropertyViewModel by viewModels()
    private var propertyId: Long = 0
    private var property: Property? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        propertyId = arguments?.getLong("propertyId", 0L) ?: 0L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPropertyDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.property(propertyId).observe(viewLifecycleOwner) { loaded ->
            if (loaded == null) return@observe
            property = loaded

            binding.propertyName.text = loaded.name
            binding.propertyAddress.text = loaded.address
            binding.propertyType.text = loaded.type
            binding.propertyRent.text = getString(R.string.rent_amount, loaded.rent)
            binding.propertyStatus.text = loaded.status
        }

        binding.editButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_property_details_to_property_form,
                Bundle().apply { putLong("propertyId", propertyId) }
            )
        }

        binding.deleteButton.setOnClickListener { askToDelete() }
    }

    private fun askToDelete() {
        val current = property ?: return

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_property)
            .setMessage(getString(R.string.delete_property_message, current.name))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.delete(current)
                Snackbar.make(
                    binding.root,
                    R.string.property_deleted,
                    Snackbar.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
