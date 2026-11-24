package com.example.propertymanagementsystem.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.propertymanagementsystem.data.model.Expense

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
    fun getAll(): LiveData<List<Expense>>

    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
    suspend fun getAllOnce(): List<Expense>

    @Query("SELECT * FROM expenses WHERE substr(date, 1, 7) = :month ORDER BY date DESC")
    fun getForMonth(month: String): LiveData<List<Expense>>

    @Query("SELECT * FROM expenses WHERE buildingId = :buildingId ORDER BY date DESC")
    fun getForBuilding(buildingId: Long): LiveData<List<Expense>>

    @Query(
        "SELECT * FROM expenses WHERE buildingId = :buildingId " +
            "AND substr(date, 1, 7) = :month ORDER BY date DESC"
    )
    fun getForBuildingMonth(buildingId: Long, month: String): LiveData<List<Expense>>

    @Query(
        "SELECT * FROM expenses WHERE substr(date, 1, 7) = :month ORDER BY date DESC"
    )
    suspend fun getForMonthOnce(month: String): List<Expense>

    @Insert
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)
}
