package com.example.propertymanagementsystem.data.repository

import android.content.Context
import android.content.SharedPreferences

/**
 * The small business settings that make a statement look like the agent's
 * own document: the business name, the fee and the receipt prefix.
 */
class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("sahihi_settings", Context.MODE_PRIVATE)

    var businessName: String
        get() = prefs.getString(KEY_BUSINESS, "My Property Agency") ?: "My Property Agency"
        set(value) = prefs.edit().putString(KEY_BUSINESS, value.trim()).apply()

    var agentName: String
        get() = prefs.getString(KEY_AGENT, "") ?: ""
        set(value) = prefs.edit().putString(KEY_AGENT, value.trim()).apply()

    var phone: String
        get() = prefs.getString(KEY_PHONE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PHONE, value.trim()).apply()

    var managementFee: Double
        get() = prefs.getFloat(KEY_FEE, 10f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_FEE, value.toFloat()).apply()

    var receiptPrefix: String
        get() = prefs.getString(KEY_PREFIX, "SH") ?: "SH"
        set(value) = prefs.edit().putString(KEY_PREFIX, value.trim().ifBlank { "SH" }).apply()

    var defaultCollector: String
        get() = prefs.getString(KEY_COLLECTOR, "Me") ?: "Me"
        set(value) = prefs.edit().putString(KEY_COLLECTOR, value.trim().ifBlank { "Me" }).apply()

    fun preparedBy(): String {
        val business = businessName.trim()
        val agent = agentName.trim()
        return when {
            business.isNotEmpty() -> business
            agent.isNotEmpty() -> agent
            else -> "Sahihi Agent"
        }
    }

    fun hasBusinessProfile(): Boolean = businessName.isNotBlank() || agentName.isNotBlank()

    companion object {
        private const val KEY_BUSINESS = "business_name"
        private const val KEY_AGENT = "agent_name"
        private const val KEY_PHONE = "phone"
        private const val KEY_FEE = "management_fee"
        private const val KEY_PREFIX = "receipt_prefix"
        private const val KEY_COLLECTOR = "default_collector"
    }
}
