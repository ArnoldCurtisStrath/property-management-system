package com.example.propertymanagementsystem.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.propertymanagementsystem.data.model.Building
import com.example.propertymanagementsystem.data.model.Expense
import com.example.propertymanagementsystem.data.model.MaintenanceRequest
import com.example.propertymanagementsystem.data.model.Payment
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.data.model.Tenant

/**
 * The Room database of Sahihi.
 *
 * Version 2 adds the building layer, the expense ledger and the receipt
 * details on a payment. The migration below upgrades a version 1 database in
 * place, so a phone that already has data does not lose it.
 */
@Database(
    entities = [
        Building::class,
        Property::class,
        Tenant::class,
        Payment::class,
        Expense::class,
        MaintenanceRequest::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun buildingDao(): BuildingDao
    abstract fun propertyDao(): PropertyDao
    abstract fun tenantDao(): TenantDao
    abstract fun paymentDao(): PaymentDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun maintenanceDao(): MaintenanceDao

    companion object {
        private const val DATABASE_NAME = "property_management.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
        }

        /**
         * Version 1 stored a property as the rentable thing. Version 2 adds
         * the building above it, the expense ledger and the receipt fields.
         *
         * Existing properties are put into one "My portfolio" building so no
         * old record is lost. The new columns have defaults because SQLite
         * cannot add a NOT NULL column without one.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `buildings` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `address` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `ownerName` TEXT NOT NULL,
                        `ownerPhone` TEXT NOT NULL,
                        `ownerEmail` TEXT NOT NULL,
                        `managementFee` REAL NOT NULL,
                        `notes` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    "ALTER TABLE `properties` ADD COLUMN `buildingId` INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_properties_buildingId` " +
                        "ON `properties` (`buildingId`)"
                )

                db.execSQL(
                    "ALTER TABLE `tenants` ADD COLUMN `dueDay` INTEGER NOT NULL DEFAULT 1"
                )
                db.execSQL(
                    "ALTER TABLE `tenants` ADD COLUMN `deposit` REAL NOT NULL DEFAULT 0"
                )
                db.execSQL("ALTER TABLE `tenants` ADD COLUMN `endDate` TEXT")
                db.execSQL("ALTER TABLE `tenants` ADD COLUMN `notes` TEXT")

                db.execSQL(
                    "ALTER TABLE `payments` ADD COLUMN `buildingId` INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "ALTER TABLE `payments` ADD COLUMN `receiptNumber` TEXT NOT NULL DEFAULT ''"
                )
                db.execSQL("ALTER TABLE `payments` ADD COLUMN `reference` TEXT")
                db.execSQL("ALTER TABLE `payments` ADD COLUMN `note` TEXT")
                db.execSQL(
                    "ALTER TABLE `payments` ADD COLUMN `recordedBy` TEXT NOT NULL DEFAULT 'Owner'"
                )
                db.execSQL("ALTER TABLE `payments` ADD COLUMN `balanceAfter` REAL")
                db.execSQL(
                    "ALTER TABLE `payments` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_payments_buildingId` " +
                        "ON `payments` (`buildingId`)"
                )

                db.execSQL(
                    "ALTER TABLE `maintenance` ADD COLUMN `priority` TEXT NOT NULL DEFAULT 'Normal'"
                )
                db.execSQL(
                    "ALTER TABLE `maintenance` ADD COLUMN `cost` REAL NOT NULL DEFAULT 0"
                )
                db.execSQL("ALTER TABLE `maintenance` ADD COLUMN `vendor` TEXT")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `expenses` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `buildingId` INTEGER NOT NULL,
                        `propertyId` INTEGER,
                        `category` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `date` TEXT NOT NULL,
                        `vendor` TEXT NOT NULL,
                        `method` TEXT NOT NULL,
                        `reference` TEXT,
                        `note` TEXT,
                        `maintenanceId` INTEGER,
                        `recordedBy` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_expenses_buildingId` " +
                        "ON `expenses` (`buildingId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_expenses_propertyId` " +
                        "ON `expenses` (`propertyId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_expenses_date` ON `expenses` (`date`)"
                )

                // Put any existing unit into one portfolio building.
                db.execSQL(
                    """
                    INSERT INTO `buildings`
                        (`name`, `address`, `type`, `ownerName`, `ownerPhone`,
                         `ownerEmail`, `managementFee`, `notes`, `createdAt`)
                    SELECT 'My portfolio', 'Nairobi', 'Apartment block', '', '', '',
                           10.0, 'Created from version 1', strftime('%s','now') * 1000
                    WHERE EXISTS (SELECT 1 FROM `properties`)
                    """.trimIndent()
                )
                db.execSQL(
                    "UPDATE `properties` SET `buildingId` = " +
                        "(SELECT `id` FROM `buildings` ORDER BY `id` LIMIT 1) " +
                        "WHERE `buildingId` = 0"
                )
                db.execSQL(
                    "UPDATE `payments` SET `buildingId` = " +
                        "(SELECT `buildingId` FROM `properties` " +
                        "WHERE `properties`.`id` = `payments`.`propertyId`) " +
                        "WHERE `buildingId` = 0"
                )
                db.execSQL(
                    "UPDATE `payments` SET `receiptNumber` = 'SH-' || " +
                        "COALESCE(substr(`date`, 1, 4), strftime('%Y','now')) || '-' || " +
                        "printf('%04d', `id`) WHERE `receiptNumber` = ''"
                )
                db.execSQL(
                    "UPDATE `payments` SET `createdAt` = " +
                        "COALESCE(CAST(strftime('%s', `date`) AS INTEGER) * 1000, 0) " +
                        "WHERE `createdAt` = 0"
                )
            }
        }
    }
}
