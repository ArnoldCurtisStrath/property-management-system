package com.example.propertymanagementsystem.data.repository

import androidx.lifecycle.LiveData
import com.example.propertymanagementsystem.data.local.BuildingDao
import com.example.propertymanagementsystem.data.model.Building

class BuildingRepository(private val dao: BuildingDao) {

    fun getAll(): LiveData<List<Building>> = dao.getAll()

    fun getById(id: Long): LiveData<Building?> = dao.getById(id)

    fun count(): LiveData<Int> = dao.count()

    suspend fun getAllOnce(): List<Building> = dao.getAllOnce()

    suspend fun getOnce(id: Long): Building? = dao.getOnce(id)

    suspend fun add(building: Building): Long = dao.insert(building)

    suspend fun update(building: Building) = dao.update(building)

    suspend fun delete(building: Building) = dao.delete(building)
}
