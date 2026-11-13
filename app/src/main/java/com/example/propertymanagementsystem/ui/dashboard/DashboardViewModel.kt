package com.example.propertymanagementsystem.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.propertymanagementsystem.data.local.AppDatabase
import com.example.propertymanagementsystem.data.model.Property

/**
 * Holds the numbers that are shown on the dashboard.
 *
 * The ViewModel gets the data from the repositories, which read it from the
 * database. The screen only observes the values and puts them in the cards,
 * so the numbers are always the ones in the database.
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val propertyDao = AppDatabase.getInstance(application).propertyDao()

    // The number of properties the landlord has.
    val propertyCount: LiveData<Int> = propertyDao.getAll().map { it.size }

    // How many of those properties are currently rented out.
    val occupiedCount: LiveData<Int> = propertyDao.countByStatus(Property.STATUS_OCCUPIED)

    // How many are empty and could be advertised.
    val vacantCount: LiveData<Int> = propertyDao.countByStatus(Property.STATUS_VACANT)
}
