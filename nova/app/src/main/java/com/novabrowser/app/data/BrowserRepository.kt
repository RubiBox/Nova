package com.novabrowser.app.data

import android.content.Context

class BrowserRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)

    val bookmarks get() = db.bookmarkDao()
    val history get() = db.historyDao()
    val downloads get() = db.downloadDao()
    val userScripts get() = db.userScriptDao()
}
