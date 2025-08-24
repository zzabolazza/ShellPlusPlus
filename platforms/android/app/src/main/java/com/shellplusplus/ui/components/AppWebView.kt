package com.shellplusplus.ui.components

import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.*
import android.net.http.SslError
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Modifier
import android.util.Log
import androidx.activity.compose.BackHandler

@Composable
fun AppWebView(
    url: String, 
    modifier: Modifier = Modifier,
    onTitleChanged: (String) -> Unit = {}
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    
    // 处理返回键
    BackHandler {
        webView?.let { view ->
            if (view.canGoBack()) {
                view.goBack()
            }
        }
    }
    
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
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
                
                // 启用第三方Cookie（使用新的API）
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                }

                // 优化触摸滚动
                isVerticalScrollBarEnabled = true
                isHorizontalScrollBarEnabled = true
                overScrollMode = android.view.View.OVER_SCROLL_IF_CONTENT_SCROLLS
                
                // 加载URL
                loadUrl(url)
                
                // 保存WebView引用
                webView = this
            }
        },
        update = { webViewInstance ->
            // 每次更新都重新加载URL
            webViewInstance.loadUrl(url)
            // 更新WebView引用
            webView = webViewInstance
        },
        modifier = modifier
    )
}
