package com.example.propertymanagementsystem.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.propertymanagementsystem.data.model.Payment

@Dao
interface PaymentDao {

    @Query("SELECT * FROM payments ORDER BY date DESC, id DESC")
    fun getAll(): LiveData<List<Payment>>

    @Query("SELECT * FROM payments ORDER BY date DESC, id DESC")
    suspend fun getAllOnce(): List<Payment>

    @Query("SELECT * FROM payments WHERE id = :id")
    suspend fun getOnce(id: Long): Payment?

    @Query("SELECT * FROM payments WHERE tenantId = :tenantId ORDER BY date DESC, id DESC")
    fun getForTenant(tenantId: Long): LiveData<List<Payment>>

    @Query("SELECT * FROM payments WHERE tenantId = :tenantId ORDER BY date DESC, id DESC")
    suspend fun getForTenantOnce(tenantId: Long): List<Payment>

    @Query("SELECT * FROM payments WHERE month = :month ORDER BY date DESC")
    fun getForMonth(month: String): LiveData<List<Payment>>

    @Query("SELECT * FROM payments WHERE month = :month ORDER BY date DESC")
    suspend fun getForMonthOnce(month: String): List<Payment>

    @Query("SELECT * FROM payments WHERE buildingId = :buildingId ORDER BY date DESC")
    fun getForBuilding(buildingId: Long): LiveData<List<Payment>>

    @Query("SELECT * FROM payments WHERE date = :date ORDER BY id DESC")
    fun getForDate(date: String): LiveData<List<Payment>>

    @Query("SELECT * FROM payments WHERE date = :date ORDER BY id DESC")
    suspend fun getForDateOnce(date: String): List<Payment>

    @Query(
        "SELECT COALESCE(SUM(amount), 0) FROM payments " +
            "WHERE month = :month AND status = 'Paid'"
    )
    fun totalCollectedInMonth(month: String): LiveData<Double>

    @Query(
        "SELECT COALESCE(SUM(amount), 0) FROM payments " +
            "WHERE month = :month AND buildingId = :buildingId AND status = 'Paid'"
    )
    fun totalCollectedInMonthForBuilding(buildingId: Long, month: String): LiveData<Double>

    @Query(
        "SELECT COALESCE(SUM(amount), 0) FROM payments " +
            "WHERE tenantId = :tenantId AND month = :month AND status = 'Paid'"
    )
    fun totalPaidByTenant(tenantId: Long, month: String): LiveData<Double>

    @Query(
        "SELECT COUNT(*) FROM payments " +
            "WHERE tenantId = :tenantId AND month = :month AND status = 'Paid'"
    )
    fun countPaidByTenant(tenantId: Long, month: String): LiveData<Int>

    @Query(
        "SELECT COUNT(*) FROM payments " +
            "WHERE tenantId = :tenantId AND month = :month AND status = 'Paid'"
    )
    suspend fun countPaidOnce(tenantId: Long, month: String): Int

    @Insert
    suspend fun insert(payment: Payment): Long

    @Update
    suspend fun update(payment: Payment)

    @Delete
    suspend fun delete(payment: Payment)
}
