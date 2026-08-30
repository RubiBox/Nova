package com.novabrowser.app.browser

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.novabrowser.app.data.UserScript

class BrowserWebViewClient(
    private val adBlockManager: AdBlockManager,
    private val isAdBlockEnabled: () -> Boolean,
    private val getUserScripts: () -> List<UserScript>,
    private val onPageStarted: (String) -> Unit,
    private val onPageFinished: (String, String) -> Unit,
    private val onProgressReset: () -> Unit
) : WebViewClient() {

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onProgressReset()
        onPageStarted(url)
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        UserScriptManager.injectAll(view, url, getUserScripts())
        onPageFinished(url, view.title ?: url)
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        if (isAdBlockEnabled() && request.url != null) {
            val urlStr = request.url.toString()
            if (adBlockManager.shouldBlock(urlStr)) {
                return adBlockManager.blockedResponse()
            }
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        // Let the WebView handle standard http/https navigation itself.
        val scheme = request.url.scheme
        if (scheme == "http" || scheme == "https") return false

        // Non-web schemes (mailto:, tel:, intent:, market:, etc.) — hand off to the OS.
        return try {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, request.url)
            view.context.startActivity(intent)
            true
        } catch (e: Exception) {
            true // swallow unsupported schemes rather than crashing
        }
    }
}
