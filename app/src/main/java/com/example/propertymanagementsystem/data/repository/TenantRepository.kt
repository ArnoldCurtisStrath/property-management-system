package com.example.propertymanagementsystem.data.repository

import androidx.lifecycle.LiveData
import com.example.propertymanagementsystem.data.local.TenantDao
import com.example.propertymanagementsystem.data.model.Tenant

/**
 * Sits between the ViewModel and the tenants table.
 */
class TenantRepository(private val dao: TenantDao) {

    fun getAll(): LiveData<List<Tenant>> = dao.getAll()

    fun getActive(): LiveData<List<Tenant>> = dao.getActive()

    fun getById(id: Long): LiveData<Tenant?> = dao.getById(id)

    fun getForProperty(propertyId: Long): LiveData<List<Tenant>> =
        dao.getForProperty(propertyId)

    fun countForProperty(propertyId: Long): LiveData<Int> = dao.countForProperty(propertyId)

    suspend fun add(tenant: Tenant): Long = dao.insert(tenant)

    suspend fun update(tenant: Tenant) = dao.update(tenant)

    suspend fun delete(tenant: Tenant) = dao.delete(tenant)

    suspend fun phoneAlreadyUsed(phone: String, id: Long): Boolean =
        dao.existsWithSamePhone(phone, id) > 0
}
