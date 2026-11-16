package com.example.propertymanagementsystem.ui.maintenance

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.propertymanagementsystem.data.local.AppDatabase
import com.example.propertymanagementsystem.data.model.MaintenanceRequest
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.data.repository.MaintenanceRepository
import com.example.propertymanagementsystem.data.repository.PropertyRepository
import kotlinx.coroutines.launch

/**
 * Holds the maintenance requests and the properties they belong to.
 */
class MaintenanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MaintenanceRepository(
        AppDatabase.getInstance(application).maintenanceDao()
    )

    private val propertyRepository = PropertyRepository(
        AppDatabase.getInstance(application).propertyDao()
    )

    // All the requests, the newest one first.
    val requests: LiveData<List<MaintenanceRequest>> = repository.getAll()

    // The properties a request can be raised against.
    val properties: LiveData<List<Property>> = propertyRepository.getAll()

    // One request, used when a request is opened.
    fun request(id: Long): LiveData<MaintenanceRequest?> = repository.getById(id)

    // The number of requests that are still open.
    val openCount: LiveData<Int> = repository.countOpen()

    fun add(request: MaintenanceRequest) {
        viewModelScope.launch {
            repository.add(request)
        }
    }

    fun update(request: MaintenanceRequest) {
        viewModelScope.launch {
            repository.update(request)
        }
    }

    fun delete(request: MaintenanceRequest) {
        viewModelScope.launch {
            repository.delete(request)
        }
    }
}
