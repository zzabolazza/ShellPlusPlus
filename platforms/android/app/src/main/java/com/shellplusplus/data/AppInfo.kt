package com.shellplusplus.data

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val name: String,
    val icon: Drawable?,
    val url: String,
    val isEnabled: Boolean
)
