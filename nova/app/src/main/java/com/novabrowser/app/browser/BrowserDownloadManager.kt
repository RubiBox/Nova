package com.novabrowser.app.browser

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import com.novabrowser.app.data.BrowserRepository
import com.novabrowser.app.data.DownloadRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BrowserDownloadManager(
    private val context: Context,
    private val repository: BrowserRepository
) {
    private val systemDownloadManager =
        context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    /** Kicks off a system download (handles large files, progress notification, resumption). */
    fun startDownload(url: String, userAgent: String?, contentDisposition: String?, mimeType: String?) {
        val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
        val resolvedMime = mimeType?.takeIf { it.isNotBlank() }
            ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileName.substringAfterLast('.', ""))
            ?: "application/octet-stream"

        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setMimeType(resolvedMime)
            addRequestHeader("User-Agent", userAgent)
            setDescription("Downloading via Nova Browser")
            setTitle(fileName)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }

        val downloadId = systemDownloadManager.enqueue(request)

        CoroutineScope(Dispatchers.IO).launch {
            repository.downloads.insert(
                DownloadRecord(
                    fileName = fileName,
                    url = url,
                    localUri = "downloadId:$downloadId",
                    mimeType = resolvedMime,
                    status = "RUNNING"
                )
            )
        }
    }

    /**
     * Direct programmatic download used by the AI agent (Phase 2) to save generated
     * files (e.g. an assembled PDF) straight into Downloads without a network request.
     */
    fun saveGeneratedFile(fileName: String, mimeType: String, bytes: ByteArray): Uri? {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = java.io.File(downloadsDir, fileName)
            file.writeBytes(bytes)
            Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }
}
