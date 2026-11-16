package com.example.propertymanagementsystem.ui.payments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.propertymanagementsystem.data.local.AppDatabase
import com.example.propertymanagementsystem.data.model.Payment
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.data.model.Tenant
import com.example.propertymanagementsystem.data.repository.PaymentRepository
import com.example.propertymanagementsystem.data.repository.PropertyRepository
import com.example.propertymanagementsystem.data.repository.TenantRepository
import kotlinx.coroutines.launch

/**
 * Holds the payment records and the numbers that are worked out from them.
 */
class PaymentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PaymentRepository(
        AppDatabase.getInstance(application).paymentDao()
    )

    private val tenantRepository = TenantRepository(
        AppDatabase.getInstance(application).tenantDao()
    )

    private val propertyRepository = PropertyRepository(
        AppDatabase.getInstance(application).propertyDao()
    )

    // Every payment that has been recorded.
    val payments: LiveData<List<Payment>> = repository.getAll()

    // The tenants that a payment can be recorded against.
    val tenants: LiveData<List<Tenant>> = tenantRepository.getActive()

    // All the properties, used to show which property a payment belongs to.
    val properties: LiveData<List<Property>> = propertyRepository.getAll()

    // The payments of one tenant.
    fun forTenant(tenantId: Long): LiveData<List<Payment>> = repository.getForTenant(tenantId)

    // How much rent was collected in a month.
    fun totalCollected(month: String): LiveData<Double> = repository.totalCollectedInMonth(month)

    // What one tenant has paid for one month.
    fun totalPaidByTenant(tenantId: Long, month: String): LiveData<Double> =
        repository.totalPaidByTenant(tenantId, month)

    // How many payments a tenant has for one month.
    fun countPaidByTenant(tenantId: Long, month: String): LiveData<Int> =
        repository.countPaidByTenant(tenantId, month)

    fun add(payment: Payment) {
        viewModelScope.launch {
            repository.add(payment)
        }
    }

    fun delete(payment: Payment) {
        viewModelScope.launch {
            repository.delete(payment)
        }
    }
}
