package com.novabrowser.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BottomToolbar(
    canGoBack: Boolean,
    canGoForward: Boolean,
    tabCount: Int,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onHome: () -> Unit,
    onNewTab: () -> Unit,
    onShowTabs: () -> Unit,
    onShowMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outline)
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .navigationBarsPaddingCompat(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, enabled = canGoBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        IconButton(onClick = onForward, enabled = canGoForward) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Forward")
        }
        IconButton(onClick = onHome) {
            Icon(Icons.Filled.Home, contentDescription = "Home")
        }
        IconButton(onClick = onNewTab) {
            Icon(Icons.Filled.Add, contentDescription = "New tab")
        }
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(width = 1.5.dp, color = MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(8.dp))
                .background(androidx.compose.ui.graphics.Color.Transparent)
                .clickableCompat(onShowTabs),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tabCount.coerceAtMost(99).toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(onClick = onShowMenu) {
            Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
        }
    }
}

@Composable
private fun Modifier.navigationBarsPaddingCompat(): Modifier = this.then(
    androidx.compose.foundation.layout.navigationBarsPadding()
)

private fun Modifier.clickableCompat(onClick: () -> Unit): Modifier = this.then(
    androidx.compose.foundation.clickable(onClick = onClick)
)
