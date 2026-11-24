package com.example.propertymanagementsystem.ui.statements

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.propertymanagementsystem.data.ledger.Statement
import com.example.propertymanagementsystem.databinding.FragmentOwnerStatementBinding
import com.example.propertymanagementsystem.util.Dates
import com.example.propertymanagementsystem.util.Money

class OwnerStatementFragment : Fragment() {

    private var _binding: FragmentOwnerStatementBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatementViewModel by viewModels()
    private var statement: Statement? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOwnerStatementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buildingId = arguments?.getLong("buildingId", 0L) ?: 0L
        val month = arguments?.getString("month") ?: Dates.currentMonth()

        binding.doneButton.setOnClickListener { findNavController().popBackStack() }
        binding.shareButton.setOnClickListener { share() }

        viewModel.load(buildingId, month)
        viewModel.statement.observe(viewLifecycleOwner) { loaded ->
            if (loaded == null) {
                Toast.makeText(requireContext(), "The statement could not be built", Toast.LENGTH_LONG)
                    .show()
                return@observe
            }
            statement = loaded
            render(loaded)
        }
    }

    private fun render(statement: Statement) {
        binding.statementNumber.text = statement.statementNumber
        binding.buildingName.text = statement.building.name
        binding.statementMeta.text = listOf(
            Dates.monthLabel(statement.month),
            "Prepared for " + statement.preparedFor
        ).joinToString(" · ")
        binding.rentRollValue.text = Money.format(statement.rentRoll)
        binding.collectedValue.text = Money.format(statement.collected)
        binding.arrearsValue.text = Money.format(statement.arrears)
        binding.vacancyLossValue.text = Money.format(statement.vacancyLoss)
        binding.expensesValue.text = Money.format(statement.expenses)
        binding.feeLabel.text = "Management fee (" +
            statement.managementFeePercent.toInt() + "%)"
        binding.feeValue.text = Money.format(statement.managementFee)
        binding.netValue.text = Money.format(statement.netToOwner)
        binding.preparedBy.text = "Prepared by ${statement.preparedBy} · ${statement.generatedAt}"

        binding.expenseLines.text = if (statement.expensesByCategory.isEmpty()) {
            "No expenses this month."
        } else {
            statement.expensesByCategory.joinToString("\n") { line ->
                line.category + " — " + Money.format(line.amount)
            }
        }
    }

    private fun share() {
        val statement = statement ?: return
        val lines = buildString {
            appendLine("SAHIHI · OWNER STATEMENT")
            appendLine(statement.building.name)
            appendLine(Dates.monthLabel(statement.month))
            appendLine("Prepared for ${statement.preparedFor} by ${statement.preparedBy}")
            appendLine("Statement ${statement.statementNumber}")
            appendLine()
            appendLine("Rent roll: ${Money.format(statement.rentRoll)}")
            appendLine("Collected: ${Money.format(statement.collected)}")
            appendLine("Arrears: ${Money.format(statement.arrears)}")
            appendLine("Vacancy loss: ${Money.format(statement.vacancyLoss)}")
            appendLine("Expenses: ${Money.format(statement.expenses)}")
            statement.expensesByCategory.forEach { line ->
                appendLine("  · ${line.category}: ${Money.format(line.amount)}")
            }
            appendLine(
                "Management fee (${statement.managementFeePercent.toInt()}%): " +
                    Money.format(statement.managementFee)
            )
            appendLine()
            appendLine("NET TO OWNER: ${Money.format(statement.netToOwner)}")
            appendLine()
            appendLine("Every shilling, accounted for.")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Owner statement · ${statement.building.name}")
            putExtra(Intent.EXTRA_TEXT, lines)
        }
        startActivity(Intent.createChooser(intent, "Share owner statement"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
