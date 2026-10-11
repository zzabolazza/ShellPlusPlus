package com.shellplusplus.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppManager @Inject constructor(
    private val configManager: ConfigManager
) {
    suspend fun getApps(): List<AppInfo> {
        val allApps = configManager.getAllApps()
        val enabledApps = configManager.getAppConfigs()
        
        return allApps.map { appConfig ->
            appConfigToAppInfo(appConfig, enabledApps.contains(appConfig.packageName))
        }
    }

    private fun appConfigToAppInfo(appConfig: AppConfig, isEnabled: Boolean): AppInfo {
        return AppInfo(
            packageName = appConfig.packageName,
            name = appConfig.name,
            icon = null,
            url = appConfig.url,
            isEnabled = isEnabled
        )
    }

    fun getSourceUrl(): String = configManager.getSourceUrl()

    fun setSourceUrl(url: String) {
        configManager.setSourceUrl(url)
    }

    suspend fun syncApps(): Boolean = configManager.syncFromSource()

    suspend fun toggleAppEnabled(app: AppInfo) {
        val enabledApps = configManager.getAppConfigs().toMutableSet()
        if (app.isEnabled) {
            enabledApps.remove(app.packageName)
        } else {
            enabledApps.add(app.packageName)
        }
        configManager.saveAppConfigs(enabledApps)
    }
}