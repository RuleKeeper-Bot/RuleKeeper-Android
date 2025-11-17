package cc.rulekeeper.dashboard

import android.app.Application
import cc.rulekeeper.dashboard.data.repository.SettingsRepository

class RuleKeeperApplication : Application() {
    lateinit var settingsRepository: SettingsRepository
    
    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(this)
    }
}
