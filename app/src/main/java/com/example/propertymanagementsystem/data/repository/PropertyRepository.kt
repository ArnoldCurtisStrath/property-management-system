package com.example.propertymanagementsystem.data.repository

import androidx.lifecycle.LiveData
import com.example.propertymanagementsystem.data.local.PropertyDao
import com.example.propertymanagementsystem.data.model.Property

/**
 * Sits between the ViewModel and the properties table.
 *
 * The ViewModel never talks to the DAO directly. It goes through this class,
 * so the place where the data comes from is in one spot and is easy to
 * explain. If the data was later moved to a server, only this file would
 * have to change.
 */
class PropertyRepository(private val dao: PropertyDao) {

    fun getAll(): LiveData<List<Property>> = dao.getAll()

    fun getById(id: Long): LiveData<Property?> = dao.getById(id)

    fun countByStatus(status: String): LiveData<Int> = dao.countByStatus(status)

    suspend fun add(property: Property): Long = dao.insert(property)

    suspend fun update(property: Property) = dao.update(property)

    suspend fun delete(property: Property) = dao.delete(property)

    // Returns true when another property already has this name and address.
    suspend fun nameAlreadyUsed(name: String, address: String, id: Long): Boolean =
        dao.existsWithSameName(name, address, id) > 0
}
