package com.shellplusplus.data

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppManager @Inject constructor(
    @ApplicationContext private val context: Context,
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

    suspend fun addApp(name: String, url: String) {
        val packageName = name.lowercase().replace(" ", "-")
        val newApp = AppConfig(
            packageName = packageName,
            name = name,
            url = url
        )
        
        // 保存到配置文件
        configManager.addApp(newApp)
    }

    suspend fun removeApp(packageName: String) {
        configManager.removeApp(packageName)
    }

    suspend fun updateApp(app: AppConfig) {
        configManager.updateApp(app)
    }

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