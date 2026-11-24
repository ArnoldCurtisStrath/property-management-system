package com.example.propertymanagementsystem.ui.buildings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.propertymanagementsystem.SahihiApp
import com.example.propertymanagementsystem.data.ledger.BuildingSummary
import com.example.propertymanagementsystem.data.ledger.TenantLedger
import com.example.propertymanagementsystem.data.ledger.TodayState
import com.example.propertymanagementsystem.data.model.Building
import kotlinx.coroutines.launch

class BuildingViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as SahihiApp).container

    val buildings: LiveData<List<Building>> = container.buildings.getAll()

    val summaries: LiveData<List<BuildingSummary>> = container.ledger.buildingSummaries

    val today: LiveData<TodayState> = container.ledger.today

    val ledgers: LiveData<List<TenantLedger>> = container.ledger.ledgers

    fun building(id: Long): LiveData<Building?> = container.buildings.getById(id)

    fun add(building: Building) {
        viewModelScope.launch { container.buildings.add(building) }
    }

    fun update(building: Building) {
        viewModelScope.launch { container.buildings.update(building) }
    }

    fun delete(building: Building) {
        viewModelScope.launch { container.buildings.delete(building) }
    }
}
