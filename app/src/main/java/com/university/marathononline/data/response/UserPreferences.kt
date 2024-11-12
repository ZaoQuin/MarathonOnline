package com.university.marathononline.data.response

import android.content.Context
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import androidx.datastore.preferences.remove
import com.university.marathononline.data.models.ERole
import com.university.marathononline.utils.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences(
    context: Context
) {
    private val applicationContext = context.applicationContext
    private val dataStore: DataStore<Preferences> = applicationContext.createDataStore(
        name = "user_preferences"
    )

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

    suspend fun saveStatusUser(verified: Boolean) {
        dataStore.edit {
                preferences ->
            preferences[KEY_AUTH_STATUS_PRE] = verified
        }
    }

    suspend fun clearAuth() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_AUTH_TOKEN_PRE)
            preferences.remove(KEY_AUTH_ROLE_PRE)
            preferences.remove(KEY_AUTH_STATUS_PRE)
        }
    }

    suspend fun clearStatusUser(verify: Boolean) {
        dataStore.edit { preferences ->
            preferences.remove(KEY_AUTH_STATUS_PRE)
        }
    }

    companion object{
        private val KEY_AUTH_TOKEN_PRE = preferencesKey<String>(KEY_AUTH_TOKEN)
        private val KEY_AUTH_ROLE_PRE = preferencesKey<String>(KEY_AUTH_ROLE)
        private val KEY_AUTH_STATUS_PRE = preferencesKey<Boolean>(KEY_AUTH_STATUS)
    }
}