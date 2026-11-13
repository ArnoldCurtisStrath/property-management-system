package com.example.propertymanagementsystem.data.repository

import androidx.lifecycle.LiveData
import com.example.propertymanagementsystem.data.local.MaintenanceDao
import com.example.propertymanagementsystem.data.model.MaintenanceRequest

/**
 * Sits between the ViewModel and the maintenance table.
 */
class MaintenanceRepository(private val dao: MaintenanceDao) {

    fun getAll(): LiveData<List<MaintenanceRequest>> = dao.getAll()

    fun getForProperty(propertyId: Long): LiveData<List<MaintenanceRequest>> =
        dao.getForProperty(propertyId)

    fun getById(id: Long): LiveData<MaintenanceRequest?> = dao.getById(id)

    // The number of requests that are still open, shown on the dashboard.
    fun countOpen(): LiveData<Int> = dao.countOpen()

    fun countByStatus(status: String): LiveData<Int> = dao.countByStatus(status)

    suspend fun add(request: MaintenanceRequest): Long = dao.insert(request)

    suspend fun update(request: MaintenanceRequest) = dao.update(request)

    suspend fun delete(request: MaintenanceRequest) = dao.delete(request)
}
