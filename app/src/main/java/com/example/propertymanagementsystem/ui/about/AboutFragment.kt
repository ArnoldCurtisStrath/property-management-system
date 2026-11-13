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

        binding.appVersion.text = getString(R.string.about_version, "1.0")
        binding.teamList.text = getString(
            R.string.about_team,
            "Arnold Wangai Mbici - Lead\n" +
                "Kabutha Joe Mwangi - Properties\n" +
                "Kwendo Andrew Abel - Tenants\n" +
                "Lusiola Erika Wambui - Payments\n" +
                "Leon Onyancha Ouya - Maintenance\n" +
                "Naaadir Abdulaziz Rana - API and background work"
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
