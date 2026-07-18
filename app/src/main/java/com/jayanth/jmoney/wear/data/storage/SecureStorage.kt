@file:Suppress("DEPRECATION")
package com.jayanth.jmoney.wear.data.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("DEPRECATION")
@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "jmoney_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )


    fun saveSessionJson(json: String) {
        sharedPreferences.edit().putString(KEY_SESSION, json).apply()
    }

    fun getSessionJson(): String? {
        return sharedPreferences.getString(KEY_SESSION, null)
    }

    fun clearSession() {
        sharedPreferences.edit().remove(KEY_SESSION).apply()
    }

    companion object {
        private const val KEY_SESSION = "supabase_session"
    }
}
