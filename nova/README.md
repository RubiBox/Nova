# Nova Browser — Phase 1 (Core Browser, no AI yet)

A full-featured Android browser built with Kotlin + Jetpack Compose + WebView (Chromium engine).

## What's included in this build

- **Multi-tab browsing** — each tab keeps its own live WebView, so switching tabs is instant (no reload).
- **Smart address bar** — combined search/URL bar, auto-detects URLs vs. search queries, live load progress.
- **Bookmarks** — save/remove from the address bar, dedicated Bookmarks screen (Room database).
- **History** — automatically recorded (except in Incognito), searchable, clearable.
- **Downloads** — files download via Android's system Download Manager (handles large files, resumption, notifications), with an in-app Downloads list.
- **Incognito mode** — no cookies, no history writes; "New incognito tab" from the menu.
- **Ad & tracker blocking** — host-based blocklist (`assets/adblock_hosts.txt`), toggle in Settings, easy to extend with more domains.
- **Dark mode** — app-wide theme toggle, plus optional "force dark" for bright websites.
- **Extensions (userscripts)** — JS/CSS injection system similar to Tampermonkey/Violentmonkey. See note below on why this replaces Chrome extensions.
- **Settings screen** — search engine choice (Google/DuckDuckGo/Bing/Brave), privacy toggles, popup blocking, Do Not Track.
- **Custom, non-templated UI** — a deliberate graphite/coral/teal visual identity (see `ui/theme/Color.kt`), not default Material purple.

## ⚠️ Important: About "Chrome extensions"

Real Chrome extensions (`.crx`, `manifest.json`, `chrome.*` APIs, background pages) **cannot run in any Android browser** — not Nova, not Kiwi, not Yandex, not even Chrome for Android itself. Extension APIs only exist in desktop Chromium. This is a platform limitation, not something specific to this app.

What Nova gives you instead: a **userscript engine** (Extensions screen → "Add script") that injects custom JS/CSS into matching pages. This covers most practical extension use cases — ad blockers, page tweaks, auto-fillers, dark-mode injectors, content scripts. It just can't run a extension's background service worker or use `chrome.*` APIs.

## Opening the project

1. Open Android Studio (Iguana/Koala or newer recommended).
2. **File → Open** → select the unzipped `NovaBrowser` folder.
3. Let Android Studio generate the Gradle wrapper automatically (first-open sync will prompt this), **or** run manually if you have Gradle installed:
   ```
   gradle wrapper --gradle-version 8.7
   ```
4. Sync Gradle, then Run ▶ on a device/emulator running **Android 8.0 (API 26)** or newer.

No API keys or network setup are needed for this phase — it's a self-contained browser.

## Project structure

```
app/src/main/java/com/novabrowser/app/
├── data/          Room entities, DAOs, database, DataStore settings
├── model/         Tab model
├── browser/       Ad blocker, userscript engine, WebView clients, download manager
├── viewmodel/      BrowserViewModel — central state
├── ui/theme/       Colors, typography, Compose theme
├── ui/components/  AddressBar, BottomToolbar, WebViewContainer, menu sheet
├── ui/screens/     TabSwitcher, Bookmarks, History, Downloads, Extensions, Settings
├── MainActivity.kt Compose entry point, screen routing
└── NovaBrowserApp.kt  Application class
```

## Known limitations in this phase (intentional — Phase 1 scope)

- File picker for `<input type="file">` uploads is stubbed (`onFileChooser` returns false) — easy to wire up with an `ActivityResultLauncher` if you need it before Phase 2.
- "Share page" menu item is a stub — hook to `Intent.ACTION_SEND`.
- The ad-block list is a small starter set — swap in a larger EasyList-derived host file anytime.
- No sync/cloud backup — everything is local (Room + DataStore).

## Next: Phase 2

AI assistant that can converse via Gemini/ChatGPT/Claude/OpenRouter/Groq APIs (your choice, configurable), optionally run a local on-device model, and **control the browser itself** — navigate, extract page content, and take actions like "go to this site, gather info, generate and download a PDF."
