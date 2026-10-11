package com.shellplusplus.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shellplusplus.util.EncryptionUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfigManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    private val encryptionUtil: EncryptionUtil
) {
    private val configFile = File(context.filesDir, "app_configs.json")
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    data class Config(
        val apps: List<AppConfig> = emptyList(),
        val enabledApps: Set<String> = emptySet()
    )

    private var config = Config()
    private var hasSavedApps = false

    init {
        loadConfig()
        if (!hasSavedApps) {
            loadBundledApps()
        }
    }

    fun getSourceUrl(): String {
        return preferences.getString(KEY_SOURCE_URL, DEFAULT_SOURCE_URL)
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_SOURCE_URL
    }

    fun setSourceUrl(url: String) {
        preferences.edit().putString(KEY_SOURCE_URL, url.trim()).apply()
    }

    suspend fun syncFromSource(): Boolean = withContext(Dispatchers.IO) {
        try {
            val apps = downloadApps(getSourceUrl())
            if (apps.isEmpty()) return@withContext false
            replaceApps(apps)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            if (!hasSavedApps) {
                loadBundledApps()
            }
            false
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

    private fun loadBundledApps() {
        try {
            val apps = context.assets.open(BUNDLED_APPS).bufferedReader().use { reader ->
                parseApps(reader.readText())
            }
            if (apps.isNotEmpty()) {
                config = config.copy(
                    apps = apps,
                    enabledApps = apps.map { it.packageName }.toSet()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun downloadApps(sourceUrl: String): List<AppConfig> {
        val separator = if (sourceUrl.contains("?")) "&" else "?"
        val requestUrl = "$sourceUrl${separator}t=${System.currentTimeMillis()}"
        val connection = (URL(requestUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Cache-Control", "no-cache")
            setRequestProperty("Accept", "application/json")
        }
        try {
            val code = connection.responseCode
            if (code !in 200..299) {
                throw IllegalStateException("应用列表请求失败: $code")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            return parseApps(body)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseApps(json: String): List<AppConfig> {
        val type = object : TypeToken<List<AppConfig>>() {}.type
        val apps: List<AppConfig> = gson.fromJson(json, type) ?: emptyList()
        return apps.filter { app ->
            app.packageName.isNotBlank() && app.name.isNotBlank() && app.url.isNotBlank()
        }
    }

    private suspend fun replaceApps(apps: List<AppConfig>) {
        config = config.copy(
            apps = apps,
            enabledApps = apps.map { it.packageName }.toSet()
        )
        saveConfig()
        hasSavedApps = true
    }

    private fun loadConfig() {
        if (!configFile.exists()) return
        try {
            val encryptedData = configFile.readText()
            val decryptedData = encryptionUtil.decrypt(encryptedData)
            config = gson.fromJson(decryptedData, Config::class.java) ?: Config()
            hasSavedApps = config.apps.isNotEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
            config = Config()
            hasSavedApps = false
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

    companion object {
        const val DEFAULT_SOURCE_URL =
            "https://raw.githubusercontent.com/zzabolazza/ShellPlusPlus/main/apps.json"
        private const val PREFS_NAME = "shell_prefs"
        private const val KEY_SOURCE_URL = "app_list_source"
        private const val BUNDLED_APPS = "apps.json"
    }
}
