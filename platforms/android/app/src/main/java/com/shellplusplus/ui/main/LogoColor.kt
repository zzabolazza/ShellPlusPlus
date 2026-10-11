package com.shellplusplus.ui.main

import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI

private val logoColorCache = mutableMapOf<String, Color>()

fun faviconUrlFor(url: String): String {
    val domain = try {
        URI(url).host ?: "unknown"
    } catch (e: Exception) {
        "unknown"
    }
    return "https://favicon.im/$domain"
}

@Composable
fun rememberLogoColor(url: String): Color {
    val fallback = MaterialTheme.colorScheme.primary
    if (url.isBlank()) return fallback

    val faviconUrl = faviconUrlFor(url)
    val cached = logoColorCache[faviconUrl]
    var color by remember(faviconUrl) { mutableStateOf(cached ?: fallback) }
    val context = LocalContext.current

    LaunchedEffect(faviconUrl) {
        logoColorCache[faviconUrl]?.let {
            color = it
            return@LaunchedEffect
        }
        val request = ImageRequest.Builder(context)
            .data(faviconUrl)
            .allowHardware(false)
            .build()
        val result = context.imageLoader.execute(request)
        if (result !is SuccessResult) return@LaunchedEffect
        val bitmap = result.drawable.toBitmap()
        val extracted = withContext(Dispatchers.Default) {
            extractLogoColor(bitmap)
        } ?: return@LaunchedEffect
        logoColorCache[faviconUrl] = extracted
        color = extracted
    }
    return color
}

private fun extractLogoColor(bitmap: Bitmap): Color? {
    if (bitmap.width < 1 || bitmap.height < 1) return null
    val palette = Palette.from(bitmap).generate()
    val swatch = palette.vibrantSwatch
        ?: palette.darkVibrantSwatch
        ?: palette.mutedSwatch
        ?: palette.dominantSwatch
        ?: return null
    return Color(swatch.rgb)
}

fun Color.forCheckCircle(): Color {
    var color = this
    repeat(3) {
        if (color.luminance() <= 0.75f) return color
        color = color.copy(
            red = color.red * 0.7f,
            green = color.green * 0.7f,
            blue = color.blue * 0.7f
        )
    }
    return color
}
