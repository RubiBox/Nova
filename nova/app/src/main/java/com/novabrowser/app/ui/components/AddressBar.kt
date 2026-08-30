package com.novabrowser.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    progress: Int,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onReload: () -> Unit,
    isIncognito: Boolean,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(targetValue = progress / 100f, label = "progress")

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (text.startsWith("https://")) Icons.Filled.Lock else Icons.Filled.Search,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = if (isIncognito) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                BasicTextFieldAddress(
                    text = text,
                    onTextChange = onTextChange,
                    onSubmit = onSubmit,
                    onFocusChange = { isFocused = it },
                    modifier = Modifier.weight(1f)
                )
                if (progress in 1..99) {
                    Text("${progress}%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Reload",
                        modifier = Modifier
                            .size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onToggleBookmark, modifier = Modifier.size(38.dp)) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (progress in 1..99) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BasicTextFieldAddress(
    text: String,
    onTextChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = text,
        onValueChange = onTextChange,
        modifier = modifier
            .onFocusChangedCompat(onFocusChange),
        singleLine = true,
        placeholder = { Text("Search or type URL", fontSize = 14.sp) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
        keyboardActions = KeyboardActions(onGo = { onSubmit(text) })
    )
}

// Small extension to keep imports tidy without pulling in extra focus-event boilerplate above.
private fun Modifier.onFocusChangedCompat(onFocusChange: (Boolean) -> Unit): Modifier =
    this.then(
        androidx.compose.ui.focus.onFocusChanged { onFocusChange(it.isFocused) }
    )
