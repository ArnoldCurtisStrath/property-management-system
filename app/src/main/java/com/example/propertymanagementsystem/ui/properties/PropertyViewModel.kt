package com.example.propertymanagementsystem.ui.properties

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.propertymanagementsystem.SahihiApp
import com.example.propertymanagementsystem.data.model.Building
import com.example.propertymanagementsystem.data.model.Property
import kotlinx.coroutines.launch

/**
 * Holds the unit records and the buildings they belong to.
 */
class PropertyViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as SahihiApp).container
    private val repository = container.units
    private val buildingRepository = container.buildings

    val properties: LiveData<List<Property>> = repository.getAll()

    val buildings: LiveData<List<Building>> = buildingRepository.getAll()

    fun forBuilding(buildingId: Long): LiveData<List<Property>> =
        repository.getForBuilding(buildingId)

    fun property(id: Long): LiveData<Property?> = repository.getById(id)

    fun add(property: Property) {
        viewModelScope.launch { repository.add(property) }
    }

    fun update(property: Property) {
        viewModelScope.launch { repository.update(property) }
    }

    fun delete(property: Property) {
        viewModelScope.launch { repository.delete(property) }
    }

    suspend fun isDuplicate(name: String, buildingId: Long, id: Long): Boolean =
        repository.nameAlreadyUsed(name, buildingId, id)
}
