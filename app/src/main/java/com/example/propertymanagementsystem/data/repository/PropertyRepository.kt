package com.example.propertymanagementsystem.data.repository

import androidx.lifecycle.LiveData
import com.example.propertymanagementsystem.data.local.PropertyDao
import com.example.propertymanagementsystem.data.model.Property

/**
 * Sits between the ViewModel and the units table.
 */
class PropertyRepository(private val dao: PropertyDao) {

    fun getAll(): LiveData<List<Property>> = dao.getAll()

    fun getForBuilding(buildingId: Long): LiveData<List<Property>> =
        dao.getForBuilding(buildingId)

    suspend fun getForBuildingOnce(buildingId: Long): List<Property> =
        dao.getForBuildingOnce(buildingId)

    suspend fun allOnce(): List<Property> = dao.getAllOnce()

    fun getById(id: Long): LiveData<Property?> = dao.getById(id)

    fun countByStatus(status: String): LiveData<Int> = dao.countByStatus(status)

    fun countForBuilding(buildingId: Long): LiveData<Int> = dao.countForBuilding(buildingId)

    fun countForBuildingByStatus(buildingId: Long, status: String): LiveData<Int> =
        dao.countForBuildingByStatus(buildingId, status)

    suspend fun add(property: Property): Long = dao.insert(property)

    suspend fun update(property: Property) = dao.update(property)

    suspend fun delete(property: Property) = dao.delete(property)

    suspend fun nameAlreadyUsed(name: String, buildingId: Long, id: Long): Boolean =
        dao.existsWithSameName(name, buildingId, id) > 0
}
