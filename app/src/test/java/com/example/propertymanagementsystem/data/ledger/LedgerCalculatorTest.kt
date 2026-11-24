package com.example.propertymanagementsystem.data.ledger

import com.example.propertymanagementsystem.data.model.Building
import com.example.propertymanagementsystem.data.model.Payment
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.data.model.Tenant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LedgerCalculatorTest {

    private val building = Building(
        id = 1,
        name = "Kasarani",
        address = "Kasarani",
        type = Building.TYPE_BEDSITTER_BLOCK,
        ownerName = "Owner",
        ownerPhone = "",
        ownerEmail = ""
    )

    private val unit = Property(
        id = 10,
        buildingId = 1,
        name = "A1",
        address = "Kasarani",
        type = Property.TYPE_BEDSITTER,
        rent = 7000.0,
        status = Property.STATUS_OCCUPIED
    )

    private fun tenant(
        rent: Double = 7000.0,
        start: String = "2026-11-01",
        dueDay: Int = 5,
        end: String? = null
    ) = Tenant(
        id = 1,
        name = "Grace",
        phone = "0712345678",
        email = "",
        propertyId = 10,
        rent = rent,
        startDate = start,
        endDate = end,
        dueDay = dueDay,
        active = true
    )

    private fun payment(
        id: Long,
        amount: Double,
        date: String,
        month: String
    ) = Payment(
        id = id,
        tenantId = 1,
        propertyId = 10,
        buildingId = 1,
        amount = amount,
        date = date,
        month = month,
        method = Payment.METHOD_CASH,
        status = Payment.STATUS_PAID
    )

    @Test
    fun fullPaymentSettlesTheMonth() {
        val ledger = LedgerCalculator.ledgerFor(
            tenant = tenant(),
            unit = unit,
            building = building,
            payments = listOf(payment(1, 7000.0, "2026-11-05", "2026-11")),
            asOfMonth = "2026-11",
            asOfDate = "2026-11-24"
        )

        assertEquals(7000.0, ledger.totalCharged, 0.01)
        assertEquals(7000.0, ledger.totalPaid, 0.01)
        assertEquals(0.0, ledger.balance, 0.01)
        assertFalse(ledger.owes)
        assertTrue(ledger.isSettled)
        assertEquals(LedgerCalculator.AGING_PAID, ledger.agingBucket)
    }

    @Test
    fun partialPaymentCarriesTheBalance() {
        val ledger = LedgerCalculator.ledgerFor(
            tenant = tenant(),
            unit = unit,
            building = building,
            payments = listOf(payment(1, 3000.0, "2026-11-05", "2026-11")),
            asOfMonth = "2026-11",
            asOfDate = "2026-11-24"
        )

        assertEquals(4000.0, ledger.balance, 0.01)
        assertTrue(ledger.owes)
        assertEquals("2026-11", ledger.oldestUnpaidMonth)
        assertEquals(19, ledger.daysLate)
        assertEquals(LedgerCalculator.AGING_1_30, ledger.agingBucket)
    }

    @Test
    fun paymentIsAppliedToTheOldestMonthFirst() {
        val ledger = LedgerCalculator.ledgerFor(
            tenant = tenant(start = "2026-10-01"),
            unit = unit,
            building = building,
            payments = listOf(payment(1, 7000.0, "2026-11-05", "2026-11")),
            asOfMonth = "2026-11",
            asOfDate = "2026-11-24"
        )

        assertEquals(14000.0, ledger.totalCharged, 0.01)
        assertEquals(7000.0, ledger.totalPaid, 0.01)
        assertEquals(7000.0, ledger.balance, 0.01)
        assertEquals("2026-11", ledger.oldestUnpaidMonth)
    }

    @Test
    fun overPaymentBecomesCredit() {
        val ledger = LedgerCalculator.ledgerFor(
            tenant = tenant(),
            unit = unit,
            building = building,
            payments = listOf(payment(1, 10000.0, "2026-11-05", "2026-11")),
            asOfMonth = "2026-11",
            asOfDate = "2026-11-24"
        )

        assertEquals(-3000.0, ledger.balance, 0.01)
        assertEquals(3000.0, ledger.credit, 0.01)
        assertTrue(ledger.isCredit)
    }

    @Test
    fun firstMonthIsProratedForAMidMonthMoveIn() {
        val ledger = LedgerCalculator.ledgerFor(
            tenant = tenant(start = "2026-11-16"),
            unit = unit,
            building = building,
            payments = emptyList(),
            asOfMonth = "2026-11",
            asOfDate = "2026-11-24"
        )

        assertEquals(3500.0, ledger.totalCharged, 0.01)
        assertEquals(3500.0, ledger.balance, 0.01)
    }

    @Test
    fun oldArrearsAreInThe90PlusBucket() {
        val ledger = LedgerCalculator.ledgerFor(
            tenant = tenant(start = "2026-08-01"),
            unit = unit,
            building = building,
            payments = emptyList(),
            asOfMonth = "2026-11",
            asOfDate = "2026-11-24"
        )

        assertEquals(28000.0, ledger.balance, 0.01)
        assertEquals("2026-08", ledger.oldestUnpaidMonth)
        assertEquals(LedgerCalculator.AGING_90_PLUS, ledger.agingBucket)
        assertNotNull(ledger.entries.firstOrNull())
    }

    @Test
    fun agingBucketFollowsTheDaysLate() {
        assertEquals(LedgerCalculator.AGING_PAID, LedgerCalculator.agingBucket(0.0, 40))
        assertEquals(LedgerCalculator.AGING_CURRENT, LedgerCalculator.agingBucket(100.0, 0))
        assertEquals(LedgerCalculator.AGING_1_30, LedgerCalculator.agingBucket(100.0, 12))
        assertEquals(LedgerCalculator.AGING_31_60, LedgerCalculator.agingBucket(100.0, 45))
        assertEquals(LedgerCalculator.AGING_61_90, LedgerCalculator.agingBucket(100.0, 75))
        assertEquals(LedgerCalculator.AGING_90_PLUS, LedgerCalculator.agingBucket(100.0, 100))
    }
}
