package cc.rulekeeper.dashboard.data.api

import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient(
    private val baseUrl: String,
    private val tokenProvider: () -> String?
) {
    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(Boolean::class.java, IntToBooleanAdapter())
        .registerTypeAdapter(Boolean::class.javaObjectType, IntToBooleanAdapter())
        .create()
    
    private val authInterceptor = Interceptor { chain ->
        val request = chain.request()
        val token = tokenProvider()
        
        val newRequest = if (token != null) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }
        
        chain.proceed(newRequest)
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    
    val authService: AuthService = retrofit.create(AuthService::class.java)
    val guildService: GuildService = retrofit.create(GuildService::class.java)
    val commandService: CommandService = retrofit.create(CommandService::class.java)
    val configService: ConfigService = retrofit.create(ConfigService::class.java)
    val moderationService: ModerationService = retrofit.create(ModerationService::class.java)
    val twitchService: TwitchService = retrofit.create(TwitchService::class.java)
    val youtubeService: YouTubeService = retrofit.create(YouTubeService::class.java)
    val roleService: RoleService = retrofit.create(RoleService::class.java)
    val formsService: FormsService = retrofit.create(FormsService::class.java)
    val ticketsService: TicketsService = retrofit.create(TicketsService::class.java)
    val backupsService: BackupsService = retrofit.create(BackupsService::class.java)
    val logsService: LogsService = retrofit.create(LogsService::class.java)
    val roleMenuService: RoleMenuService = retrofit.create(RoleMenuService::class.java)
    val leaderboardService: LeaderboardService = retrofit.create(LeaderboardService::class.java)
    val usersService: UsersService = retrofit.create(UsersService::class.java)
    val permissionsService: PermissionsService = retrofit.create(PermissionsService::class.java)
    val settingsService: SettingsService = retrofit.create(SettingsService::class.java)
    
    companion object {
        const val DEFAULT_BASE_URL = "https://rulekeeper.cc/api/v1/"
        
        @Volatile
        private var instance: ApiClient? = null
        
        fun getInstance(
            baseUrl: String = DEFAULT_BASE_URL,
            tokenProvider: () -> String?
        ): ApiClient {
            // Always create a new instance to ensure tokenProvider is current
            // The singleton pattern doesn't work well when the tokenProvider changes
            return synchronized(this) {
                ApiClient(baseUrl, tokenProvider).also { instance = it }
            }
        }
        
        fun resetInstance() {
            instance = null
        }
    }
}
