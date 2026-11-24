package com.example.propertymanagementsystem.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.propertymanagementsystem.SahihiApp
import com.example.propertymanagementsystem.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val container by lazy { (requireActivity().application as SahihiApp).container }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val settings = container.settings

        binding.businessInput.setText(settings.businessName)
        binding.agentInput.setText(settings.agentName)
        binding.phoneInput.setText(settings.phone)
        binding.feeInput.setText(settings.managementFee.toInt().toString())
        binding.prefixInput.setText(settings.receiptPrefix)
        binding.collectorInput.setText(settings.defaultCollector)

        binding.saveButton.setOnClickListener { save() }
        binding.cancelButton.setOnClickListener { findNavController().popBackStack() }
    }

    private fun save() {
        val fee = binding.feeInput.text.toString().trim().toDoubleOrNull() ?: 10.0
        if (fee < 0.0 || fee > 100.0) {
            binding.feeLayout.error = "The fee must be between 0 and 100"
            return
        }
        binding.feeLayout.error = null

        val settings = container.settings
        settings.businessName = binding.businessInput.text.toString().trim()
        settings.agentName = binding.agentInput.text.toString().trim()
        settings.phone = binding.phoneInput.text.toString().trim()
        settings.managementFee = fee
        settings.receiptPrefix = binding.prefixInput.text.toString().trim()
        settings.defaultCollector = binding.collectorInput.text.toString().trim()

        Toast.makeText(requireContext(), "Settings saved", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
