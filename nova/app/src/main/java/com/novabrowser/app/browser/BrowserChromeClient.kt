package com.novabrowser.app.browser

import android.graphics.Bitmap
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

class BrowserChromeClient(
    private val blockPopups: () -> Boolean,
    private val onProgressChanged: (Int) -> Unit,
    private val onTitleChanged: (String) -> Unit,
    private val onFaviconChanged: (Bitmap?) -> Unit,
    private val onFileChooser: (ValueCallback<Array<Uri>>) -> Boolean,
    private val onNewWindowRequested: (WebView) -> Unit
) : WebChromeClient() {

    override fun onProgressChanged(view: WebView, newProgress: Int) {
        onProgressChanged(newProgress)
    }

    override fun onReceivedTitle(view: WebView, title: String?) {
        onTitleChanged(title ?: view.url ?: "New Tab")
    }

    override fun onReceivedIcon(view: WebView, icon: Bitmap?) {
        onFaviconChanged(icon)
    }

    override fun onShowFileChooser(
        webView: WebView,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: FileChooserParams
    ): Boolean = onFileChooser(filePathCallback)

    override fun onCreateWindow(
        view: WebView,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: android.os.Message
    ): Boolean {
        if (!isUserGesture && blockPopups()) return false

        // Create a hidden transport WebView for the popup and hand it to the host
        // (host decides whether to open it as a new tab).
        val newWebView = WebView(view.context)
        val transport = resultMsg.obj as WebView.WebViewTransport
        transport.webView = newWebView
        resultMsg.sendToTarget()
        onNewWindowRequested(newWebView)
        return true
    }
}
