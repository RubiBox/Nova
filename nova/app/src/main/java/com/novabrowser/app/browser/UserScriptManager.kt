package com.novabrowser.app.browser

import android.webkit.WebView
import com.novabrowser.app.data.UserScript

/**
 * Injects enabled userscripts into a page. This is Nova Browser's "extensions" system —
 * see Entities.kt for why real Chrome extensions can't run on Android WebView.
 *
 * Matching uses simple glob patterns, e.g.:
 *   "*"                        -> all sites
 *   "*://*.example.com/*"      -> example.com and subdomains
 *   "https://news.site/*"      -> a specific site
 */
object UserScriptManager {

    fun matches(pattern: String, url: String): Boolean {
        if (pattern == "*" || pattern.isBlank()) return true
        val regex = Regex(
            "^" + Regex.escape(pattern)
                .replace("\\*", ".*") + "$",
            RegexOption.IGNORE_CASE
        )
        return regex.matches(url)
    }

    fun injectAll(webView: WebView, url: String, scripts: List<UserScript>) {
        scripts.filter { it.enabled && matches(it.matchPattern, url) }
            .forEach { script -> inject(webView, script) }
    }

    private fun inject(webView: WebView, script: UserScript) {
        if (script.cssCode.isNotBlank()) {
            val cssEscaped = script.cssCode
                .replace("\\", "\\\\")
                .replace("`", "\\`")
                .replace("\n", " ")
            val cssJs = """
                (function() {
                    var style = document.createElement('style');
                    style.setAttribute('data-nova-userscript', '${script.id}');
                    style.innerHTML = `$cssEscaped`;
                    document.head.appendChild(style);
                })();
            """.trimIndent()
            webView.evaluateJavascript(cssJs, null)
        }
        if (script.jsCode.isNotBlank()) {
            // Wrap in an IIFE so scripts don't pollute / clash with each other.
            val wrapped = "(function(){ try { ${script.jsCode} } catch(e) { console.error('[NovaUserScript:${script.name}]', e); } })();"
            webView.evaluateJavascript(wrapped, null)
        }
    }
}
