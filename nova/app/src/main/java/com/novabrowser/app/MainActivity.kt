package com.novabrowser.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.novabrowser.app.ui.components.AddressBar
import com.novabrowser.app.ui.components.BottomToolbar
import com.novabrowser.app.ui.components.BrowserMenuSheet
import com.novabrowser.app.ui.components.WebViewContainer
import com.novabrowser.app.ui.screens.BookmarksScreen
import com.novabrowser.app.ui.screens.DownloadsScreen
import com.novabrowser.app.ui.screens.ExtensionsScreen
import com.novabrowser.app.ui.screens.HistoryScreen
import com.novabrowser.app.ui.screens.SettingsScreen
import com.novabrowser.app.ui.screens.TabSwitcherScreen
import com.novabrowser.app.ui.theme.NovaBrowserTheme
import com.novabrowser.app.viewmodel.BrowserViewModel
import kotlinx.coroutines.launch

private sealed class Screen {
    data object Browsing : Screen()
    data object TabSwitcher : Screen()
    data object Bookmarks : Screen()
    data object History : Screen()
    data object Downloads : Screen()
    data object Extensions : Screen()
    data object Settings : Screen()
}

class MainActivity : ComponentActivity() {
    private val viewModel: BrowserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (viewModel.tabs.value.isEmpty()) {
            viewModel.createTab("https://www.google.com")
        }
        setContent {
            val darkMode by viewModel.settings.darkMode.collectAsState(initial = true)
            NovaBrowserTheme(darkTheme = darkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BrowserRoot(viewModel)
                }
            }
        }
    }
}

@Composable
private fun BrowserRoot(viewModel: BrowserViewModel) {
    var screen by remember { mutableStateOf<Screen>(Screen.Browsing) }
    var showMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScopeCompat()

    val tabs by viewModel.tabs.collectAsState()
    val activeTabId by viewModel.activeTabId.collectAsState()
    val addressText by viewModel.addressBarText.collectAsState()
    viewModel.tabsRevision.collectAsState() // observed to force recomposition on tab mutation

    val bookmarks by viewModel.bookmarksFlow.collectAsState(initial = emptyList())
    val history by viewModel.historyFlow.collectAsState(initial = emptyList())
    val downloads by viewModel.downloadsFlow.collectAsState(initial = emptyList())
    val userScripts by viewModel.userScriptsFlow.collectAsState(initial = emptyList())

    val darkMode by viewModel.settings.darkMode.collectAsState(initial = true)
    val adBlock by viewModel.settings.adBlockEnabled.collectAsState(initial = true)
    val forceDarkPages by viewModel.settings.forceDarkPages.collectAsState(initial = false)
    val blockPopups by viewModel.settings.blockPopups.collectAsState(initial = true)
    val doNotTrack by viewModel.settings.doNotTrack.collectAsState(initial = true)
    val userScriptsEnabled by viewModel.settings.userScriptsEnabled.collectAsState(initial = true)
    val searchEngine by viewModel.settings.searchEngine.collectAsState(initial = "GOOGLE")

    val activeTab = tabs.find { it.id == activeTabId }
    val isBookmarked = bookmarks.any { it.url == activeTab?.url }

    when (screen) {
        is Screen.Browsing -> {
            Column(modifier = Modifier.fillMaxSize()) {
                AddressBar(
                    text = addressText,
                    onTextChange = viewModel::updateAddressBarText,
                    onSubmit = { viewModel.navigate(it) },
                    progress = activeTab?.progress ?: 0,
                    isBookmarked = isBookmarked,
                    onToggleBookmark = { viewModel.toggleBookmark() },
                    onReload = { viewModel.reload() },
                    isIncognito = activeTab?.isIncognito ?: false
                )
                if (activeTab != null) {
                    WebViewContainer(
                        tab = activeTab,
                        viewModel = viewModel,
                        modifier = Modifier.weight(1f)
                    )
                }
                BottomToolbar(
                    canGoBack = activeTab?.webView?.canGoBack() ?: false,
                    canGoForward = activeTab?.webView?.canGoForward() ?: false,
                    tabCount = tabs.size,
                    onBack = { viewModel.goBack() },
                    onForward = { viewModel.goForward() },
                    onHome = { viewModel.navigate("https://www.google.com") },
                    onNewTab = { viewModel.createTab("https://www.google.com") },
                    onShowTabs = { screen = Screen.TabSwitcher },
                    onShowMenu = { showMenu = true }
                )
            }

            if (showMenu) {
                BrowserMenuSheet(
                    onDismiss = { showMenu = false },
                    onHistory = { screen = Screen.History },
                    onDownloads = { screen = Screen.Downloads },
                    onExtensions = { screen = Screen.Extensions },
                    onSettings = { screen = Screen.Settings },
                    onShare = { /* wire to Android share sheet via Intent.ACTION_SEND if desired */ },
                    onNewIncognitoTab = { viewModel.createTab("https://www.google.com", incognito = true) }
                )
            }
        }

        is Screen.TabSwitcher -> TabSwitcherScreen(
            tabs = tabs,
            activeTabId = activeTabId,
            onSelectTab = { viewModel.switchTab(it); screen = Screen.Browsing },
            onCloseTab = { viewModel.closeTab(it) },
            onNewTab = { viewModel.createTab("https://www.google.com") },
            onNewIncognitoTab = { viewModel.createTab("https://www.google.com", incognito = true) },
            onDismiss = { screen = Screen.Browsing }
        )

        is Screen.Bookmarks -> BookmarksScreen(
            bookmarks = bookmarks,
            onOpen = { viewModel.navigate(it); screen = Screen.Browsing },
            onDelete = { viewModel.deleteBookmark(it) },
            onBack = { screen = Screen.Browsing }
        )

        is Screen.History -> HistoryScreen(
            history = history,
            onOpen = { viewModel.navigate(it); screen = Screen.Browsing },
            onClearAll = { viewModel.clearHistory() },
            onBack = { screen = Screen.Browsing }
        )

        is Screen.Downloads -> DownloadsScreen(
            downloads = downloads,
            onBack = { screen = Screen.Browsing }
        )

        is Screen.Extensions -> ExtensionsScreen(
            scripts = userScripts,
            onToggle = { viewModel.toggleUserScript(it) },
            onDelete = { viewModel.deleteUserScript(it) },
            onSave = { viewModel.saveUserScript(it) },
            onBack = { screen = Screen.Browsing }
        )

        is Screen.Settings -> SettingsScreen(
            darkMode = darkMode,
            adBlockEnabled = adBlock,
            forceDarkPages = forceDarkPages,
            blockPopups = blockPopups,
            doNotTrack = doNotTrack,
            userScriptsEnabled = userScriptsEnabled,
            searchEngine = searchEngine,
            onDarkModeChange = { scope.launch { viewModel.settings.setDarkMode(it) } },
            onAdBlockChange = { scope.launch { viewModel.settings.setAdBlock(it) } },
            onForceDarkPagesChange = { scope.launch { viewModel.settings.setForceDarkPages(it) } },
            onBlockPopupsChange = { scope.launch { viewModel.settings.setBlockPopups(it) } },
            onDoNotTrackChange = { scope.launch { viewModel.settings.setDoNotTrack(it) } },
            onUserScriptsChange = { scope.launch { viewModel.settings.setUserScriptsEnabled(it) } },
            onSearchEngineChange = { scope.launch { viewModel.settings.setSearchEngine(it) } },
            onClearBrowsingData = { viewModel.clearBrowsingData() },
            onBack = { screen = Screen.Browsing }
        )
    }
}

@Composable
private fun rememberCoroutineScopeCompat() = androidx.compose.runtime.rememberCoroutineScope()
