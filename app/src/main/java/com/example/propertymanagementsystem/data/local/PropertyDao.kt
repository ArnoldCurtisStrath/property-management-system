package com.example.propertymanagementsystem.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.propertymanagementsystem.data.model.Property

/**
 * Everything the app can do with the properties table.
 *
 * The queries that the screens observe return LiveData, so the list on the
 * screen refreshes by itself whenever a record is added, changed or removed.
 */
@Dao
interface PropertyDao {

    @Query("SELECT * FROM properties ORDER BY name")
    fun getAll(): LiveData<List<Property>>

    @Query("SELECT * FROM properties WHERE id = :id")
    fun getById(id: Long): LiveData<Property?>

    // This one is used when a record has to be read once and not watched,
    // for example when checking for a duplicate before saving.
    @Query("SELECT * FROM properties WHERE id = :id")
    suspend fun getOnce(id: Long): Property?

    @Query("SELECT COUNT(*) FROM properties WHERE status = :status")
    fun countByStatus(status: String): LiveData<Int>

    // Used to stop the same property being saved twice. The id is left out of
    // the check so a property can still be edited and saved with its own name.
    @Query(
        "SELECT COUNT(*) FROM properties " +
            "WHERE LOWER(name) = LOWER(:name) AND LOWER(address) = LOWER(:address) AND id != :id"
    )
    suspend fun existsWithSameName(name: String, address: String, id: Long): Int

    @Insert
    suspend fun insert(property: Property): Long

    @Update
    suspend fun update(property: Property)

    @Delete
    suspend fun delete(property: Property)
}
