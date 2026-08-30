package com.novabrowser.app.browser

import android.content.Context
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream
import java.net.URI

/**
 * Lightweight host-based ad/tracker blocker.
 * Loads a plaintext list of ad/tracker domains from assets/adblock_hosts.txt
 * (one host per line) and rejects matching requests inside shouldInterceptRequest.
 *
 * This is the same technique used by uBlock/Brave's basic host-blocking mode —
 * not a full filter-list (ABP syntax) engine, but effective and very cheap at runtime.
 */
class AdBlockManager private constructor(private val blockedHosts: Set<String>) {

    fun shouldBlock(url: String): Boolean {
        return try {
            val host = URI(url).host?.lowercase() ?: return false
            blockedHosts.any { blocked -> host == blocked || host.endsWith(".$blocked") }
        } catch (e: Exception) {
            false
        }
    }

    fun blockedResponse(): WebResourceResponse =
        WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(ByteArray(0)))

    companion object {
        @Volatile private var INSTANCE: AdBlockManager? = null

        fun getInstance(context: Context): AdBlockManager =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context).also { INSTANCE = it }
            }

        private fun build(context: Context): AdBlockManager {
            val hosts = mutableSetOf<String>()
            try {
                context.assets.open("adblock_hosts.txt").bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        val trimmed = line.trim()
                        if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                            hosts.add(trimmed.lowercase())
                        }
                    }
                }
            } catch (e: Exception) {
                // Missing asset shouldn't crash the browser; ad-block simply becomes a no-op.
            }
            return AdBlockManager(hosts)
        }
    }
}
