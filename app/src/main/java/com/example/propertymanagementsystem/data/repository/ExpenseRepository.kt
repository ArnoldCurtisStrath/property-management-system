package com.example.propertymanagementsystem.data.repository

import androidx.lifecycle.LiveData
import com.example.propertymanagementsystem.data.local.ExpenseDao
import com.example.propertymanagementsystem.data.model.Expense

class ExpenseRepository(private val dao: ExpenseDao) {

    fun getAll(): LiveData<List<Expense>> = dao.getAll()

    fun getForMonth(month: String): LiveData<List<Expense>> = dao.getForMonth(month)

    fun getForBuilding(buildingId: Long): LiveData<List<Expense>> =
        dao.getForBuilding(buildingId)

    fun getForBuildingMonth(buildingId: Long, month: String): LiveData<List<Expense>> =
        dao.getForBuildingMonth(buildingId, month)

    suspend fun getAllOnce(): List<Expense> = dao.getAllOnce()

    suspend fun getForMonthOnce(month: String): List<Expense> = dao.getForMonthOnce(month)

    suspend fun add(expense: Expense): Long = dao.insert(expense)

    suspend fun update(expense: Expense) = dao.update(expense)

    suspend fun delete(expense: Expense) = dao.delete(expense)
}
