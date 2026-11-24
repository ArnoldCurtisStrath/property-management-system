package com.example.propertymanagementsystem.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.propertymanagementsystem.data.model.Building

@Dao
interface BuildingDao {

    @Query("SELECT * FROM buildings ORDER BY name")
    fun getAll(): LiveData<List<Building>>

    @Query("SELECT * FROM buildings ORDER BY name")
    suspend fun getAllOnce(): List<Building>

    @Query("SELECT * FROM buildings WHERE id = :id")
    fun getById(id: Long): LiveData<Building?>

    @Query("SELECT * FROM buildings WHERE id = :id")
    suspend fun getOnce(id: Long): Building?

    @Query("SELECT COUNT(*) FROM buildings")
    fun count(): LiveData<Int>

    @Insert
    suspend fun insert(building: Building): Long

    @Update
    suspend fun update(building: Building)

    @Delete
    suspend fun delete(building: Building)
}
