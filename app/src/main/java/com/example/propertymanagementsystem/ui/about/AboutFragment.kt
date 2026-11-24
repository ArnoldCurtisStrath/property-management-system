package com.example.propertymanagementsystem.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.databinding.FragmentAboutBinding

/**
 * Shows what the app is about and who built it.
 *
 * The screen also holds the names of the people in the group, which is
 * useful when the app is presented to the lecturer.
 */
class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appVersion.text = getString(R.string.about_version, "2.0")
        binding.teamList.text = getString(
            R.string.about_team,
            "Arnold Wangai Mbici — Lead, ledger, architecture and documentation\n" +
                "Kabutha Joe Mwangi — Buildings, units and the Today screen\n" +
                "Kwendo Andrew Abel — Tenants, ledger and receipts\n" +
                "Lusiola Erika Wambui — Payments, M-Pesa and owner statements\n" +
                "Leon Onyancha Ouya — Repairs, weather and the property pulse\n" +
                "Naaadir Abdulaziz Rana — API, daily brief and release"
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
