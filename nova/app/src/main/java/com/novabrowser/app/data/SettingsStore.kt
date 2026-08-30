package com.novabrowser.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "nova_settings")

enum class SearchEngine(val label: String, val urlTemplate: String) {
    GOOGLE("Google", "https://www.google.com/search?q=%s"),
    DUCKDUCKGO("DuckDuckGo", "https://duckduckgo.com/?q=%s"),
    BING("Bing", "https://www.bing.com/search?q=%s"),
    BRAVE("Brave Search", "https://search.brave.com/search?q=%s")
}

class SettingsStore(private val context: Context) {
    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val AD_BLOCK = booleanPreferencesKey("ad_block")
        val FORCE_DARK_PAGES = booleanPreferencesKey("force_dark_pages")
        val SEARCH_ENGINE = stringPreferencesKey("search_engine")
        val USERSCRIPTS_ENABLED = booleanPreferencesKey("userscripts_enabled")
        val JS_ENABLED = booleanPreferencesKey("js_enabled")
        val BLOCK_POPUPS = booleanPreferencesKey("block_popups")
        val DO_NOT_TRACK = booleanPreferencesKey("do_not_track")
    }

    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[Keys.DARK_MODE] ?: true }
    val adBlockEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.AD_BLOCK] ?: true }
    val forceDarkPages: Flow<Boolean> = context.dataStore.data.map { it[Keys.FORCE_DARK_PAGES] ?: false }
    val searchEngine: Flow<String> = context.dataStore.data.map { it[Keys.SEARCH_ENGINE] ?: SearchEngine.GOOGLE.name }
    val userScriptsEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.USERSCRIPTS_ENABLED] ?: true }
    val jsEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.JS_ENABLED] ?: true }
    val blockPopups: Flow<Boolean> = context.dataStore.data.map { it[Keys.BLOCK_POPUPS] ?: true }
    val doNotTrack: Flow<Boolean> = context.dataStore.data.map { it[Keys.DO_NOT_TRACK] ?: true }

    suspend fun setDarkMode(value: Boolean) = context.dataStore.edit { it[Keys.DARK_MODE] = value }
    suspend fun setAdBlock(value: Boolean) = context.dataStore.edit { it[Keys.AD_BLOCK] = value }
    suspend fun setForceDarkPages(value: Boolean) = context.dataStore.edit { it[Keys.FORCE_DARK_PAGES] = value }
    suspend fun setSearchEngine(value: String) = context.dataStore.edit { it[Keys.SEARCH_ENGINE] = value }
    suspend fun setUserScriptsEnabled(value: Boolean) = context.dataStore.edit { it[Keys.USERSCRIPTS_ENABLED] = value }
    suspend fun setJsEnabled(value: Boolean) = context.dataStore.edit { it[Keys.JS_ENABLED] = value }
    suspend fun setBlockPopups(value: Boolean) = context.dataStore.edit { it[Keys.BLOCK_POPUPS] = value }
    suspend fun setDoNotTrack(value: Boolean) = context.dataStore.edit { it[Keys.DO_NOT_TRACK] = value }
}
