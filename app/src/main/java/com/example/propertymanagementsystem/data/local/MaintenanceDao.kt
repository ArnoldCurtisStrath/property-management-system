package com.example.propertymanagementsystem.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.propertymanagementsystem.data.model.MaintenanceRequest

/**
 * The queries used on the maintenance table.
 */
@Dao
interface MaintenanceDao {

    @Query("SELECT * FROM maintenance ORDER BY reportedAt DESC, id DESC")
    fun getAll(): LiveData<List<MaintenanceRequest>>

    @Query("SELECT * FROM maintenance WHERE propertyId = :propertyId ORDER BY reportedAt DESC")
    fun getForProperty(propertyId: Long): LiveData<List<MaintenanceRequest>>

    @Query("SELECT * FROM maintenance WHERE id = :id")
    fun getById(id: Long): LiveData<MaintenanceRequest?>

    // The number of requests that still need attention. This is the number
    // shown on the dashboard.
    @Query("SELECT COUNT(*) FROM maintenance WHERE status != 'Done'")
    fun countOpen(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM maintenance WHERE status = :status")
    fun countByStatus(status: String): LiveData<Int>

    @Insert
    suspend fun insert(request: MaintenanceRequest): Long

    @Update
    suspend fun update(request: MaintenanceRequest)

    @Delete
    suspend fun delete(request: MaintenanceRequest)
}
