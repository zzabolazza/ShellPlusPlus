package com.shellplusplus

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import coil.Coil
import coil.ImageLoader
import coil.decode.SvgDecoder
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ShellPlusPlusApp : Application() {
    override fun onCreate() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        super.onCreate()
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .components {
                    add(SvgDecoder.Factory())
                }
                .build()
        )
    }
}