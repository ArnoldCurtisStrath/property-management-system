package com.example.propertymanagementsystem.ui.more

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.propertymanagementsystem.R
import com.example.propertymanagementsystem.SahihiApp
import com.example.propertymanagementsystem.data.DemoData
import com.example.propertymanagementsystem.databinding.FragmentMoreBinding
import com.example.propertymanagementsystem.ui.auth.LoginActivity
import com.example.propertymanagementsystem.util.SessionManager
import com.example.propertymanagementsystem.work.WorkScheduler
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MoreFragment : Fragment() {

    private var _binding: FragmentMoreBinding? = null
    private val binding get() = _binding!!

    private val container by lazy { (requireActivity().application as SahihiApp).container }
    private val session by lazy { SessionManager(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val business = container.settings.businessName.ifBlank { "My Property Agency" }
        binding.businessName.text = business
        binding.profileInitials.text = business.take(2).uppercase()
        binding.accountEmail.text = session.signedInEmail().ifBlank { "Signed in" }

        binding.editProfileButton.setOnClickListener { openSettings() }
        binding.settingsRow.setOnClickListener { openSettings() }
        binding.buildingsRow.setOnClickListener {
            findNavController().navigate(R.id.buildingListFragment)
        }
        binding.peopleRow.setOnClickListener {
            findNavController().navigate(R.id.tenantListFragment)
        }
        binding.repairsRow.setOnClickListener {
            findNavController().navigate(R.id.maintenanceListFragment)
        }
        binding.aboutRow.setOnClickListener {
            findNavController().navigate(R.id.aboutFragment)
        }

        binding.loadDemoButton.setOnClickListener { confirmDemo() }
        binding.briefButton.setOnClickListener {
            WorkScheduler.runBriefNow(requireContext())
            Toast.makeText(requireContext(), "The daily brief is on its way", Toast.LENGTH_SHORT)
                .show()
        }

        binding.logoutButton.setOnClickListener {
            session.signOut()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun openSettings() {
        findNavController().navigate(R.id.settingsFragment)
    }

    private fun confirmDemo() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Load the sample portfolio?")
            .setMessage(
                "This adds three buildings, ten units, tenants, payments, expenses and repairs. " +
                    "It does not delete anything that is already here."
            )
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Load it") { _, _ -> loadDemo() }
            .show()
    }

    private fun loadDemo() {
        binding.loadDemoButton.isEnabled = false
        binding.loadDemoButton.text = "Loading…"
        viewLifecycleOwner.lifecycleScope.launch {
            val loaded = withContext(Dispatchers.IO) { DemoData.seed(container) }
            binding.loadDemoButton.isEnabled = true
            binding.loadDemoButton.text = "Load sample portfolio"
            Toast.makeText(
                requireContext(),
                if (loaded) "Sample portfolio loaded" else "The sample portfolio is already here",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
