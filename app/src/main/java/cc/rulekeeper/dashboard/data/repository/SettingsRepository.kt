package cc.rulekeeper.dashboard.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    
    companion object {
        val API_BASE_URL = stringPreferencesKey("api_base_url")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val IS_ADMIN = stringPreferencesKey("is_admin")
        
        const val DEFAULT_API_URL = "https://rulekeeper.cc/api/v1/"
    }
    
    // Cached token for synchronous access in OkHttp interceptor
    @Volatile
    private var cachedAccessToken: String? = null
    
    // Scope for the repository to manage coroutines
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    val apiBaseUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[API_BASE_URL] ?: DEFAULT_API_URL
    }
    
    val accessToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN]
    }
    
    val refreshToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN]
    }
    
    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID]
    }
    
    val username: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USERNAME]
    }
    
    init {
        // Initialize cached token from DataStore
        repositoryScope.launch {
            try {
                accessToken.collect { token ->
                    cachedAccessToken = token
                }
            } catch (e: Exception) {
                // Log error but don't crash
                e.printStackTrace()
            }
        }
    }
    
    val isAdmin: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_ADMIN]?.toBoolean() ?: false
    }
    
    // Synchronous access to cached token for OkHttp interceptor
    fun getCachedAccessToken(): String? = cachedAccessToken
    
    suspend fun setApiBaseUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[API_BASE_URL] = url
        }
    }
    
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        cachedAccessToken = accessToken
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
        }
    }
    
    suspend fun saveUserInfo(userId: String, username: String, isAdmin: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID] = userId
            preferences[USERNAME] = username
            preferences[IS_ADMIN] = isAdmin.toString()
        }
    }
    
    suspend fun clearTokens() {
        cachedAccessToken = null
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN)
            preferences.remove(REFRESH_TOKEN)
            preferences.remove(USER_ID)
            preferences.remove(USERNAME)
            preferences.remove(IS_ADMIN)
        }
    }
    
    suspend fun clearAll() {
        cachedAccessToken = null
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
