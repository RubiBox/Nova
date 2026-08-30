package com.novabrowser.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val folder: String = "General",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "history")
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val visitedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DownloadRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val url: String,
    val localUri: String,
    val mimeType: String,
    val sizeBytes: Long = 0,
    val downloadedAt: Long = System.currentTimeMillis(),
    val status: String = "COMPLETE" // PENDING, RUNNING, COMPLETE, FAILED
)

/**
 * Represents a "userscript" — Nova Browser's substitute for Chrome extensions.
 * Real .crx Chrome extensions cannot run on Android WebView (no browser can do this,
 * including Chrome for Android itself). Userscripts fill the same role for
 * JS/CSS-injection-style extensions (ad blockers, page tweakers, dark-mode injectors,
 * auto-fillers, etc.) — similar to Tampermonkey/Violentmonkey on desktop.
 */
@Entity(tableName = "userscripts")
data class UserScript(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val matchPattern: String = "*", // simple glob, e.g. *://*.example.com/*
    val jsCode: String,
    val cssCode: String = "",
    val enabled: Boolean = true,
    val runAt: String = "DOCUMENT_END", // DOCUMENT_START, DOCUMENT_END, DOCUMENT_IDLE
    val source: String = "manual", // "manual" or "imported"
    val createdAt: Long = System.currentTimeMillis()
)
