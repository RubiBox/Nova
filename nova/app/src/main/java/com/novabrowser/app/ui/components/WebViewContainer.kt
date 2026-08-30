package com.novabrowser.app.ui.components

import android.webkit.WebView
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.novabrowser.app.model.Tab
import com.novabrowser.app.viewmodel.BrowserViewModel

@Composable
fun WebViewContainer(
    tab: Tab,
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            FrameLayout(context).apply {
                val webView = tab.webView ?: WebView(context).also { tab.webView = it }
                (webView.parent as? FrameLayout)?.removeView(webView)
                addView(webView, FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                ))
                if (tab.webView?.url == null || tab.webView?.url == "about:blank") {
                    viewModel.configureWebView(webView, tab)
                    webView.loadUrl(tab.url)
                }
            }
        },
        update = { frameLayout ->
            val webView = tab.webView ?: return@AndroidView
            if (webView.parent != frameLayout) {
                (webView.parent as? FrameLayout)?.removeView(webView)
                frameLayout.removeAllViews()
                frameLayout.addView(webView, FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                ))
            }
        }
    )
}
