package com.novabrowser.app.model

import android.graphics.Bitmap
import android.webkit.WebView
import java.util.UUID

/**
 * One browser tab. Holds its own WebView instance so switching tabs is instant
 * (no reload) — the same approach real browsers use.
 */
class Tab(
    val id: String = UUID.randomUUID().toString(),
    val isIncognito: Boolean = false,
    var webView: WebView? = null
) {
    var title: String = "New Tab"
    var url: String = "about:blank"
    var favicon: Bitmap? = null
    var progress: Int = 0
    var canGoBack: Boolean = false
    var canGoForward: Boolean = false
    var isLoading: Boolean = false
}
