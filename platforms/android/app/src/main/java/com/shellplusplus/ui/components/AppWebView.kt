package com.shellplusplus.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.*
import android.net.http.SslError
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Modifier
import android.util.Log
import androidx.activity.compose.BackHandler
import kotlin.math.abs
import kotlin.math.hypot

@SuppressLint("ClickableViewAccessibility")
@Composable
fun AppWebView(
    url: String, 
    modifier: Modifier = Modifier,
    active: Boolean = true,
    onTitleChanged: (String) -> Unit = {},
    onTwoFingerTouch: () -> Unit = {},
    onShowSwitcher: () -> Unit = {},
    onTwoFingerSwipeDown: () -> Unit = {},
    onPinchIn: () -> Unit = {},
    touchEnabled: Boolean = true
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    val darkTheme = isSystemInDarkTheme()
    val webViewBackground = MaterialTheme.colorScheme.background.toArgb()
    val currentOnTwoFingerTouch by rememberUpdatedState(onTwoFingerTouch)
    val currentOnShowSwitcher by rememberUpdatedState(onShowSwitcher)
    val currentOnTwoFingerSwipeDown by rememberUpdatedState(onTwoFingerSwipeDown)
    val currentOnPinchIn by rememberUpdatedState(onPinchIn)
    val currentTouchEnabled by rememberUpdatedState(touchEnabled)
    
    // 处理返回键
    BackHandler(enabled = active) {
        webView?.let { view ->
            if (view.canGoBack()) {
                view.goBack()
            }
        }
    }
    
    AndroidView(
        factory = { ctx ->
            PassThroughWebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                webViewClient = object : WebViewClient() {
                    @Deprecated("Deprecated in Java")
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        val urlString = request.url.toString()
                        Log.d("AppWebView", "正在加载URL: $urlString")
                        
                        // 处理Google登录相关的URL
                        if (urlString.contains("accounts.google.com") || 
                            urlString.contains("oauth") ||
                            urlString.contains("google.com/oauth")) {
                            return false // 允许Google登录相关页面正常加载
                        }
                        
                        return if (urlString.startsWith("http://")) {
                            val httpsUrl = urlString.replace("http://", "https://")
                            view.loadUrl(httpsUrl)
                            true
                        } else {
                            false
                        }
                    }

                    override fun onReceivedError(
                        view: WebView,
                        request: WebResourceRequest,
                        error: WebResourceError
                    ) {
                        Log.e("AppWebView", "页面加载错误: ${error.errorCode} - ${request.url}")
                        if (error.errorCode == ERROR_FILE_NOT_FOUND) {
                            view.reload()
                        } else {
                            super.onReceivedError(view, request, error)
                        }
                    }
                    
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.d("AppWebView", "页面加载完成: $url")
                        // 页面加载完成后更新标题
                        view?.title?.let { title ->
                            if (title.isNotEmpty()) {
                                onTitleChanged(title)
                            }
                        }
                    }
                    
                    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                        Log.w("AppWebView", "SSL错误: ${error?.toString()}")
                        // 对于Google域名，可以选择忽略SSL错误以提高兼容性
                        if (error?.url?.contains("google.com") == true) {
                            handler?.proceed()
                        } else {
                            handler?.cancel()
                        }
                    }
                }
                
                // 设置 WebChromeClient 以处理权限请求和标题变化
                webChromeClient = object : WebChromeClient() {
                    override fun onPermissionRequest(request: PermissionRequest) {
                        request.grant(request.resources)
                    }
                    
                    override fun onReceivedTitle(view: WebView?, title: String?) {
                        super.onReceivedTitle(view, title)
                        // 当页面标题变化时，同步更新菜单栏
                        title?.let { 
                            if (it.isNotEmpty()) {
                                onTitleChanged(it)
                            }
                        }
                    }
                }
                
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    // 设置缓存模式为默认缓存
                    cacheMode = WebSettings.LOAD_DEFAULT
                    // 设置UA为Chrome移动端，提高兼容性
                    userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Mobile Safari/537.36"
                    // 启用 JavaScript 接口
                    javaScriptCanOpenWindowsAutomatically = true
                    // 启用媒体播放
                    mediaPlaybackRequiresUserGesture = false
                    // 优化触摸滚动
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    
                    // 放宽安全相关设置，提高Google登录兼容性
                    allowFileAccess = true
                    allowContentAccess = true
                    // 注意：这些设置已在较新的Android版本中弃用，但为了兼容性保留
                    // 在API 29+中，这些设置由系统自动管理
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q) {
                        @Suppress("DEPRECATION")
                        allowFileAccessFromFileURLs = true
                        @Suppress("DEPRECATION")
                        allowUniversalAccessFromFileURLs = true
                    }
                    
                    // 启用现代Web特性
                    setGeolocationEnabled(true)
                    
                    // 设置混合内容处理为允许，改善Google登录兼容性
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    
                    // 启用DOM存储
                    domStorageEnabled = true
                    databaseEnabled = true
                    
                    // 启用更多Web特性以提高兼容性
                    // 注意：setAppCacheEnabled 和 setAppCachePath 已在 API 33 中移除
                    // 缓存现在由系统自动管理
                    
                    // 启用表单数据保存（在较新的Android版本中已弃用，但为了兼容性保留）
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
                        @Suppress("DEPRECATION")
                        saveFormData = true
                        @Suppress("DEPRECATION")
                        savePassword = true
                    }
                }
                applyWebViewDarkTheme(settings, darkTheme)
                setBackgroundColor(webViewBackground)
                
                // 启用第三方Cookie（使用新的API）
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                }

                // 优化触摸滚动
                isVerticalScrollBarEnabled = true
                isHorizontalScrollBarEnabled = true
                overScrollMode = android.view.View.OVER_SCROLL_IF_CONTENT_SCROLLS

                val swipeSlop = 80f * ctx.resources.displayMetrics.density
                var trackingSwipe = false
                var swipeIsPinch = false
                var gestureMode = 0
                var startX0 = 0f
                var startY0 = 0f
                var startX1 = 0f
                var startY1 = 0f
                var startSpan = 1f
                setOnTouchListener { _, event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_POINTER_DOWN -> {
                            if (event.pointerCount == 2) {
                                currentOnTwoFingerTouch()
                                startX0 = event.getX(0)
                                startY0 = event.getY(0)
                                startX1 = event.getX(1)
                                startY1 = event.getY(1)
                                startSpan = hypot(startX1 - startX0, startY1 - startY0).coerceAtLeast(1f)
                                trackingSwipe = true
                                swipeIsPinch = false
                                gestureMode = 0
                            } else {
                                trackingSwipe = false
                                swipeIsPinch = true
                            }
                        }
                        MotionEvent.ACTION_MOVE -> {
                            if (trackingSwipe && !swipeIsPinch && event.pointerCount == 2 && gestureMode != 2 && gestureMode != 3) {
                                val x0 = event.getX(0)
                                val y0 = event.getY(0)
                                val x1 = event.getX(1)
                                val y1 = event.getY(1)
                                val span = hypot(x1 - x0, y1 - y0)
                                val dx = (x0 + x1) / 2f - (startX0 + startX1) / 2f
                                val dy = (y0 + y1) / 2f - (startY0 + startY1) / 2f
                                val ratio = span / startSpan
                                val spanShrink = startSpan - span
                                if (gestureMode == 0 && ratio <= 0.75f && spanShrink > hypot(dx, dy)) {
                                    gestureMode = 3
                                    currentOnPinchIn()
                                } else if (gestureMode == 0 && ratio > 1.15f) {
                                    swipeIsPinch = true
                                } else if (gestureMode == 0 && ratio >= 0.85f) {
                                    val horizontal = abs(dx) >= swipeSlop && abs(dx) > abs(dy) * 1.5f
                                    val swipeDown = dy >= swipeSlop && dy > abs(dx) * 1.5f
                                    if (horizontal) {
                                        gestureMode = 1
                                        currentOnShowSwitcher()
                                    } else if (swipeDown) {
                                        gestureMode = 2
                                        currentOnTwoFingerSwipeDown()
                                    }
                                }
                            }
                        }
                    }
                    when (event.actionMasked) {
                        MotionEvent.ACTION_POINTER_UP -> {
                            if (event.pointerCount <= 2) {
                                trackingSwipe = false
                                swipeIsPinch = false
                                gestureMode = 0
                            }
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            trackingSwipe = false
                            swipeIsPinch = false
                            gestureMode = 0
                        }
                    }
                    val blockPage = !currentTouchEnabled
                    gestureMode != 0 || blockPage
                }
                
                tag = url
                loadUrl(url)
                
                // 保存WebView引用
                webView = this
            }
        },
        update = { webViewInstance ->
            val passThrough = webViewInstance as PassThroughWebView
            applyWebViewDarkTheme(webViewInstance.settings, darkTheme)
            webViewInstance.setBackgroundColor(webViewBackground)
            val themeChanged = passThrough.appliedDarkTheme != null &&
                passThrough.appliedDarkTheme != darkTheme
            passThrough.appliedDarkTheme = darkTheme
            if (webViewInstance.tag != url) {
                webViewInstance.tag = url
                webViewInstance.loadUrl(url)
            } else if (themeChanged) {
                webViewInstance.reload()
            }
            webViewInstance.visibility = if (active) android.view.View.VISIBLE else android.view.View.INVISIBLE
            passThrough.touchEnabled = currentTouchEnabled
            webView = webViewInstance
        },
        onRelease = { it.destroy() },
        modifier = modifier
    )
}

private class PassThroughWebView(context: Context) : WebView(context) {
    var touchEnabled: Boolean = true
    var appliedDarkTheme: Boolean? = null

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (!touchEnabled) return false
        return super.dispatchTouchEvent(ev)
    }
}
