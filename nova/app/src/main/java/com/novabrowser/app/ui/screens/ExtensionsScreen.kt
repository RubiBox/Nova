package com.novabrowser.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.novabrowser.app.data.UserScript

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionsScreen(
    scripts: List<UserScript>,
    onToggle: (UserScript) -> Unit,
    onDelete: (UserScript) -> Unit,
    onSave: (UserScript) -> Unit,
    onBack: () -> Unit
) {
    var showEditor by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Extensions") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { showEditor = true }, icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Add script") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Nova uses userscripts (JS/CSS injection) in place of Chrome extensions — " +
                        "real .crx extensions can't run in any Android browser, Chrome included. " +
                        "This covers ad blockers, page tweaks, auto-fillers, and dark-mode injectors.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (scripts.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.Extension, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("No extensions installed", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                LazyColumn {
                    items(scripts, key = { it.id }) { script ->
                        ListItem(
                            headlineContent = { Text(script.name, fontWeight = FontWeight.Medium) },
                            supportingContent = { Text("${script.matchPattern}  •  ${script.description.ifBlank { "No description" }}") },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(checked = script.enabled, onCheckedChange = { onToggle(script) })
                                    IconButton(onClick = { onDelete(script) }) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showEditor) {
        UserScriptEditorDialog(
            onDismiss = { showEditor = false },
            onSave = { script -> onSave(script); showEditor = false }
        )
    }
}

@Composable
private fun UserScriptEditorDialog(onDismiss: () -> Unit, onSave: (UserScript) -> Unit) {
    var name by remember { mutableStateOf("") }
    var pattern by remember { mutableStateOf("*") }
    var js by remember { mutableStateOf("") }
    var css by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New extension (userscript)") },
        text = {
            Column(
                modifier = Modifier.verticalScrollCompat(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = pattern, onValueChange = { pattern = it }, label = { Text("Match pattern (e.g. *://*.example.com/*)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = css, onValueChange = { css = it }, label = { Text("CSS (optional)") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = js, onValueChange = { js = it }, label = { Text("JavaScript") }, minLines = 4, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onSave(UserScript(name = name, matchPattern = pattern.ifBlank { "*" }, jsCode = js, cssCode = css))
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun Modifier.verticalScrollCompat(): Modifier = this.then(
    androidx.compose.foundation.verticalScroll(androidx.compose.foundation.rememberScrollState())
)
