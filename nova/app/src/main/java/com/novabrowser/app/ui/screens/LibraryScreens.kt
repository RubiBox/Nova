package com.novabrowser.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.novabrowser.app.data.Bookmark
import com.novabrowser.app.data.DownloadRecord
import com.novabrowser.app.data.HistoryEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    bookmarks: List<Bookmark>,
    onOpen: (String) -> Unit,
    onDelete: (Bookmark) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Bookmarks") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
        )
    }) { padding ->
        if (bookmarks.isEmpty()) {
            EmptyState("No bookmarks yet", "Tap the bookmark icon in the address bar to save pages.", Modifier.padding(padding))
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(bookmarks, key = { it.id }) { bookmark ->
                    ListItem(
                        headlineContent = { Text(bookmark.title.ifBlank { bookmark.url }, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        supportingContent = { Text(bookmark.url, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        trailingContent = {
                            IconButton(onClick = { onDelete(bookmark) }) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
                        },
                        modifier = Modifier.clickableRow { onOpen(bookmark.url) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    history: List<HistoryEntry>,
    onOpen: (String) -> Unit,
    onClearAll: () -> Unit,
    onBack: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("History") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
            actions = { IconButton(onClick = onClearAll) { Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear history") } }
        )
    }) { padding ->
        if (history.isEmpty()) {
            EmptyState("No history yet", "Pages you visit will show up here.", Modifier.padding(padding))
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(history, key = { it.id }) { entry ->
                    ListItem(
                        headlineContent = { Text(entry.title.ifBlank { entry.url }, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        supportingContent = { Text("${entry.url}  •  ${dateFormat.format(Date(entry.visitedAt))}", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        modifier = Modifier.clickableRow { onOpen(entry.url) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    downloads: List<DownloadRecord>,
    onBack: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Downloads") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
        )
    }) { padding ->
        if (downloads.isEmpty()) {
            EmptyState("No downloads yet", "Files you download will appear here.", Modifier.padding(padding))
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(downloads, key = { it.id }) { item ->
                    ListItem(
                        headlineContent = { Text(item.fileName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        supportingContent = { Text("${item.status}  •  ${dateFormat.format(Date(item.downloadedAt))}") }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

private fun Modifier.clickableRow(onClick: () -> Unit): Modifier =
    this.then(androidx.compose.foundation.clickable(onClick = onClick))
