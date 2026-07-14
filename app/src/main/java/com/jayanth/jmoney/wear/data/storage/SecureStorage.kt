package com.jayanth.jmoney.wear.data.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext context: Context
) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences = EncryptedSharedPreferences.create(
        "jmoney_secure_prefs",
        masterKeyAlias,
        context,
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
