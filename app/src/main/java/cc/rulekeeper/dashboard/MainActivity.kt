package cc.rulekeeper.dashboard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import cc.rulekeeper.dashboard.ui.navigation.RuleKeeperNavigation
import cc.rulekeeper.dashboard.ui.theme.RuleKeeperTheme

class MainActivity : ComponentActivity() {
    private var oauthCode by mutableStateOf<String?>(null)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate()
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Handle OAuth callback
        handleIntent(intent)
        
        val app = application as RuleKeeperApplication
        
        setContent {
            RuleKeeperTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RuleKeeperNavigation(
                        settingsRepository = app.settingsRepository,
                        oauthCode = oauthCode,
                        onOauthCodeProcessed = {
                            // Clear the code after it's been used
                            oauthCode = null
                        }
                    )
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent?) {
        val data: Uri? = intent?.data
        
        if (data != null && data.scheme == "cc.rulekeeper.dashboard" && data.host == "callback") {
            // Extract the authorization code from the callback URL
            val code = data.getQueryParameter("code")
            if (code != null) {
                Log.d("MainActivity", "Received OAuth code: $code")
                oauthCode = code
            } else {
                Log.e("MainActivity", "No code in OAuth callback")
            }
        }
    }
}
