package com.shellplusplus.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shellplusplus.util.EncryptionUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Singleton
class ConfigManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    private val encryptionUtil: EncryptionUtil
) {
    private val configFile = File(context.filesDir, "app_configs.json")

    data class Config(
        val apps: List<AppConfig> = emptyList(),
        val enabledApps: Set<String> = emptySet()
    )

    private var config = Config()

    init {
        loadConfig()
        // 如果配置文件不存在或为空，初始化默认app
        if (config.apps.isEmpty()) {
            // 延迟初始化，确保加密工具已经准备好
            initializeDefaultAppsAsync()
        }
    }

    private fun initializeDefaultAppsAsync() {
        // 使用协程来异步初始化，避免在构造函数中调用suspend函数
        CoroutineScope(Dispatchers.IO).launch {
            initializeDefaultApps()
        }
    }

    private suspend fun initializeDefaultApps() {
        val defaultApps = DefaultApps.apps
        config = config.copy(
            apps = defaultApps,
            enabledApps = defaultApps.map { it.packageName }.toSet()
        )
        saveConfig()
    }

    private fun loadConfig() {
        if (configFile.exists()) {
            try {
                val encryptedData = configFile.readText()
                val decryptedData = encryptionUtil.decrypt(encryptedData)
                config = gson.fromJson(decryptedData, Config::class.java) ?: Config()
            } catch (e: Exception) {
                e.printStackTrace()
                config = Config()
            }
        }
    }

    private suspend fun saveConfig() = withContext(Dispatchers.IO) {
        try {
            val jsonData = gson.toJson(config)
            val encryptedData = encryptionUtil.encrypt(jsonData)
            configFile.writeText(encryptedData)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getAllApps(): List<AppConfig> = withContext(Dispatchers.IO) {
        config.apps
    }

    suspend fun getAppConfigs(): Set<String> = withContext(Dispatchers.IO) {
        config.enabledApps
    }

    suspend fun saveAppConfigs(configs: Set<String>) = withContext(Dispatchers.IO) {
        config = config.copy(enabledApps = configs)
        saveConfig()
    }

    suspend fun addApp(app: AppConfig) = withContext(Dispatchers.IO) {
        val updatedApps = config.apps.toMutableList()
        val index = updatedApps.indexOfFirst { it.packageName == app.packageName }
        if (index >= 0) {
            updatedApps[index] = app
        } else {
            updatedApps.add(app)
        }
        // 新添加的app默认启用
        val updatedEnabledApps = config.enabledApps.toMutableSet()
        updatedEnabledApps.add(app.packageName)
        
        config = config.copy(
            apps = updatedApps,
            enabledApps = updatedEnabledApps
        )
        saveConfig()
    }

    suspend fun removeApp(packageName: String) = withContext(Dispatchers.IO) {
        val updatedApps = config.apps.filterNot { it.packageName == packageName }
        val updatedEnabledApps = config.enabledApps.filterNot { it == packageName }.toSet()
        
        config = config.copy(
            apps = updatedApps,
            enabledApps = updatedEnabledApps
        )
        saveConfig()
    }

    suspend fun updateApp(app: AppConfig) = withContext(Dispatchers.IO) {
        val updatedApps = config.apps.map { 
            if (it.packageName == app.packageName) app else it 
        }
        config = config.copy(apps = updatedApps)
        saveConfig()
    }
}