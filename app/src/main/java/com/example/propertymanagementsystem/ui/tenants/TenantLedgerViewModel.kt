package com.example.propertymanagementsystem.ui.tenants

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.propertymanagementsystem.SahihiApp
import com.example.propertymanagementsystem.data.ledger.TenantLedger
import com.example.propertymanagementsystem.data.model.Payment
import com.example.propertymanagementsystem.data.model.Tenant
import kotlinx.coroutines.launch

class TenantLedgerViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as SahihiApp).container

    fun ledgerFor(tenantId: Long): LiveData<TenantLedger?> =
        container.ledger.ledgers.map { list -> list.find { it.tenant.id == tenantId } }

    fun deletePayment(payment: Payment) {
        viewModelScope.launch { container.ledger.deletePayment(payment) }
    }

    fun deleteTenant(tenant: Tenant, unitId: Long) {
        viewModelScope.launch {
            container.tenants.delete(tenant)
            val unit = container.database.propertyDao().getOnce(unitId)
            if (unit != null) {
                container.units.update(unit.copy(status = com.example.propertymanagementsystem.data.model.Property.STATUS_VACANT))
            }
        }
    }
}
