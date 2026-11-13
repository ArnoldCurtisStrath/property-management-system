package com.example.propertymanagementsystem.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.propertymanagementsystem.data.model.MaintenanceRequest
import com.example.propertymanagementsystem.data.model.Payment
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.data.model.Tenant

/**
 * The Room database of the app.
 *
 * All four tables are registered here. Room then generates the code that
 * actually stores and reads the records, and the DAO interfaces above are
 * used to talk to it.
 *
 * The database is only created once and kept for the life of the app. Making
 * it inside a companion object with a null check means every screen gets the
 * same instance instead of opening the database file again and again.
 */
@Database(
    entities = [Property::class, Tenant::class, Payment::class, MaintenanceRequest::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun propertyDao(): PropertyDao
    abstract fun tenantDao(): TenantDao
    abstract fun paymentDao(): PaymentDao
    abstract fun maintenanceDao(): MaintenanceDao

    companion object {
        private const val DATABASE_NAME = "property_management.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            // The double check is enough here. It stops two screens opening the
            // database at the same time and ending up with two copies.
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build().also { instance = it }
            }
        }
    }
}
