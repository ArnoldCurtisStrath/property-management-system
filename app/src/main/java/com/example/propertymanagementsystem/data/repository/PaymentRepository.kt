package com.example.propertymanagementsystem.data.repository

import androidx.lifecycle.LiveData
import com.example.propertymanagementsystem.data.local.PaymentDao
import com.example.propertymanagementsystem.data.model.Payment

/**
 * Sits between the ViewModel and the payments table.
 */
class PaymentRepository(private val dao: PaymentDao) {

    fun getAll(): LiveData<List<Payment>> = dao.getAll()

    fun getForTenant(tenantId: Long): LiveData<List<Payment>> = dao.getForTenant(tenantId)

    fun getForMonth(month: String): LiveData<List<Payment>> = dao.getForMonth(month)

    // How much rent was collected in a month, used on the dashboard.
    fun totalCollectedInMonth(month: String): LiveData<Double> =
        dao.totalCollectedInMonth(month)

    // What one tenant has paid for one month, used on the tenant screen.
    fun totalPaidByTenant(tenantId: Long, month: String): LiveData<Double> =
        dao.totalPaidByTenant(tenantId, month)

    fun countPaidByTenant(tenantId: Long, month: String): LiveData<Int> =
        dao.countPaidByTenant(tenantId, month)

    suspend fun add(payment: Payment): Long = dao.insert(payment)

    suspend fun update(payment: Payment) = dao.update(payment)

    suspend fun delete(payment: Payment) = dao.delete(payment)
}
