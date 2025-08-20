package com.shellplusplus.data

import android.graphics.drawable.Drawable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconCache @Inject constructor() {
    private val cache = mutableMapOf<String, Drawable>()

    fun get(packageName: String): Drawable? = cache[packageName]

    fun put(packageName: String, drawable: Drawable) {
        cache[packageName] = drawable
    }

    fun clear() {
        cache.clear()
    }
}