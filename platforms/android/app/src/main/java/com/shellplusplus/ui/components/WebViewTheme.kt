package com.shellplusplus.ui.components

import android.webkit.WebSettings
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

fun applyWebViewDarkTheme(settings: WebSettings, darkTheme: Boolean) {
    when {
        WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING) -> {
            WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, darkTheme)
        }
        WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK) -> {
            @Suppress("DEPRECATION")
            WebSettingsCompat.setForceDark(
                settings,
                if (darkTheme) WebSettingsCompat.FORCE_DARK_AUTO else WebSettingsCompat.FORCE_DARK_OFF
            )
        }
    }
}
