package com.example.propertymanagementsystem.ui.properties

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.propertymanagementsystem.data.local.AppDatabase
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.data.repository.PropertyRepository
import kotlinx.coroutines.launch

/**
 * Holds the property records for the screens that show them.
 *
 * The ViewModel asks the repository for the data and the screens observe what
 * it gives back. Nothing is done on the screen itself, that is why the list
 * on the screen is still right even after the phone has been turned.
 */
class PropertyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PropertyRepository(
        AppDatabase.getInstance(application).propertyDao()
    )

    // All the properties, in alphabetical order.
    val properties: LiveData<List<Property>> = repository.getAll()

    // One property, used on the details screen.
    fun property(id: Long): LiveData<Property?> = repository.getById(id)

    // Saves a new property. The database gives back the row id it was given.
    fun add(property: Property) {
        viewModelScope.launch {
            repository.add(property)
        }
    }

    // Saves the changes made to a property that is already there.
    fun update(property: Property) {
        viewModelScope.launch {
            repository.update(property)
        }
    }

    // Removes a property together with the tenants and payments under it.
    fun delete(property: Property) {
        viewModelScope.launch {
            repository.delete(property)
        }
    }

    // Checks that the same property has not been saved twice. It is used by
    // the form before a property is saved.
    suspend fun isDuplicate(name: String, address: String, id: Long): Boolean =
        repository.nameAlreadyUsed(name, address, id)
}
