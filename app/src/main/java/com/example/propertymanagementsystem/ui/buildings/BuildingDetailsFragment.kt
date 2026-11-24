package com.example.propertymanagementsystem.ui.buildings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.data.ledger.BuildingSummary
import com.example.propertymanagementsystem.data.ledger.TenantLedger
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.data.remote.WeatherApi
import com.example.propertymanagementsystem.databinding.FragmentBuildingDetailsBinding
import com.example.propertymanagementsystem.databinding.ViewWeatherCardBinding
import com.example.propertymanagementsystem.util.Dates
import com.example.propertymanagementsystem.util.Money
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BuildingDetailsFragment : Fragment() {

    private var _binding: FragmentBuildingDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BuildingDetailsViewModel by viewModels()
    private var buildingId: Long = 0

    private lateinit var unitAdapter: UnitRowAdapter
    private lateinit var expenseAdapter: ExpenseRowAdapter
    private var weatherBinding: ViewWeatherCardBinding? = null
    private var lastWeatherAddress: String = ""

    private var units: List<Property> = emptyList()
    private var ledgers: List<TenantLedger> = emptyList()
    private var unitRows: List<UnitRow> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildingId = arguments?.getLong("buildingId", 0L) ?: 0L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuildingDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        unitAdapter = UnitRowAdapter { row ->
            if (row.hasTenant) {
                findNavController().navigate(
                    R.id.tenantDetailsFragment,
                    Bundle().apply { putLong("tenantId", row.tenantId) }
                )
            } else {
                findNavController().navigate(
                    R.id.propertyDetailsFragment,
                    Bundle().apply { putLong("propertyId", row.unit.id) }
                )
            }
        }
        binding.unitList.layoutManager = LinearLayoutManager(requireContext())
        binding.unitList.adapter = unitAdapter

        expenseAdapter = ExpenseRowAdapter()
        binding.expenseList.layoutManager = LinearLayoutManager(requireContext())
        binding.expenseList.adapter = expenseAdapter

        weatherBinding = ViewWeatherCardBinding.inflate(
            layoutInflater, binding.weatherContainer, false
        )
        binding.weatherContainer.addView(weatherBinding?.root)
        weatherBinding?.retryButton?.setOnClickListener {
            if (lastWeatherAddress.isNotBlank()) loadWeather(lastWeatherAddress)
        }

        binding.editButton.setOnClickListener {
            findNavController().navigate(
                R.id.buildingFormFragment,
                Bundle().apply { putLong("buildingId", buildingId) }
            )
        }
        binding.addUnitButton.setOnClickListener {
            findNavController().navigate(
                R.id.propertyFormFragment,
                Bundle().apply { putLong("buildingId", buildingId) }
            )
        }
        binding.addExpenseButton.setOnClickListener {
            findNavController().navigate(
                R.id.expenseFormFragment,
                Bundle().apply { putLong("buildingId", buildingId) }
            )
        }
        binding.repairsButton.setOnClickListener {
            findNavController().navigate(R.id.maintenanceListFragment)
        }
        binding.statementButton.setOnClickListener {
            findNavController().navigate(
                R.id.ownerStatementFragment,
                Bundle().apply {
                    putLong("buildingId", buildingId)
                    putString("month", viewModel.month)
                }
            )
        }

        viewModel.building(buildingId).observe(viewLifecycleOwner) { building ->
            if (building == null) return@observe
            binding.buildingName.text = building.name
            binding.buildingOwner.text = listOf(
                building.ownerName.ifBlank { "No owner set" },
                building.ownerPhone
            ).filter { it.isNotBlank() }.joinToString(" · ")
            binding.buildingMeta.text = listOf(building.address, building.type)
                .filter { it.isNotBlank() }.joinToString(" · ")
            binding.netLabel.text = "NET TO OWNER · " + Dates.monthLabel(viewModel.month).uppercase()
            if (building.address.isNotBlank() && building.address != lastWeatherAddress) {
                lastWeatherAddress = building.address
                loadWeather(building.address)
            }
        }

        viewModel.summary(buildingId).observe(viewLifecycleOwner) { summary ->
            if (summary != null) renderSummary(summary)
        }

        viewModel.units(buildingId).observe(viewLifecycleOwner) {
            units = it
            renderUnits()
        }

        viewModel.ledgers.observe(viewLifecycleOwner) {
            ledgers = it
            renderUnits()
        }

        viewModel.expenses(buildingId).observe(viewLifecycleOwner) { expenses ->
            expenseAdapter.submitList(expenses)
            val total = expenses.sumOf { it.amount }
            binding.expensesTotal.text = Money.format(total)
            binding.emptyExpenses.visibility =
                if (expenses.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun renderSummary(summary: BuildingSummary) {
        binding.netAmount.text = Money.format(summary.netToOwner)
        binding.netAmount.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (summary.netToOwner < 0) R.color.stamp else R.color.ink
            )
        )
        binding.netDetail.text = "Collected ${Money.format(summary.collected)} · " +
            "Expenses ${Money.format(summary.expenses)} · " +
            "Fee ${Money.format(summary.managementFee)}"
        binding.rentRollValue.text = Money.format(summary.rentRoll)
        binding.collectedValue.text = Money.format(summary.collected)
        binding.arrearsValue.text = Money.format(summary.arrears)
        binding.arrearsValue.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (summary.arrears > 0) R.color.danger else R.color.success
            )
        )
        binding.occupancyValue.text = "${(summary.occupancyRate * 100).toInt()}%"
        binding.vacancyLossValue.text = Money.format(summary.vacancyLoss)
        binding.openRepairsValue.text = summary.openRepairs.toString()
    }

    private fun renderUnits() {
        unitRows = units.map { unit ->
            val ledger = ledgers.firstOrNull {
                it.unit?.id == unit.id && it.tenant.active
            }
            if (ledger != null) {
                UnitRow(
                    unit = unit,
                    tenantId = ledger.tenant.id,
                    tenantName = ledger.tenant.name,
                    amount = ledger.balance,
                    statusLabel = ledger.statusLabel,
                    hasTenant = true,
                    isOverdue = ledger.owes && ledger.daysLate > 0,
                    isVacant = false
                )
            } else {
                UnitRow(
                    unit = unit,
                    tenantId = 0L,
                    tenantName = unit.type,
                    amount = unit.rent,
                    statusLabel = unit.status,
                    hasTenant = false,
                    isOverdue = unit.status == Property.STATUS_MAINTENANCE,
                    isVacant = true
                )
            }
        }
        unitAdapter.submitList(unitRows)
        binding.emptyUnits.visibility =
            if (unitRows.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun loadWeather(address: String) {
        val card = weatherBinding ?: return
        card.weatherProgress.visibility = View.VISIBLE
        card.weatherPlace.text = ""
        card.weatherDescription.text = ""
        card.weatherForecast.text = ""
        card.weatherError.visibility = View.GONE
        card.retryButton.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            val weather = withContext(Dispatchers.IO) { WeatherApi().load(address) }
            card.weatherProgress.visibility = View.GONE
            if (weather == null) {
                card.weatherError.visibility = View.VISIBLE
                card.weatherError.text = getString(R.string.weather_failed)
                card.retryButton.visibility = View.VISIBLE
                return@launch
            }
            card.weatherPlace.text = weather.place
            card.weatherTemperature.text =
                getString(R.string.weather_temperature, weather.temperature)
            card.weatherDescription.text = if (weather.rainLikely) {
                weather.description + " · plan visits around the rain"
            } else {
                weather.description
            }
            card.weatherForecast.text = weather.days.joinToString(" · ") { day ->
                Dates.shortDate(day.date) + " " + day.maxTemperature.toInt() +
                    "° · " + day.rainChance + "% rain"
            }.ifBlank { "Forecast unavailable" }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        weatherBinding = null
        _binding = null
    }
}
