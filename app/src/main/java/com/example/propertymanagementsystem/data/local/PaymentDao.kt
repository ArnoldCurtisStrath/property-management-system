package com.example.propertymanagementsystem.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.propertymanagementsystem.data.model.Payment

/**
 * The queries used on the payments table.
 *
 * The sums are done by the database itself instead of in Kotlin. That is
 * quicker and it keeps the totals correct even when there are many payments.
 */
@Dao
interface PaymentDao {

    @Query("SELECT * FROM payments ORDER BY date DESC, id DESC")
    fun getAll(): LiveData<List<Payment>>

    @Query("SELECT * FROM payments WHERE tenantId = :tenantId ORDER BY date DESC, id DESC")
    fun getForTenant(tenantId: Long): LiveData<List<Payment>>

    @Query("SELECT * FROM payments WHERE month = :month ORDER BY date DESC")
    fun getForMonth(month: String): LiveData<List<Payment>>

    // The total rent collected in one month. COALESCE is used so the sum is
    // 0 and not null when there are no payments at all.
    @Query(
        "SELECT COALESCE(SUM(amount), 0) FROM payments " +
            "WHERE month = :month AND status = 'Paid'"
    )
    fun totalCollectedInMonth(month: String): LiveData<Double>

    // What one tenant has paid for one month.
    @Query(
        "SELECT COALESCE(SUM(amount), 0) FROM payments " +
            "WHERE tenantId = :tenantId AND month = :month AND status = 'Paid'"
    )
    fun totalPaidByTenant(tenantId: Long, month: String): LiveData<Double>

    // How many payments a tenant has for one month. Used to work out if the
    // rent for that month has been paid.
    @Query(
        "SELECT COUNT(*) FROM payments " +
            "WHERE tenantId = :tenantId AND month = :month AND status = 'Paid'"
    )
    fun countPaidByTenant(tenantId: Long, month: String): LiveData<Int>

    @Insert
    suspend fun insert(payment: Payment): Long

    @Update
    suspend fun update(payment: Payment)

    @Delete
    suspend fun delete(payment: Payment)
}
