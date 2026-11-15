package com.example.propertymanagementsystem.ui.tenants

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.propertymanagementsystem.data.local.AppDatabase
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.data.model.Tenant
import com.example.propertymanagementsystem.data.repository.PropertyRepository
import com.example.propertymanagementsystem.data.repository.TenantRepository
import kotlinx.coroutines.launch

/**
 * Holds the tenant records for the screens that show them.
 */
class TenantViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TenantRepository(
        AppDatabase.getInstance(application).tenantDao()
    )

    // The properties are needed by the list so it can show where each tenant
    // lives.
    private val propertyRepository = PropertyRepository(
        AppDatabase.getInstance(application).propertyDao()
    )
    // All the tenants, in alphabetical order.
    val tenants: LiveData<List<Tenant>> = repository.getAll()

    // All the properties, used to show the name of the property of a tenant.
    val properties: LiveData<List<Property>> = propertyRepository.getAll()

    // One tenant, used on the details screen.
    fun tenant(id: Long): LiveData<Tenant?> = repository.getById(id)

    fun add(tenant: Tenant) {
        viewModelScope.launch {
            repository.add(tenant)
        }
    }

    fun update(tenant: Tenant) {
        viewModelScope.launch {
            repository.update(tenant)
        }
    }

    fun delete(tenant: Tenant) {
        viewModelScope.launch {
            repository.delete(tenant)
        }
    }

    // Stops the same phone number being used by two tenants.
    suspend fun isDuplicatePhone(phone: String, id: Long): Boolean =
        repository.phoneAlreadyUsed(phone, id)
}
