package com.example.propertymanagementsystem.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.propertymanagementsystem.data.model.Property

@Dao
interface PropertyDao {

    @Query("SELECT * FROM properties ORDER BY name")
    fun getAll(): LiveData<List<Property>>

    @Query("SELECT * FROM properties ORDER BY name")
    suspend fun getAllOnce(): List<Property>

    @Query("SELECT * FROM properties WHERE buildingId = :buildingId ORDER BY name")
    fun getForBuilding(buildingId: Long): LiveData<List<Property>>

    @Query("SELECT * FROM properties WHERE buildingId = :buildingId ORDER BY name")
    suspend fun getForBuildingOnce(buildingId: Long): List<Property>

    @Query("SELECT * FROM properties WHERE id = :id")
    fun getById(id: Long): LiveData<Property?>

    @Query("SELECT * FROM properties WHERE id = :id")
    suspend fun getOnce(id: Long): Property?

    @Query("SELECT COUNT(*) FROM properties WHERE status = :status")
    fun countByStatus(status: String): LiveData<Int>

    @Query("SELECT COUNT(*) FROM properties WHERE buildingId = :buildingId")
    fun countForBuilding(buildingId: Long): LiveData<Int>

    @Query(
        "SELECT COUNT(*) FROM properties " +
            "WHERE buildingId = :buildingId AND status = :status"
    )
    fun countForBuildingByStatus(buildingId: Long, status: String): LiveData<Int>

    @Query(
        "SELECT COUNT(*) FROM properties " +
            "WHERE LOWER(name) = LOWER(:name) AND buildingId = :buildingId AND id != :id"
    )
    suspend fun existsWithSameName(name: String, buildingId: Long, id: Long): Int

    @Insert
    suspend fun insert(property: Property): Long

    @Update
    suspend fun update(property: Property)

    @Delete
    suspend fun delete(property: Property)
}
