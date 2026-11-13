package com.example.propertymanagementsystem.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.propertymanagementsystem.databinding.FragmentDashboardBinding

/**
 * The first screen the user sees after signing in.
 *
 * It shows a short summary of the portfolio. The numbers come from the
 * ViewModel, which reads them from the database, so they change by themselves
 * when a property is added, changed or removed.
 */
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // The total number of properties that have been saved.
        viewModel.propertyCount.observe(viewLifecycleOwner) { count ->
            binding.propertyCount.text = count.toString()
        }

        // How many of them are rented out at the moment.
        viewModel.occupiedCount.observe(viewLifecycleOwner) { count ->
            binding.occupiedCount.text = count.toString()
        }

        // How many are still empty and could be advertised.
        viewModel.vacantCount.observe(viewLifecycleOwner) { count ->
            binding.vacantCount.text = count.toString()
        }
    }

    // The binding is cleared when the screen is gone, otherwise the view
    // would be held in memory and the app would slowly run out of space.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
