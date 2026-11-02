package com.example.data

import android.content.Context
import android.content.SharedPreferences

data class EmergencyContact(
    val id: Int,
    val name: String,
    val phoneNumber: String
)

class EmergencyStorage(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("alic_emergency_prefs", Context.MODE_PRIVATE)

    fun getContacts(): List<EmergencyContact> {
        return listOf(
            EmergencyContact(
                id = 1,
                name = prefs.getString("contact_1_name", "Contact 1 (Emergency)") ?: "Contact 1 (Emergency)",
                phoneNumber = prefs.getString("contact_1_phone", "") ?: ""
            ),
            EmergencyContact(
                id = 2,
                name = prefs.getString("contact_2_name", "Contact 2 (Emergency)") ?: "Contact 2 (Emergency)",
                phoneNumber = prefs.getString("contact_2_phone", "") ?: ""
            ),
            EmergencyContact(
                id = 3,
                name = prefs.getString("contact_3_name", "Contact 3 (Emergency)") ?: "Contact 3 (Emergency)",
                phoneNumber = prefs.getString("contact_3_phone", "") ?: ""
            )
        )
    }

    fun saveContact(id: Int, name: String, phone: String) {
        prefs.edit()
            .putString("contact_${id}_name", name.trim())
            .putString("contact_${id}_phone", phone.trim())
            .apply()
    }

    fun hasConfiguredContacts(): Boolean {
        return getContacts().any { it.phoneNumber.isNotBlank() }
    }
}
