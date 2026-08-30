package com.novabrowser.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.item
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.novabrowser.app.data.SearchEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    darkMode: Boolean,
    adBlockEnabled: Boolean,
    forceDarkPages: Boolean,
    blockPopups: Boolean,
    doNotTrack: Boolean,
    userScriptsEnabled: Boolean,
    searchEngine: String,
    onDarkModeChange: (Boolean) -> Unit,
    onAdBlockChange: (Boolean) -> Unit,
    onForceDarkPagesChange: (Boolean) -> Unit,
    onBlockPopupsChange: (Boolean) -> Unit,
    onDoNotTrackChange: (Boolean) -> Unit,
    onUserScriptsChange: (Boolean) -> Unit,
    onSearchEngineChange: (String) -> Unit,
    onClearBrowsingData: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
        )
    }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            item { SectionHeader("Appearance") }
            item { SwitchRow("Dark theme", "App interface theme", darkMode, onDarkModeChange) }
            item { SwitchRow("Force dark on pages", "Applies dark styling to bright websites", forceDarkPages, onForceDarkPagesChange) }

            item { SectionHeader("Privacy & Security") }
            item { SwitchRow("Block ads & trackers", "Blocks known ad/tracking domains", adBlockEnabled, onAdBlockChange) }
            item { SwitchRow("Block pop-ups", "Stops automatic pop-up windows", blockPopups, onBlockPopupsChange) }
            item { SwitchRow("Send Do Not Track", "Requests sites not track you", doNotTrack, onDoNotTrackChange) }
            item {
                ListItem(
                    headlineContent = { Text("Clear browsing data") },
                    supportingContent = { Text("Cookies, cache, and history") },
                    modifier = Modifier.clickableCompat(onClearBrowsingData)
                )
            }

            item { SectionHeader("Extensions") }
            item { SwitchRow("Enable userscripts", "Runs installed extensions on pages", userScriptsEnabled, onUserScriptsChange) }

            item { SectionHeader("Search Engine") }
            items_(SearchEngine.entries.toList()) { engine ->
                ListItem(
                    headlineContent = { Text(engine.label) },
                    trailingContent = {
                        RadioButton(selected = searchEngine == engine.name, onClick = { onSearchEngineChange(engine.name) })
                    },
                    modifier = Modifier.clickableCompat { onSearchEngineChange(engine.name) }
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 6.dp)
    )
}

@Composable
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onCheckedChange) }
    )
}

private fun Modifier.clickableCompat(onClick: () -> Unit): Modifier =
    this.then(androidx.compose.foundation.clickable(onClick = onClick))

// Small alias to avoid name clash with LazyColumn's own `items` when iterating a plain List.
private inline fun androidx.compose.foundation.lazy.LazyListScope.items_(
    list: List<SearchEngine>,
    crossinline content: @Composable (SearchEngine) -> Unit
) {
    items(list.size) { index -> content(list[index]) }
}
