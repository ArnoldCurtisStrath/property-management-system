package com.example.propertymanagementsystem.data.ledger

import com.example.propertymanagementsystem.data.model.Building
import com.example.propertymanagementsystem.data.model.Expense
import com.example.propertymanagementsystem.data.model.MaintenanceRequest
import com.example.propertymanagementsystem.data.model.Payment
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.data.model.Tenant
import com.example.propertymanagementsystem.util.Dates

/**
 * Rolls the tenant ledgers up into the three views the agent needs:
 * the day book, the building cards, and the owner statement.
 */
object PortfolioCalculator {

    fun today(
        buildings: List<Building>,
        units: List<Property>,
        tenants: List<Tenant>,
        payments: List<Payment>,
        expenses: List<Expense>,
        maintenance: List<MaintenanceRequest>,
        ledgers: List<TenantLedger>,
        month: String = Dates.currentMonth()
    ): TodayState {
        val activeLedgers = ledgers.filter { it.tenant.active }
        val expected = expectedForMonth(activeLedgers, month)
        val collected = payments
            .filter { it.month == month && it.status == Payment.STATUS_PAID }
            .sumOf { it.amount }
        val monthPayments = payments.filter {
            it.month == month && it.status == Payment.STATUS_PAID
        }
        val outstanding = activeLedgers.filter { it.owes }.sumOf { it.balance }
        val vacantUnits = units.filter { it.status == Property.STATUS_VACANT }
        val occupied = units.count { it.status == Property.STATUS_OCCUPIED }
        val openRepairs = maintenance.filter { it.status != MaintenanceRequest.STATUS_DONE }
        val monthExpenses = expenses.filter { Dates.monthOf(it.date) == month }.sumOf { it.amount }
        val sortedPayments = payments
            .filter { it.status == Payment.STATUS_PAID }
            .sortedWith(compareByDescending<Payment> { it.date }.thenByDescending { it.id })

        return TodayState(
            month = month,
            buildingCount = buildings.size,
            unitCount = units.size,
            occupiedCount = occupied,
            vacantCount = vacantUnits.size,
            rentRoll = units.sumOf { it.rent },
            expected = expected,
            collected = collected,
            cashCollected = monthPayments
                .filter { it.method == Payment.METHOD_CASH }
                .sumOf { it.amount },
            mpesaCollected = monthPayments
                .filter { it.method == Payment.METHOD_MPESA }
                .sumOf { it.amount },
            receiptCount = monthPayments.size,
            outstanding = outstanding,
            collectionRate = if (expected <= 0.0) 0.0 else (collected / expected).coerceIn(0.0, 1.0),
            arrearsCount = activeLedgers.count { it.owes },
            arrearsAmount = outstanding,
            vacancyLoss = vacantUnits.sumOf { it.rent },
            openRepairs = openRepairs.size,
            urgentRepairs = openRepairs.count {
                it.priority == MaintenanceRequest.PRIORITY_URGENT ||
                    it.priority == MaintenanceRequest.PRIORITY_HIGH
            },
            expensesThisMonth = monthExpenses,
            netThisMonth = collected - monthExpenses,
            recentReceipts = sortedPayments.take(5),
            topDebtors = activeLedgers.filter { it.owes }
                .sortedByDescending { it.balance }
                .take(5),
            buildings = buildings.map { building ->
                buildingSummary(building, units, tenants, payments, expenses, maintenance, ledgers, month)
            }
        )
    }

    fun buildingSummary(
        building: Building,
        units: List<Property>,
        tenants: List<Tenant>,
        payments: List<Payment>,
        expenses: List<Expense>,
        maintenance: List<MaintenanceRequest>,
        ledgers: List<TenantLedger>,
        month: String = Dates.currentMonth()
    ): BuildingSummary {
        val buildingUnits = units.filter { it.buildingId == building.id }
        val unitIds = buildingUnits.map { it.id }.toSet()
        val buildingTenants = tenants.filter { it.propertyId in unitIds }
        val activeTenants = buildingTenants.filter { it.active }
        val buildingLedgers = ledgers.filter { it.tenant.propertyId in unitIds && it.tenant.active }
        val expected = expectedForMonth(buildingLedgers, month)
        val collected = payments
            .filter {
                it.buildingId == building.id &&
                    it.month == month &&
                    it.status == Payment.STATUS_PAID
            }
            .sumOf { it.amount }
        val arrears = buildingLedgers.filter { it.owes }.sumOf { it.balance }
        val monthExpenses = expenses
            .filter { it.buildingId == building.id && Dates.monthOf(it.date) == month }
            .sumOf { it.amount }
        val managementFee = collected * building.managementFee / 100.0
        val occupied = buildingUnits.count { it.status == Property.STATUS_OCCUPIED }
        val vacant = buildingUnits.count { it.status == Property.STATUS_VACANT }
        val openRepairs = maintenance.count {
            it.propertyId in unitIds && it.status != MaintenanceRequest.STATUS_DONE
        }

        return BuildingSummary(
            building = building,
            unitCount = buildingUnits.size,
            occupiedCount = occupied,
            vacantCount = vacant,
            tenantCount = activeTenants.size,
            rentRoll = buildingUnits.sumOf { it.rent },
            expected = expected,
            collected = collected,
            arrears = arrears,
            arrearsCount = buildingLedgers.count { it.owes },
            vacancyLoss = buildingUnits
                .filter { it.status == Property.STATUS_VACANT }
                .sumOf { it.rent },
            expenses = monthExpenses,
            managementFee = managementFee,
            netToOwner = collected - monthExpenses - managementFee,
            openRepairs = openRepairs,
            occupancyRate = if (buildingUnits.isEmpty()) 0.0 else occupied.toDouble() / buildingUnits.size
        )
    }

    fun statement(
        building: Building,
        units: List<Property>,
        tenants: List<Tenant>,
        payments: List<Payment>,
        expenses: List<Expense>,
        maintenance: List<MaintenanceRequest>,
        ledgers: List<TenantLedger>,
        month: String,
        preparedBy: String
    ): Statement {
        val summary = buildingSummary(building, units, tenants, payments, expenses, maintenance, ledgers, month)
        val monthExpenses = expenses.filter {
            it.buildingId == building.id && Dates.monthOf(it.date) == month
        }
        val byCategory = monthExpenses
            .groupBy { it.category }
            .map { (category, rows) -> ExpenseLine(category, rows.sumOf { it.amount }) }
            .sortedByDescending { it.amount }
        val monthPayments = payments.filter {
            it.buildingId == building.id &&
                it.month == month &&
                it.status == Payment.STATUS_PAID
        }

        return Statement(
            building = building,
            month = month,
            statementNumber = "ST-" + month.replace("-", "") + "-B" + building.id,
            unitCount = summary.unitCount,
            occupiedCount = summary.occupiedCount,
            tenantCount = summary.tenantCount,
            rentRoll = summary.rentRoll,
            expected = summary.expected,
            collected = summary.collected,
            arrears = summary.arrears,
            vacancyLoss = summary.vacancyLoss,
            expenses = summary.expenses,
            expensesByCategory = byCategory,
            managementFeePercent = building.managementFee,
            managementFee = summary.managementFee,
            netToOwner = summary.netToOwner,
            cashCollected = monthPayments
                .filter { it.method == Payment.METHOD_CASH }
                .sumOf { it.amount },
            mpesaCollected = monthPayments
                .filter { it.method == Payment.METHOD_MPESA }
                .sumOf { it.amount },
            bankCollected = monthPayments
                .filter { it.method == Payment.METHOD_BANK }
                .sumOf { it.amount },
            openRepairs = summary.openRepairs,
            preparedFor = building.ownerName.ifBlank { "The owner" },
            preparedBy = preparedBy,
            generatedAt = Dates.today()
        )
    }

    private fun expectedForMonth(ledgers: List<TenantLedger>, month: String): Double =
        ledgers.sumOf { ledger ->
            ledger.charges.filter { it.month == month }.sumOf { it.amount }
        }
}
