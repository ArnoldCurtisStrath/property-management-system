package com.example.propertymanagementsystem.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.propertymanagementsystem.data.model.Tenant

/**
 * The queries used on the tenants table.
 */
@Dao
interface TenantDao {

    @Query("SELECT * FROM tenants ORDER BY name")
    fun getAll(): LiveData<List<Tenant>>

    @Query("SELECT * FROM tenants WHERE active = 1 ORDER BY name")
    fun getActive(): LiveData<List<Tenant>>

    @Query("SELECT * FROM tenants WHERE id = :id")
    fun getById(id: Long): LiveData<Tenant?>

    @Query("SELECT * FROM tenants WHERE propertyId = :propertyId ORDER BY name")
    fun getForProperty(propertyId: Long): LiveData<List<Tenant>>

    @Query("SELECT COUNT(*) FROM tenants WHERE propertyId = :propertyId AND active = 1")
    fun countForProperty(propertyId: Long): LiveData<Int>

    // Stops a phone number being used by two tenants at the same time.
    @Query("SELECT COUNT(*) FROM tenants WHERE phone = :phone AND id != :id")
    suspend fun existsWithSamePhone(phone: String, id: Long): Int

    @Insert
    suspend fun insert(tenant: Tenant): Long

    @Update
    suspend fun update(tenant: Tenant)

    @Delete
    suspend fun delete(tenant: Tenant)
}
