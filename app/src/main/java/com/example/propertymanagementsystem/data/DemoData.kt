package com.example.propertymanagementsystem.data

import com.example.propertymanagementsystem.AppContainer
import com.example.propertymanagementsystem.data.model.Building
import com.example.propertymanagementsystem.data.model.Expense
import com.example.propertymanagementsystem.data.model.MaintenanceRequest
import com.example.propertymanagementsystem.data.model.Payment
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.data.model.Tenant
import com.example.propertymanagementsystem.util.Dates

/**
 * A realistic sample portfolio for the presentation and for a new agent who
 * wants to see the app before entering their own buildings.
 *
 * It only seeds once: if the Kasarani building is already there, the method
 * returns false and leaves the portfolio alone.
 */
object DemoData {

    suspend fun seed(container: AppContainer): Boolean {
        val existing = container.buildings.getAllOnce()
        if (existing.any { it.name == "Kasarani Bedsitters" }) return false

        container.settings.businessName = "MbiCi Property Care"
        container.settings.agentName = "Arnold"
        container.settings.phone = "0712 345 678"
        container.settings.managementFee = 10.0
        container.settings.defaultCollector = "Me"
        container.settings.receiptPrefix = "SH"

        val current = Dates.currentMonth()
        val previous = Dates.addMonths(current, -1)

        val kasarani = container.buildings.add(
            Building(
                name = "Kasarani Bedsitters",
                address = "Kasarani, Nairobi",
                type = Building.TYPE_BEDSITTER_BLOCK,
                ownerName = "Mr. Mwangi",
                ownerPhone = "0722 000 111",
                ownerEmail = "mwangi@example.co.ke",
                managementFee = 10.0,
                notes = "Six bedsitters behind the mosque."
            )
        )
        val kilimani = container.buildings.add(
            Building(
                name = "Kilimani Apartments",
                address = "Kilimani, Nairobi",
                type = Building.TYPE_APARTMENT_BLOCK,
                ownerName = "Mrs. Wanjiru",
                ownerPhone = "0722 000 222",
                ownerEmail = "wanjiru@example.co.ke",
                managementFee = 12.0,
                notes = "Three two-bedroom units."
            )
        )
        val ruaka = container.buildings.add(
            Building(
                name = "Ruaka Shops",
                address = "Ruaka, Kiambu",
                type = Building.TYPE_COMMERCIAL,
                ownerName = "Mr. Otieno",
                ownerPhone = "0722 000 333",
                ownerEmail = "otieno@example.co.ke",
                managementFee = 8.0,
                notes = "Two shops on the main road."
            )
        )

        val a1 = unit(container, kasarani, "A1", Property.TYPE_BEDSITTER, 7_000.0)
        val a2 = unit(container, kasarani, "A2", Property.TYPE_BEDSITTER, 7_000.0)
        val a3 = unit(container, kasarani, "A3", Property.TYPE_BEDSITTER, 7_000.0)
        val a4 = unit(container, kasarani, "A4", Property.TYPE_BEDSITTER, 7_000.0)
        unit(
            container, kasarani, "A5", Property.TYPE_BEDSITTER, 7_000.0,
            status = Property.STATUS_VACANT
        )

        val b1 = unit(container, kilimani, "B1", Property.TYPE_TWO_BEDROOM, 15_000.0)
        val b2 = unit(container, kilimani, "B2", Property.TYPE_TWO_BEDROOM, 15_000.0)
        val b3 = unit(container, kilimani, "B3", Property.TYPE_TWO_BEDROOM, 15_000.0)

        val s1 = unit(container, ruaka, "Shop 1", Property.TYPE_SHOP, 20_000.0)
        val s2 = unit(container, ruaka, "Shop 2", Property.TYPE_SHOP, 20_000.0)

        val grace = tenant(
            container, a1, "Grace Wanjiku", "0712345678", 7_000.0,
            "$current-01", dueDay = 5, deposit = 7_000.0
        )
        val peter = tenant(
            container, a2, "Peter Otieno", "0723456789", 7_000.0,
            "$current-01", dueDay = 5, deposit = 7_000.0
        )
        val mary = tenant(
            container, a3, "Mary Achieng", "0734567890", 7_000.0,
            "$previous-01", dueDay = 5, deposit = 7_000.0
        )
        val john = tenant(
            container, a4, "John Kamau", "0745678901", 7_000.0,
            "$current-01", dueDay = 5, deposit = 7_000.0
        )
        val alice = tenant(
            container, b1, "Alice Wambui", "0756789012", 15_000.0,
            "$current-01", dueDay = 1, deposit = 15_000.0
        )
        val brian = tenant(
            container, b2, "Brian Ochieng", "0767890123", 15_000.0,
            "$previous-01", dueDay = 1, deposit = 15_000.0
        )
        val faith = tenant(
            container, b3, "Faith Njeri", "0778901234", 15_000.0,
            "$current-01", dueDay = 1, deposit = 15_000.0
        )
        val njeri = tenant(
            container, s1, "Njeri Kiosk", "0789012345", 20_000.0,
            "$current-01", dueDay = 3, deposit = 20_000.0
        )
        val hardware = tenant(
            container, s2, "Brian Hardware", "0790123456", 20_000.0,
            "$current-01", dueDay = 3, deposit = 20_000.0
        )

        pay(container, grace, 3_000.0, day(current, 5), Payment.METHOD_CASH, null, "Otieno")
        pay(container, peter, 7_000.0, day(current, 3), Payment.METHOD_CASH, null, "Otieno")
        pay(container, john, 3_000.0, day(current, 8), Payment.METHOD_MPESA, "QGH7X8Y9Z", "Me")
        pay(container, alice, 15_000.0, day(current, 2), Payment.METHOD_MPESA, "RKT4M2N8P", "Me")
        pay(container, brian, 10_000.0, day(previous, 10), Payment.METHOD_CASH, null, "Otieno")
        pay(container, faith, 15_000.0, day(current, 4), Payment.METHOD_BANK, "BANK-8821", "Me")
        pay(container, njeri, 20_000.0, day(current, 6), Payment.METHOD_CASH, null, "Otieno")
        pay(container, hardware, 10_000.0, day(current, 9), Payment.METHOD_MPESA, "TXL9K4M2Q", "Me")

        val water = container.ledger.addExpense(
            Expense(
                buildingId = kasarani,
                category = Expense.CATEGORY_WATER,
                amount = 2_500.0,
                date = day(current, 3),
                vendor = "Nairobi Water",
                method = Payment.METHOD_CASH,
                recordedBy = "Otieno"
            )
        )
        check(water > 0)
        container.ledger.addExpense(
            Expense(
                buildingId = kasarani,
                category = Expense.CATEGORY_SECURITY,
                amount = 1_500.0,
                date = day(current, 5),
                vendor = "Guardian Security",
                method = Payment.METHOD_CASH,
                recordedBy = "Otieno"
            )
        )
        container.ledger.addExpense(
            Expense(
                buildingId = kilimani,
                category = Expense.CATEGORY_REPAIRS,
                amount = 4_000.0,
                date = day(current, 10),
                vendor = "Fundi John",
                method = Payment.METHOD_CASH,
                recordedBy = "Me"
            )
        )
        container.ledger.addExpense(
            Expense(
                buildingId = kilimani,
                category = Expense.CATEGORY_CLEANING,
                amount = 1_500.0,
                date = day(current, 8),
                vendor = "Clean Team",
                method = Payment.METHOD_CASH,
                recordedBy = "Me"
            )
        )
        container.ledger.addExpense(
            Expense(
                buildingId = ruaka,
                category = Expense.CATEGORY_GARBAGE,
                amount = 1_000.0,
                date = day(current, 4),
                vendor = "Ruaka Garbage",
                method = Payment.METHOD_CASH,
                recordedBy = "Otieno"
            )
        )

        container.ledger.addMaintenance(
            MaintenanceRequest(
                propertyId = a1,
                title = "Kitchen tap leaking",
                description = "The cold water tap in the kitchen has been dripping for a week.",
                category = MaintenanceRequest.CATEGORY_PLUMBING,
                priority = MaintenanceRequest.PRIORITY_NORMAL,
                status = MaintenanceRequest.STATUS_OPEN,
                reportedAt = day(current, 18)
            )
        )
        container.ledger.addMaintenance(
            MaintenanceRequest(
                propertyId = a3,
                title = "Roof leaking in the bedroom",
                description = "Water comes through the ceiling when it rains heavily.",
                category = MaintenanceRequest.CATEGORY_STRUCTURAL,
                priority = MaintenanceRequest.PRIORITY_URGENT,
                status = MaintenanceRequest.STATUS_OPEN,
                reportedAt = day(current, 20)
            )
        )
        container.ledger.addMaintenance(
            MaintenanceRequest(
                propertyId = b2,
                title = "Broken window",
                description = "The bedroom window does not close properly.",
                category = MaintenanceRequest.CATEGORY_STRUCTURAL,
                priority = MaintenanceRequest.PRIORITY_HIGH,
                status = MaintenanceRequest.STATUS_IN_PROGRESS,
                reportedAt = day(current, 16)
            )
        )
        container.ledger.addMaintenance(
            MaintenanceRequest(
                propertyId = s1,
                title = "Socket replacement",
                description = "The socket near the counter was sparking.",
                category = MaintenanceRequest.CATEGORY_ELECTRICAL,
                priority = MaintenanceRequest.PRIORITY_NORMAL,
                status = MaintenanceRequest.STATUS_DONE,
                reportedAt = day(current, 12),
                resolvedAt = day(current, 14),
                cost = 1_200.0,
                vendor = "Fundi Peter"
            )
        )
        container.ledger.addExpense(
            Expense(
                buildingId = ruaka,
                category = Expense.CATEGORY_REPAIRS,
                amount = 1_200.0,
                date = day(current, 14),
                vendor = "Fundi Peter",
                method = Payment.METHOD_CASH,
                note = "Socket replacement at Shop 1",
                recordedBy = "Me"
            )
        )

        return true
    }

    private suspend fun unit(
        container: AppContainer,
        buildingId: Long,
        name: String,
        type: String,
        rent: Double,
        status: String = Property.STATUS_OCCUPIED
    ): Long {
        val building = container.buildings.getOnce(buildingId)
        return container.units.add(
            Property(
                buildingId = buildingId,
                name = name,
                address = building?.address.orEmpty(),
                type = type,
                rent = rent,
                status = status
            )
        )
    }

    private suspend fun tenant(
        container: AppContainer,
        unitId: Long,
        name: String,
        phone: String,
        rent: Double,
        startDate: String,
        dueDay: Int,
        deposit: Double
    ): Long {
        return container.tenants.add(
            Tenant(
                name = name,
                phone = phone,
                email = "",
                propertyId = unitId,
                rent = rent,
                startDate = startDate,
                dueDay = dueDay,
                deposit = deposit,
                active = true
            )
        )
    }

    private suspend fun pay(
        container: AppContainer,
        tenantId: Long,
        amount: Double,
        date: String,
        method: String,
        reference: String?,
        collector: String
    ) {
        container.ledger.recordPayment(
            tenantId = tenantId,
            amount = amount,
            date = date,
            method = method,
            reference = reference,
            note = if (method == Payment.METHOD_MPESA) "M-Pesa confirmation" else null,
            recordedBy = collector
        )
    }

    private fun day(month: String, day: Int): String {
        val max = Dates.daysInMonth(month)
        val safe = day.coerceIn(1, max)
        return "$month-" + safe.toString().padStart(2, '0')
    }
}
