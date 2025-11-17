package cc.rulekeeper.dashboard.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String,
    @SerializedName("use_discord")
    val useDiscord: Boolean = false,
    @SerializedName("redirect_uri")
    val redirectUri: String? = null,
    @SerializedName("is_mobile")
    val isMobile: Boolean = false
)

data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String?,
    @SerializedName("refresh_token")
    val refreshToken: String?,
    @SerializedName("token_type")
    val tokenType: String?,
    @SerializedName("expires_in")
    val expiresIn: Int?,
    val user: User?,
    @SerializedName("oauth_url")
    val oauthUrl: String?,
    val message: String?,
    @SerializedName("requires_mfa")
    val requiresMfa: Boolean?
)

data class DiscordCallbackRequest(
    val code: String,
    @SerializedName("redirect_uri")
    val redirectUri: String? = null
)

data class RefreshTokenRequest(
    @SerializedName("refresh_token")
    val refreshToken: String
)

data class User(
    @SerializedName("user_id")
    val userId: String,
    val username: String,
    val discriminator: String? = null,
    val avatar: String? = null,
    @SerializedName("is_admin")
    val isAdmin: Boolean = false,
    @SerializedName("is_head_admin")
    val isHeadAdmin: Boolean = false,
    val type: String,
    val guilds: List<String>? = null
)
