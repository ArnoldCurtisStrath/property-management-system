package com.example.propertymanagementsystem.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.propertymanagementsystem.data.local.AppDatabase
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.util.Dates

/**
 * Holds the numbers that are shown on the dashboard.
 *
 * The ViewModel gets the data from the repositories, which read it from the
 * database. The screen only observes the values and puts them in the cards,
 * so the numbers are always the ones in the database.
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val propertyDao = AppDatabase.getInstance(application).propertyDao()

    // The payments are needed for the amount of rent that has been collected.
    private val paymentDao = AppDatabase.getInstance(application).paymentDao()

    // The requests are needed for the number of problems that are still open.
    private val maintenanceDao = AppDatabase.getInstance(application).maintenanceDao()

    // The number of properties the landlord has.
    val propertyCount: LiveData<Int> = propertyDao.getAll().map { it.size }

    // How many of those properties are currently rented out.
    val occupiedCount: LiveData<Int> = propertyDao.countByStatus(Property.STATUS_OCCUPIED)

    // How many are empty and could be advertised.
    val vacantCount: LiveData<Int> = propertyDao.countByStatus(Property.STATUS_VACANT)

    // How much rent has been collected in the current month.
    val collectedThisMonth: LiveData<Double> =
        paymentDao.totalCollectedInMonth(Dates.currentMonth())

    // How many maintenance requests are still open.
    val openRequestCount: LiveData<Int> = maintenanceDao.countOpen()
}
