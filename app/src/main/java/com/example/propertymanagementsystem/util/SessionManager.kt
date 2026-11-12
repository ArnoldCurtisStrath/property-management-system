package com.example.propertymanagementsystem.util

import android.content.Context
import java.security.MessageDigest

/**
 * Keeps the account that was created and whether somebody is signed in.
 *
 * The app is used by one landlord so a full user table is not needed. The
 * details are kept in SharedPreferences, which is a small place on the phone
 * that survives closing the app.
 *
 * The password is not stored as it is typed. It is run through a hash first,
 * so what is saved on the phone is not the real password.
 */
class SessionManager(context: Context) {

    private val preferences = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    // Saves the new account. The password is hashed before it is stored.
    fun register(email: String, password: String) {
        preferences.edit()
            .putString(KEY_EMAIL, email.trim())
            .putString(KEY_PASSWORD, hash(password))
            .apply()
    }

    // Checks the details that were typed against the saved account.
    // Returns false when there is no account yet or when the details are wrong.
    fun login(email: String, password: String): Boolean {
        val savedEmail = preferences.getString(KEY_EMAIL, null)
        val savedPassword = preferences.getString(KEY_PASSWORD, null)
        if (savedEmail == null || savedPassword == null) return false
        return savedEmail.equals(email.trim(), ignoreCase = true) &&
            savedPassword == hash(password)
    }

    // True when an account has been created on this phone.
    fun hasAccount(): Boolean = preferences.contains(KEY_EMAIL)

    // Remembers that somebody is signed in and which account it is.
    fun setSignedIn(email: String) {
        preferences.edit().putBoolean(KEY_SIGNED_IN, true).putString(KEY_ACCOUNT, email).apply()
    }

    fun isSignedIn(): Boolean = preferences.getBoolean(KEY_SIGNED_IN, false)

    // The email of the person who is signed in. It is shown on the drawer.
    fun signedInEmail(): String = preferences.getString(KEY_ACCOUNT, "") ?: ""

    // Called when the user logs out. The account is kept so they can sign in
    // again without creating a new one.
    fun signOut() {
        preferences.edit().putBoolean(KEY_SIGNED_IN, false).apply()
    }

    // Turns the password into a short string using SHA-256. The same password
    // always gives the same result, so it can be compared later.
    private fun hash(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
        private const val KEY_SIGNED_IN = "signed_in"
        private const val KEY_ACCOUNT = "account"
    }
}
