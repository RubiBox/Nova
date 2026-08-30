package com.novabrowser.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserMenuSheet(
    onDismiss: () -> Unit,
    onHistory: () -> Unit,
    onDownloads: () -> Unit,
    onExtensions: () -> Unit,
    onSettings: () -> Unit,
    onShare: () -> Unit,
    onNewIncognitoTab: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(bottom = 24.dp)) {
            MenuRow(Icons.Filled.VisibilityOff, "New incognito tab") { onNewIncognitoTab(); onDismiss() }
            MenuRow(Icons.Filled.History, "History") { onHistory(); onDismiss() }
            MenuRow(Icons.Filled.Download, "Downloads") { onDownloads(); onDismiss() }
            MenuRow(Icons.Filled.Extension, "Extensions") { onExtensions(); onDismiss() }
            MenuRow(Icons.Filled.Share, "Share page") { onShare(); onDismiss() }
            MenuRow(Icons.Filled.Settings, "Settings") { onSettings(); onDismiss() }
        }
    }
}

@Composable
private fun MenuRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(icon, contentDescription = null) },
        modifier = Modifier.clickableCompat(onClick)
    )
}

private fun Modifier.clickableCompat(onClick: () -> Unit): Modifier =
    this.then(androidx.compose.foundation.clickable(onClick = onClick))
