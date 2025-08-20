package com.shellplusplus.di

import android.content.Context
import com.google.gson.Gson
import com.shellplusplus.data.AppManager
import com.shellplusplus.data.ConfigManager
import com.shellplusplus.data.IconCache
import com.shellplusplus.service.AppSwitchGestureDetector
import com.shellplusplus.util.EncryptionUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun provideConfigManager(
        @ApplicationContext context: Context,
        gson: Gson,
        encryptionUtil: EncryptionUtil
    ): ConfigManager {
        return ConfigManager(context, gson, encryptionUtil)
    }

    @Provides
    @Singleton
    fun provideAppManager(
        @ApplicationContext context: Context,
        configManager: ConfigManager
    ): AppManager {
        return AppManager(context, configManager)
    }

    @Provides
    @Singleton
    fun provideAppSwitchGestureDetector(): AppSwitchGestureDetector {
        return AppSwitchGestureDetector()
    }

    @Provides
    @Singleton
    fun provideIconCache(): IconCache {
        return IconCache()
    }

    @Provides
    @Singleton
    fun provideEncryptionUtil(): EncryptionUtil {
        return EncryptionUtil()
    }
}