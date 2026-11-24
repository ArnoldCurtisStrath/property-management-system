package com.example.propertymanagementsystem.ui.expenses

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.propertymanagementsystem.SahihiApp
import com.example.propertymanagementsystem.data.model.Building
import com.example.propertymanagementsystem.data.model.Expense
import com.example.propertymanagementsystem.data.model.Payment
import com.example.propertymanagementsystem.databinding.FragmentExpenseFormBinding
import com.example.propertymanagementsystem.util.Dates
import com.example.propertymanagementsystem.util.Money
import com.example.propertymanagementsystem.util.Validation
import kotlinx.coroutines.launch
import java.util.Calendar

class ExpenseFormFragment : Fragment() {

    private var _binding: FragmentExpenseFormBinding? = null
    private val binding get() = _binding!!

    private val container by lazy { (requireActivity().application as SahihiApp).container }
    private var buildings: List<Building> = emptyList()
    private var preferredBuildingId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferredBuildingId = arguments?.getLong("buildingId", 0L) ?: 0L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.categorySpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            Expense.allCategories
        )
        binding.methodSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            Payment.allMethods
        )
        binding.dateInput.setText(Dates.today())
        binding.dateInput.setOnClickListener { openDatePicker() }

        container.buildings.getAll().observe(viewLifecycleOwner) { list ->
            buildings = list
            binding.buildingSpinner.adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                list.map { it.name }
            )
            if (preferredBuildingId != 0L) {
                val index = list.indexOfFirst { it.id == preferredBuildingId }
                if (index >= 0) binding.buildingSpinner.setSelection(index)
            }
        }

        binding.saveButton.setOnClickListener { save() }
        binding.cancelButton.setOnClickListener { findNavController().popBackStack() }
    }

    private fun openDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                binding.dateInput.setText(
                    String.format("%04d-%02d-%02d", year, month + 1, day)
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun save() {
        val building = buildings.getOrNull(binding.buildingSpinner.selectedItemPosition)
        if (building == null) {
            Toast.makeText(requireContext(), "Add a building first", Toast.LENGTH_LONG).show()
            return
        }

        val amount = Money.parse(binding.amountInput.text?.toString().orEmpty())
        if (amount == null || !Validation.isValidAmount(amount)) {
            binding.amountLayout.error = "Enter an amount greater than zero"
            return
        }
        binding.amountLayout.error = null

        val date = binding.dateInput.text?.toString().orEmpty().trim()
        if (!Validation.isValidDate(date)) {
            Toast.makeText(requireContext(), "Choose a valid date", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = Expense(
            buildingId = building.id,
            category = binding.categorySpinner.selectedItem.toString(),
            amount = amount,
            date = date,
            vendor = binding.vendorInput.text?.toString()?.trim().orEmpty(),
            method = binding.methodSpinner.selectedItem.toString(),
            reference = binding.referenceInput.text?.toString()?.trim()?.ifBlank { null },
            note = binding.noteInput.text?.toString()?.trim()?.ifBlank { null },
            recordedBy = container.settings.defaultCollector
        )

        viewLifecycleOwner.lifecycleScope.launch {
            container.expenses.add(expense)
            Toast.makeText(requireContext(), "Expense saved", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
