package com.shellplusplus.ui.components

import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Modifier
import android.util.Log

@Composable
fun AppWebView(
    url: String, 
    modifier: Modifier = Modifier
) {
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
                    // 设置缓存模式
                    cacheMode = WebSettings.LOAD_NO_CACHE
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
            }
        },
        update = { webView ->
            webView.loadUrl(url)
        },
        modifier = modifier
    )
}
