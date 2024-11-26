package com.university.marathononline.data.response

import android.content.Context
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import androidx.datastore.preferences.preferencesOf
import androidx.datastore.preferences.remove
import com.university.marathononline.data.models.ERole
import com.university.marathononline.utils.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

class UserPreferences(
    context: Context
) {
    private val applicationContext = context.applicationContext
    private val dataStore: DataStore<Preferences> = applicationContext.createDataStore(
        name = "user_preferences"
    )
    private val secretKey = createOrGetKey()

    private fun encryptData(data: String): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        return Pair(iv, encryptedData)
    }

    private fun decryptData(encryptedData: ByteArray, iv: ByteArray): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)

        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        val decryptedData = cipher.doFinal(encryptedData)
        return String(decryptedData, Charsets.UTF_8)
    }

    val authToken: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[KEY_AUTH_TOKEN_PRE]
        }

    val role: Flow<ERole?>
        get() = dataStore.data.map { preferences ->
            preferences[KEY_AUTH_ROLE_PRE]?.let {
                ERole.valueOf(it)
            }
        }

    val isVerified: Flow<Boolean?>
        get() = dataStore.data.map { preferences ->
            preferences[KEY_AUTH_STATUS_PRE]
        }

    val isDeleted: Flow<Boolean?>
        get() = dataStore.data.map { preferences ->
            preferences[KEY_AUTH_DELETED_PRE]
        }

    val fullName: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[KEY_AUTH_FULL_NAME_PRE]
        }

    val email: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[KEY_AUTH_EMAIL_PRE]
        }

    val password: Flow<String?>
        get() = dataStore.data.map { preferences ->
            val encryptedPassword = preferences[KEY_AUTH_PASSWORD_PRE]?.let { Base64.getDecoder().decode(it) }
            val iv = preferences[KEY_AUTH_PASSWORD_IV_PRE]?.let { Base64.getDecoder().decode(it) }

            if (encryptedPassword != null && iv != null) {
                decryptData(encryptedPassword, iv)
            } else {
                null
            }
        }

    val remember: Flow<Boolean?>
        get() = dataStore.data.map { preferences ->
            preferences[KEY_AUTH_REMEMBER]
        }

    suspend fun saveAuthToken(authToken: String){
        dataStore.edit {
            preferences ->
            preferences[KEY_AUTH_TOKEN_PRE] = authToken
        }
    }

    suspend fun saveRoleUser(role: ERole) {
        dataStore.edit {
                preferences ->
            preferences[KEY_AUTH_ROLE_PRE] = role.name
        }
    }

    suspend fun saveFullName(fullName: String) {
        dataStore.edit {
                preferences ->
            preferences[KEY_AUTH_FULL_NAME_PRE] = fullName
        }
    }

    suspend fun saveStatusUser(verified: Boolean) {
        dataStore.edit {
                preferences ->
            preferences[KEY_AUTH_STATUS_PRE] = verified
        }
    }

    suspend fun saveDeleted(isDeleted: Boolean){
        dataStore.edit {
                preferences ->
            preferences[KEY_AUTH_DELETED_PRE] = isDeleted
        }
    }

    suspend fun saveAuthenticated(fullName: String, accessToken: String, role: ERole, isVerified: Boolean, isDeleted: Boolean){
        dataStore.edit {
            preferences ->
            preferences[KEY_AUTH_FULL_NAME_PRE] = fullName
            preferences[KEY_AUTH_TOKEN_PRE] = accessToken
            preferences[KEY_AUTH_ROLE_PRE] = role.name
            preferences[KEY_AUTH_STATUS_PRE] = isVerified
            preferences[KEY_AUTH_DELETED_PRE] = isDeleted
        }
    }

    suspend fun saveLoginInfo(email: String, password: String) {
        val (passwordIv, encryptedPassword) = encryptData(password)
        dataStore.edit {
                preferences ->
            preferences[KEY_AUTH_EMAIL_PRE] = email
            preferences[KEY_AUTH_PASSWORD_PRE] = Base64.getEncoder().encodeToString(encryptedPassword)
            preferences[KEY_AUTH_PASSWORD_IV_PRE] = Base64.getEncoder().encodeToString(passwordIv)
            preferences[KEY_AUTH_REMEMBER] = true
        }
    }

    suspend fun clearAuth() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_AUTH_TOKEN_PRE)
            preferences.remove(KEY_AUTH_ROLE_PRE)
            preferences.remove(KEY_AUTH_STATUS_PRE)
            preferences.remove(KEY_AUTH_FULL_NAME_PRE)
        }
    }

    suspend fun clearLoginInfo() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_AUTH_EMAIL_PRE)
            preferences.remove(KEY_AUTH_PASSWORD_PRE)
            preferences.remove(KEY_AUTH_PASSWORD_IV_PRE)
            preferences[KEY_AUTH_REMEMBER] = false
        }
    }

    suspend fun clearStatusUser(verify: Boolean) {
        dataStore.edit { preferences ->
            preferences.remove(KEY_AUTH_STATUS_PRE)
        }
    }

    suspend fun clearAuthenticated() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_AUTH_FULL_NAME_PRE)
            preferences.remove(KEY_AUTH_TOKEN_PRE)
            preferences.remove(KEY_AUTH_ROLE_PRE)
            preferences.remove(KEY_AUTH_STATUS_PRE)
            preferences.remove(KEY_AUTH_DELETED_PRE)
        }
    }

    companion object{
        private val KEY_AUTH_TOKEN_PRE = preferencesKey<String>(KEY_AUTH_TOKEN)
        private val KEY_AUTH_ROLE_PRE = preferencesKey<String>(KEY_AUTH_ROLE)
        private val KEY_AUTH_STATUS_PRE = preferencesKey<Boolean>(KEY_AUTH_STATUS)
        private val KEY_AUTH_DELETED_PRE = preferencesKey<Boolean>(KEY_AUTH_DELETED)
        private val KEY_AUTH_EMAIL_PRE = preferencesKey<String>(KEY_EMAIL)
        private val KEY_AUTH_FULL_NAME_PRE = preferencesKey<String>(KEY_FULL_NAME)
        private val KEY_AUTH_PASSWORD_PRE = preferencesKey<String>(KEY_PASSWORD)
        private val KEY_AUTH_PASSWORD_IV_PRE = preferencesKey<String>(KEY_PASSWORD_IV)
        private val KEY_AUTH_REMEMBER = preferencesKey<Boolean>(KEY_REMEMBER_ME)
    }
}