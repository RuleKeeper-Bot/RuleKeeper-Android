package cc.rulekeeper.dashboard.data.repository

import cc.rulekeeper.dashboard.data.api.ApiClient
import cc.rulekeeper.dashboard.data.model.*

class AuthRepository(
    private val apiClient: ApiClient,
    private val settingsRepository: SettingsRepository
) {
    suspend fun login(username: String, password: String, useDiscord: Boolean = false, redirectUri: String? = null, isMobile: Boolean = false): Result<LoginResponse> {
        return try {
            val response = apiClient.authService.login(
                LoginRequest(username, password, useDiscord = useDiscord, redirectUri = redirectUri, isMobile = isMobile)
            )
            
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                
                // Save tokens if available
                if (loginResponse.accessToken != null && loginResponse.refreshToken != null) {
                    settingsRepository.saveTokens(
                        loginResponse.accessToken,
                        loginResponse.refreshToken
                    )
                    
                    // Reset ApiClient instance to pick up new token
                    ApiClient.resetInstance()
                    
                    // Save user info
                    loginResponse.user?.let { user ->
                        settingsRepository.saveUserInfo(
                            user.userId,
                            user.username,
                            user.isAdmin
                        )
                    }
                }
                
                Result.success(loginResponse)
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun handleDiscordCallback(code: String, redirectUri: String? = null): Result<LoginResponse> {
        return try {
            val response = apiClient.authService.discordCallback(
                DiscordCallbackRequest(code, redirectUri)
            )
            
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                
                if (loginResponse.accessToken != null && loginResponse.refreshToken != null) {
                    settingsRepository.saveTokens(
                        loginResponse.accessToken,
                        loginResponse.refreshToken
                    )
                    
                    // Reset ApiClient instance to pick up new token
                    ApiClient.resetInstance()
                    
                    loginResponse.user?.let { user ->
                        settingsRepository.saveUserInfo(
                            user.userId,
                            user.username,
                            user.isAdmin
                        )
                    }
                }
                
                Result.success(loginResponse)
            } else {
                Result.failure(Exception("Discord auth failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun refreshToken(refreshToken: String): Result<LoginResponse> {
        return try {
            val response = apiClient.authService.refreshToken(
                RefreshTokenRequest(refreshToken)
            )
            
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                
                if (loginResponse.accessToken != null) {
                    settingsRepository.saveTokens(
                        loginResponse.accessToken,
                        refreshToken
                    )
                    
                    // Reset ApiClient instance to pick up new token
                    ApiClient.resetInstance()
                }
                
                Result.success(loginResponse)
            } else {
                Result.failure(Exception("Token refresh failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun logout(): Result<Unit> {
        try {
            // Call logout endpoint before clearing tokens (so auth header is still present)
            apiClient.authService.logout()
        } catch (e: Exception) {
            // Ignore logout endpoint errors - we'll clear tokens anyway
        } finally {
            // Always clear tokens and reset instance
            settingsRepository.clearTokens()
            ApiClient.resetInstance()
        }
        return Result.success(Unit)
    }
}
