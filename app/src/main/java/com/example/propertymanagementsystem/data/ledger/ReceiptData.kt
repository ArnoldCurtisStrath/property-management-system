package com.example.propertymanagementsystem.data.ledger

import com.example.propertymanagementsystem.data.model.Building
import com.example.propertymanagementsystem.data.model.Payment
import com.example.propertymanagementsystem.data.model.Property
import com.example.propertymanagementsystem.data.model.Tenant

/**
 * Everything the receipt screen needs to draw a receipt and share it.
 */
data class ReceiptData(
    val payment: Payment,
    val tenant: Tenant,
    val unit: Property?,
    val building: Building?
)
