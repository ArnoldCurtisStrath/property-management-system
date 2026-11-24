package com.example.propertymanagementsystem.ui.buildings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.data.ledger.TodayState
import com.example.propertymanagementsystem.databinding.FragmentBuildingListBinding
import com.example.propertymanagementsystem.util.Money

class BuildingListFragment : Fragment() {

    private var _binding: FragmentBuildingListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BuildingViewModel by viewModels()
    private lateinit var adapter: BuildingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuildingListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = BuildingAdapter { summary ->
            findNavController().navigate(
                R.id.buildingDetailsFragment,
                Bundle().apply { putLong("buildingId", summary.building.id) }
            )
        }
        binding.buildingList.layoutManager = LinearLayoutManager(requireContext())
        binding.buildingList.adapter = adapter

        binding.addBuildingButton.setOnClickListener {
            findNavController().navigate(R.id.buildingFormFragment)
        }

        viewModel.summaries.observe(viewLifecycleOwner) { summaries ->
            adapter.submitList(summaries)
            binding.emptyMessage.visibility =
                if (summaries.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.today.observe(viewLifecycleOwner) { today ->
            renderSummary(today)
        }
    }

    private fun renderSummary(today: TodayState) {
        binding.totalBuildings.text = today.buildingCount.toString()
        binding.totalUnits.text = today.unitCount.toString()
        binding.totalArrears.text = Money.format(today.arrearsAmount)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
