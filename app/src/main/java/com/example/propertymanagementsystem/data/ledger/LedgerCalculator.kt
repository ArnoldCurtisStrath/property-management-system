package com.example.propertymanagementsystem.data.ledger

import com.example.propertymanagementsystem.data.model.Building
import com.example.propertymanagementsystem.data.model.Payment
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.data.model.Tenant
import com.example.propertymanagementsystem.util.Dates

/**
 * The ledger engine.
 *
 * Rent charges are derived from the tenancy. Payments are applied to the
 * oldest unpaid charge first. The balance is the difference. Everything here
 * is pure Kotlin so it can be unit-tested without Android.
 */
object LedgerCalculator {

    const val AGING_PAID = "Paid"
    const val AGING_CURRENT = "Current"
    const val AGING_1_30 = "1-30 days"
    const val AGING_31_60 = "31-60 days"
    const val AGING_61_90 = "61-90 days"
    const val AGING_90_PLUS = "90+ days"

    fun buildLedgers(
        buildings: List<Building>,
        units: List<Property>,
        tenants: List<Tenant>,
        payments: List<Payment>,
        asOfMonth: String = Dates.currentMonth(),
        asOfDate: String = Dates.today()
    ): List<TenantLedger> {
        val buildingById = buildings.associateBy { it.id }
        val unitById = units.associateBy { it.id }
        val paidPayments = payments.filter { it.status == Payment.STATUS_PAID }
        val paymentsByTenant = paidPayments.groupBy { it.tenantId }

        return tenants.map { tenant ->
            val unit = unitById[tenant.propertyId]
            val building = unit?.let { buildingById[it.buildingId] }
            ledgerFor(
                tenant = tenant,
                unit = unit,
                building = building,
                payments = paymentsByTenant[tenant.id].orEmpty(),
                asOfMonth = asOfMonth,
                asOfDate = asOfDate
            )
        }
    }

    fun ledgerFor(
        tenant: Tenant,
        unit: Property?,
        building: Building?,
        payments: List<Payment>,
        asOfMonth: String = Dates.currentMonth(),
        asOfDate: String = Dates.today()
    ): TenantLedger {
        val charges = chargesFor(tenant, asOfMonth)
        val paidPayments = payments
            .filter { it.status == Payment.STATUS_PAID }
            .sortedWith(compareBy({ it.date }, { it.id }))

        val allocated = charges.map { it.copy(paid = 0.0) }.toMutableList()

        for (payment in paidPayments) {
            var left = payment.amount
            for (index in allocated.indices) {
                if (left <= 0.005) break
                val charge = allocated[index]
                val remaining = charge.remaining
                if (remaining <= 0.005) continue
                val applied = minOf(left, remaining)
                allocated[index] = charge.copy(paid = charge.paid + applied)
                left -= applied
            }
        }

        val totalCharged = allocated.sumOf { it.amount }
        val totalPaid = paidPayments.sumOf { it.amount }
        val balance = totalCharged - totalPaid
        val credit = if (balance < 0.0) -balance else 0.0

        val oldestUnpaid = allocated.firstOrNull { !it.isPaid }
        val daysLate = oldestUnpaid?.let { Dates.daysLate(it.dueDate, asOfDate) } ?: 0
        val bucket = agingBucket(balance, daysLate)
        val entries = buildEntries(allocated, paidPayments)

        return TenantLedger(
            tenant = tenant,
            unit = unit,
            building = building,
            charges = allocated,
            payments = paidPayments,
            entries = entries,
            balance = round2(balance),
            credit = round2(credit),
            totalCharged = round2(totalCharged),
            totalPaid = round2(totalPaid),
            oldestUnpaidMonth = oldestUnpaid?.month,
            daysLate = daysLate,
            agingBucket = bucket
        )
    }

    fun chargesFor(tenant: Tenant, asOfMonth: String = Dates.currentMonth()): List<RentCharge> {
        if (tenant.rent <= 0.0) return emptyList()

        val startMonth = Dates.monthOf(tenant.startDate)
        var endMonth = tenant.endDate?.let { Dates.monthOf(it) } ?: asOfMonth
        if (endMonth > asOfMonth) endMonth = asOfMonth
        if (startMonth > endMonth) return emptyList()

        val charges = mutableListOf<RentCharge>()
        for (month in Dates.monthsBetween(startMonth, endMonth)) {
            val daysInMonth = Dates.daysInMonth(month)
            var firstDay = 1
            var lastDay = daysInMonth

            if (month == startMonth) {
                firstDay = Dates.dayOfMonth(tenant.startDate).coerceIn(1, daysInMonth)
            }
            if (month == endMonth && tenant.endDate != null) {
                lastDay = Dates.dayOfMonth(tenant.endDate).coerceIn(1, daysInMonth)
            }
            if (lastDay < firstDay) continue

            var amount = tenant.rent
            if (firstDay > 1 || lastDay < daysInMonth) {
                val occupiedDays = Dates.daysBetweenInclusive(
                    "$month-" + firstDay.toString().padStart(2, '0'),
                    "$month-" + lastDay.toString().padStart(2, '0')
                )
                amount = tenant.rent * occupiedDays / daysInMonth
            }
            if (amount <= 0.005) continue

            var dueDate = Dates.dueDate(month, tenant.dueDay)
            if (month == startMonth && dueDate < tenant.startDate) {
                dueDate = tenant.startDate
            }
            charges.add(RentCharge(month, round2(amount), dueDate))
        }
        return charges
    }

    private fun buildEntries(
        charges: List<RentCharge>,
        payments: List<Payment>
    ): List<LedgerEntry> {
        data class Sortable(
            val date: String,
            val order: Int,
            val id: Long,
            val entry: (Double) -> LedgerEntry
        )

        val items = mutableListOf<Sortable>()
        charges.forEach { charge ->
            items.add(
                Sortable(charge.dueDate, 0, charge.month.hashCode().toLong()) { balance ->
                    LedgerEntry(
                        type = LedgerEntryType.CHARGE,
                        date = charge.dueDate,
                        label = "Rent · ${Dates.monthShortLabel(charge.month)}",
                        amount = charge.amount,
                        balanceAfter = balance
                    )
                }
            )
        }
        payments.forEach { payment ->
            items.add(
                Sortable(payment.date, 1, payment.id) { balance ->
                    LedgerEntry(
                        type = LedgerEntryType.PAYMENT,
                        date = payment.date,
                        label = payment.method + " payment",
                        amount = payment.amount,
                        balanceAfter = balance,
                        receiptNumber = payment.receiptNumber,
                        method = payment.method,
                        reference = payment.reference,
                        paymentId = payment.id
                    )
                }
            )
        }

        val entries = mutableListOf<LedgerEntry>()
        var running = 0.0
        items.sortedWith(compareBy({ it.date }, { it.order }, { it.id })).forEach { item ->
            // The lambda is given the balance after the item, so the caller
            // can see how the entry moved the account.
            val isCharge = item.order == 0
            val amount = item.entry(0.0).amount
            running += if (isCharge) amount else -amount
            entries.add(item.entry(round2(running)))
        }
        return entries
    }

    fun agingBucket(balance: Double, daysLate: Int): String {
        if (balance <= 0.005) return AGING_PAID
        return when {
            daysLate <= 0 -> AGING_CURRENT
            daysLate <= 30 -> AGING_1_30
            daysLate <= 60 -> AGING_31_60
            daysLate <= 90 -> AGING_61_90
            else -> AGING_90_PLUS
        }
    }

    fun round2(value: Double): Double = Math.round(value * 100.0) / 100.0
}
