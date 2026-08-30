package com.novabrowser.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.novabrowser.app.model.Tab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabSwitcherScreen(
    tabs: List<Tab>,
    activeTabId: String?,
    onSelectTab: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onNewTab: () -> Unit,
    onNewIncognitoTab: () -> Unit,
    onDismiss: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${tabs.size} Tabs") },
                navigationIcon = {
                    TextButton(onClick = onDismiss) { Text("Done") }
                },
                actions = {
                    TextButton(onClick = onNewIncognitoTab) { Text("Incognito") }
                    IconButton(onClick = onNewTab) { Icon(Icons.Filled.Add, contentDescription = "New tab") }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(tabs, key = { it.id }) { tab ->
                TabCard(
                    tab = tab,
                    isActive = tab.id == activeTabId,
                    onClick = { onSelectTab(tab.id) },
                    onClose = { onCloseTab(tab.id) }
                )
            }
        }
    }
}

@Composable
private fun TabCard(
    tab: Tab,
    isActive: Boolean,
    onClick: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .then(
                if (isActive) Modifier else Modifier
            )
            .clickable(onClick = onClick)
            .padding(10.dp)
            .height(150.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (tab.isIncognito) "🕶 Incognito" else tab.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onClose, modifier = Modifier.size(22.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Close tab", modifier = Modifier.size(14.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tab.url.take(40),
                fontSize = 10.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(6.dp)
            )
        }
    }
}
