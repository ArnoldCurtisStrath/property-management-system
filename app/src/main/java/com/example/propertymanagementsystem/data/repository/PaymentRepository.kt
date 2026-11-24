package com.example.propertymanagementsystem.data.repository

import androidx.lifecycle.LiveData
import com.example.propertymanagementsystem.data.local.PaymentDao
import com.example.propertymanagementsystem.data.model.Payment

class PaymentRepository(private val dao: PaymentDao) {

    fun getAll(): LiveData<List<Payment>> = dao.getAll()

    fun getForTenant(tenantId: Long): LiveData<List<Payment>> = dao.getForTenant(tenantId)

    fun getForMonth(month: String): LiveData<List<Payment>> = dao.getForMonth(month)

    fun getForBuilding(buildingId: Long): LiveData<List<Payment>> =
        dao.getForBuilding(buildingId)

    fun getForDate(date: String): LiveData<List<Payment>> = dao.getForDate(date)

    suspend fun getAllOnce(): List<Payment> = dao.getAllOnce()

    suspend fun getForTenantOnce(tenantId: Long): List<Payment> = dao.getForTenantOnce(tenantId)

    suspend fun getForMonthOnce(month: String): List<Payment> = dao.getForMonthOnce(month)

    suspend fun getForDateOnce(date: String): List<Payment> = dao.getForDateOnce(date)

    fun totalCollectedInMonth(month: String): LiveData<Double> =
        dao.totalCollectedInMonth(month)

    fun totalCollectedInMonthForBuilding(buildingId: Long, month: String): LiveData<Double> =
        dao.totalCollectedInMonthForBuilding(buildingId, month)

    fun totalPaidByTenant(tenantId: Long, month: String): LiveData<Double> =
        dao.totalPaidByTenant(tenantId, month)

    fun countPaidByTenant(tenantId: Long, month: String): LiveData<Int> =
        dao.countPaidByTenant(tenantId, month)

    suspend fun add(payment: Payment): Long = dao.insert(payment)

    suspend fun update(payment: Payment) = dao.update(payment)

    suspend fun delete(payment: Payment) = dao.delete(payment)
}
