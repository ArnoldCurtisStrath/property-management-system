package com.example.propertymanagementsystem.ui.buildings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.propertymanagementsystem.SahihiApp
import com.example.propertymanagementsystem.data.ledger.BuildingSummary
import com.example.propertymanagementsystem.data.ledger.TenantLedger
import com.example.propertymanagementsystem.data.local.AppDatabase
import com.example.propertymanagementsystem.data.model.Building
import com.example.propertymanagementsystem.data.model.Expense
import com.example.propertymanagementsystem.data.model.MaintenanceRequest
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.data.model.Tenant
import com.example.propertymanagementsystem.util.Dates

class BuildingDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as SahihiApp).container

    fun building(id: Long): LiveData<Building?> = container.buildings.getById(id)

    fun units(id: Long): LiveData<List<Property>> = container.units.getForBuilding(id)

    fun summary(id: Long): LiveData<BuildingSummary?> =
        container.ledger.buildingSummaries.map { list -> list.find { it.building.id == id } }

    val ledgers: LiveData<List<TenantLedger>> = container.ledger.ledgers

    val tenants: LiveData<List<Tenant>> = container.ledger.tenants

    val maintenance: LiveData<List<MaintenanceRequest>> = container.ledger.maintenance

    fun expenses(id: Long): LiveData<List<Expense>> =
        container.expenses.getForBuildingMonth(id, Dates.currentMonth())

    val month: String = Dates.currentMonth()
}
