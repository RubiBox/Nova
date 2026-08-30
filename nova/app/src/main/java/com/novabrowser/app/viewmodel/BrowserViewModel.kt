package com.novabrowser.app.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Patterns
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.novabrowser.app.browser.AdBlockManager
import com.novabrowser.app.browser.BrowserChromeClient
import com.novabrowser.app.browser.BrowserDownloadManager
import com.novabrowser.app.browser.BrowserWebViewClient
import com.novabrowser.app.data.Bookmark
import com.novabrowser.app.data.BrowserRepository
import com.novabrowser.app.data.HistoryEntry
import com.novabrowser.app.data.SearchEngine
import com.novabrowser.app.data.SettingsStore
import com.novabrowser.app.data.UserScript
import com.novabrowser.app.model.Tab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.net.URLEncoder

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BrowserRepository(application)
    val settings = SettingsStore(application)
    private val adBlockManager = AdBlockManager.getInstance(application)
    val downloadManager = BrowserDownloadManager(application, repository)

    private val _tabs = MutableStateFlow<List<Tab>>(emptyList())
    val tabs: StateFlow<List<Tab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<String?>(null)
    val activeTabId: StateFlow<String?> = _activeTabId.asStateFlow()

    private val _addressBarText = MutableStateFlow("")
    val addressBarText: StateFlow<String> = _addressBarText.asStateFlow()

    private val _tabsRevision = MutableStateFlow(0) // bump to force UI recomposition on mutation
    val tabsRevision: StateFlow<Int> = _tabsRevision.asStateFlow()

    val bookmarksFlow = repository.bookmarks.observeAll()
    val historyFlow = repository.history.observeAll()
    val downloadsFlow = repository.downloads.observeAll()
    val userScriptsFlow = repository.userScripts.observeAll()

    val activeTab: Tab? get() = _tabs.value.find { it.id == _activeTabId.value }

    fun createTab(url: String = "https://www.google.com", incognito: Boolean = false, switchToIt: Boolean = true): Tab {
        val tab = Tab(isIncognito = incognito)
        _tabs.value = _tabs.value + tab
        if (switchToIt) _activeTabId.value = tab.id
        loadUrlInternal(tab, url)
        bump()
        return tab
    }

    fun closeTab(tabId: String) {
        val target = _tabs.value.find { it.id == tabId } ?: return
        target.webView?.destroy()
        val remaining = _tabs.value.filterNot { it.id == tabId }
        _tabs.value = remaining
        if (_activeTabId.value == tabId) {
            _activeTabId.value = remaining.lastOrNull()?.id
        }
        if (remaining.isEmpty()) createTab()
        bump()
    }

    fun switchTab(tabId: String) {
        _activeTabId.value = tabId
        _addressBarText.value = activeTab?.url.orEmpty()
        bump()
    }

    fun configureWebView(webView: WebView, tab: Tab) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            mediaPlaybackRequiresUserGesture = false
            cacheMode = WebSettings.LOAD_DEFAULT
            userAgentString = userAgentString.replace("; wv", "") // present as a standard mobile browser, not "WebView"
        }

        CookieManager.getInstance().apply {
            setAcceptCookie(!tab.isIncognito)
            setAcceptThirdPartyCookies(webView, !tab.isIncognito)
        }

        webView.webViewClient = BrowserWebViewClient(
            adBlockManager = adBlockManager,
            isAdBlockEnabled = { runBlockingFirst(settings.adBlockEnabled, true) },
            getUserScripts = { runBlockingListFirst() },
            onPageStarted = { url ->
                tab.isLoading = true
                tab.url = url
                if (activeTabId.value == tab.id) _addressBarText.value = url
                bump()
            },
            onPageFinished = { url, title ->
                tab.isLoading = false
                tab.url = url
                tab.title = title
                bump()
                if (!tab.isIncognito) recordHistory(title, url)
            },
            onProgressReset = { tab.progress = 0; bump() }
        )

        webView.webChromeClient = BrowserChromeClient(
            blockPopups = { runBlockingFirst(settings.blockPopups, true) },
            onProgressChanged = { progress -> tab.progress = progress; bump() },
            onTitleChanged = { title -> tab.title = title; bump() },
            onFaviconChanged = { icon: Bitmap? -> tab.favicon = icon; bump() },
            onFileChooser = { false }, // hook up an ActivityResult launcher at the Activity level if needed
            onNewWindowRequested = { newWebView ->
                val newTab = Tab(isIncognito = tab.isIncognito, webView = newWebView)
                _tabs.value = _tabs.value + newTab
                _activeTabId.value = newTab.id
                configureWebView(newWebView, newTab)
                bump()
            }
        )

        tab.webView = webView
    }

    fun navigate(input: String) {
        val tab = activeTab ?: createTab()
        val resolved = resolveInput(input)
        loadUrlInternal(tab, resolved)
    }

    private fun loadUrlInternal(tab: Tab, url: String) {
        tab.url = url
        tab.webView?.loadUrl(url) ?: run {
            // WebView not yet attached (first load) — Tab.url will be picked up by the
            // Composable's AndroidView factory on creation.
        }
        _addressBarText.value = url
    }

    fun resolveInput(input: String): String {
        val trimmed = input.trim()
        val looksLikeUrl = Patterns.WEB_URL.matcher(trimmed).matches() &&
            !trimmed.contains(" ")
        return when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            looksLikeUrl -> "https://$trimmed"
            else -> {
                val engineName = runBlockingFirst(settings.searchEngine, SearchEngine.GOOGLE.name)
                val engine = SearchEngine.entries.find { it.name == engineName } ?: SearchEngine.GOOGLE
                engine.urlTemplate.replace("%s", URLEncoder.encode(trimmed, "UTF-8"))
            }
        }
    }

    fun goBack() { activeTab?.webView?.let { if (it.canGoBack()) it.goBack() } }
    fun goForward() { activeTab?.webView?.let { if (it.canGoForward()) it.goForward() } }
    fun reload() { activeTab?.webView?.reload() }
    fun stop() { activeTab?.webView?.stopLoading() }

    fun updateAddressBarText(text: String) { _addressBarText.value = text }

    // ---- Bookmarks ----
    fun toggleBookmark() {
        val tab = activeTab ?: return
        viewModelScope.launch {
            if (repository.bookmarks.isBookmarked(tab.url)) {
                repository.bookmarks.deleteByUrl(tab.url)
            } else {
                repository.bookmarks.insert(Bookmark(title = tab.title, url = tab.url))
            }
        }
    }

    fun deleteBookmark(bookmark: Bookmark) = viewModelScope.launch { repository.bookmarks.delete(bookmark) }

    // ---- History ----
    private fun recordHistory(title: String, url: String) {
        if (url == "about:blank") return
        viewModelScope.launch { repository.history.insert(HistoryEntry(title = title, url = url)) }
    }

    fun clearHistory() = viewModelScope.launch { repository.history.clearAll() }

    // ---- Incognito / privacy ----
    fun clearBrowsingData() {
        CookieManager.getInstance().removeAllCookies(null)
        WebStorage.getInstance().deleteAllData()
        viewModelScope.launch { repository.history.clearAll() }
    }

    // ---- Userscripts ("extensions") ----
    fun saveUserScript(script: UserScript) = viewModelScope.launch { repository.userScripts.insert(script) }
    fun deleteUserScript(script: UserScript) = viewModelScope.launch { repository.userScripts.delete(script) }
    fun toggleUserScript(script: UserScript) = viewModelScope.launch {
        repository.userScripts.update(script.copy(enabled = !script.enabled))
    }

    private var cachedScripts: List<UserScript> = emptyList()
    init {
        viewModelScope.launch {
            userScriptsFlow.collect { cachedScripts = it }
        }
    }
    private fun runBlockingListFirst(): List<UserScript> = cachedScripts

    // Small helper: settings Flows are collected reactively in Compose, but the
    // WebViewClient callbacks are plain Kotlin lambdas invoked from WebView's own
    // thread — so we keep a lightweight cached snapshot for those checks.
    private var cachedAdBlock = true
    private var cachedPopupBlock = true
    private var cachedSearchEngine = SearchEngine.GOOGLE.name
    init {
        viewModelScope.launch { settings.adBlockEnabled.collect { cachedAdBlock = it } }
        viewModelScope.launch { settings.blockPopups.collect { cachedPopupBlock = it } }
        viewModelScope.launch { settings.searchEngine.collect { cachedSearchEngine = it } }
    }
    private fun <T> runBlockingFirst(flow: kotlinx.coroutines.flow.Flow<T>, default: T): T = when (flow) {
        settings.adBlockEnabled -> cachedAdBlock as T
        settings.blockPopups -> cachedPopupBlock as T
        settings.searchEngine -> cachedSearchEngine as T
        else -> default
    }

    private fun bump() { _tabsRevision.value = _tabsRevision.value + 1 }
}
