package com.shellplusplus.ui.components

import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.*
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Modifier
import android.util.Log
import androidx.activity.compose.BackHandler

@Composable
fun AppWebView(
    url: String, 
    modifier: Modifier = Modifier
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
                        if (error.errorCode == ERROR_FILE_NOT_FOUND) {
                            view.reload()
                        } else {
                            super.onReceivedError(view, request, error)
                        }
                    }
                    
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.d("AppWebView", "页面加载完成: $url")
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
                    // 设置UA为Firefox移动端
                    userAgentString = "Mozilla/5.0 (Android 10; Mobile; rv:109.0) Gecko/109.0 Firefox/119.0"
                    // 启用 JavaScript 接口
                    javaScriptCanOpenWindowsAutomatically = true
                    // 启用媒体播放
                    mediaPlaybackRequiresUserGesture = false
                    // 优化触摸滚动
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    
                    // 添加安全相关设置
                    allowFileAccess = false
                    allowContentAccess = false
                    allowFileAccessFromFileURLs = false
                    allowUniversalAccessFromFileURLs = false
                    
                    // 启用现代Web特性
                    setGeolocationEnabled(true)
                    
                    // 设置混合内容处理为允许，改善Google登录兼容性
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    
                    // 启用DOM存储
                    domStorageEnabled = true
                    databaseEnabled = true
                }

                // 设置 WebChromeClient 以处理权限请求
                webChromeClient = object : WebChromeClient() {
                    override fun onPermissionRequest(request: PermissionRequest) {
                        request.grant(request.resources)
                    }
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
