package com.fadhilmanfa.pingo.ui.pages

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Parcel
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fadhilmanfa.pingo.data.BookmarkItem
import com.fadhilmanfa.pingo.data.DownloadItem
import com.fadhilmanfa.pingo.data.DownloadStatus
import com.fadhilmanfa.pingo.data.HistoryItem
import com.fadhilmanfa.pingo.data.TabItem
import com.fadhilmanfa.pingo.data.remote.GroqRepository
import com.fadhilmanfa.pingo.ui.components.AiNavBar
import com.fadhilmanfa.pingo.ui.components.AiResponseBottomSheet
import com.fadhilmanfa.pingo.ui.components.BookmarkBottomSheet
import com.fadhilmanfa.pingo.ui.components.FabMenuOverlay
import com.fadhilmanfa.pingo.ui.components.NavBar
import com.fadhilmanfa.pingo.ui.components.PrewarmComposables
import com.fadhilmanfa.pingo.ui.components.PullToRefreshWebView
import com.fadhilmanfa.pingo.ui.components.ScrollDirection
import com.fadhilmanfa.pingo.ui.components.TabSwitcherBottomSheet
import com.fadhilmanfa.pingo.ui.components.UrlEditingOverlay
import com.fadhilmanfa.pingo.ui.theme.Secondary
import com.fadhilmanfa.pingo.ui.viewmodels.AiUiState
import com.fadhilmanfa.pingo.ui.viewmodels.AiViewModel
import com.fadhilmanfa.pingo.util.Config
import com.fadhilmanfa.pingo.util.WebContentParser
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlinx.coroutines.delay

private fun captureWebViewThumbnail(webView: WebView, maxWidth: Int = 300): Bitmap? {
    return try {
        if (webView.width <= 0 || webView.height <= 0) return null
        val scale = maxWidth.toFloat() / webView.width
        val scaledHeight = (webView.height * scale).toInt()
        val bitmap = Bitmap.createBitmap(maxWidth, scaledHeight, Bitmap.Config.RGB_565)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.scale(scale, scale)
        webView.draw(canvas)
        bitmap
    } catch (e: Exception) {
        null
    }
}

// Delimiter yang lebih aman untuk menghindari konflik dengan konten URL/Title
private const val DATA_DELIMITER = "|||"

@Composable
fun BrowsingMainPage(currentTheme: String = "system", onThemeChanged: (String) -> Unit = {}) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val sharedPrefs = remember {
        context.getSharedPreferences("pingo_browser_prefs", Context.MODE_PRIVATE)
    }

    // AI ViewModel setup
    val aiViewModel: AiViewModel =
            viewModel(
                    factory =
                            remember {
                                object : ViewModelProvider.Factory {
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        @Suppress("UNCHECKED_CAST")
                                        return AiViewModel(GroqRepository(Config.GROQ_API_KEY)) as T
                                    }
                                }
                            }
            )
    val aiUiState by aiViewModel.uiState.collectAsStateWithLifecycle()

    var isNavBarCollapsed by remember { mutableStateOf(false) }
    var isNavBarAtTop by remember { mutableStateOf(false) }
    var isUrlEditingMode by remember { mutableStateOf(false) }
    var isAiModeActive by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showTabSheet by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showAdBlocker by remember { mutableStateOf(false) }
    var showWhitelist by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showDownloads by remember { mutableStateOf(false) }
    var showBookmarkSheet by remember { mutableStateOf(false) }
    var tabSheetDragProgress by remember { mutableFloatStateOf(0f) }
    var uiAlpha by remember { mutableFloatStateOf(0f) }
    var isAppStarting by remember { mutableStateOf(true) }

    val tabs = remember {
        val savedTabs = sharedPrefs.getStringSet("saved_tabs_data", null)
        val tabList = mutableStateListOf<TabItem>()
        if (savedTabs != null) {
            savedTabs.forEach { data ->
                // Coba parsing dengan delimiter baru dulu, jika gagal gunakan yang lama
                val parts =
                        if (data.contains(DATA_DELIMITER)) {
                            data.split(DATA_DELIMITER)
                        } else {
                            data.split("|")
                        }

                if (parts.size >= 4) {
                    tabList.add(
                            TabItem(
                                    id = parts[0],
                                    title = parts[1],
                                    url = parts[2],
                                    isActive = parts[3].toBoolean()
                            )
                    )
                }
            }
        }
        if (tabList.isEmpty()) {
            tabList.add(
                    TabItem(
                            id = UUID.randomUUID().toString(),
                            title = "Google",
                            url = "https://www.google.com",
                            isActive = true
                    )
            )
        } else if (tabList.none { it.isActive }) {
            tabList[0] = tabList[0].copy(isActive = true)
        }
        tabList
    }

    val historyItems = remember {
        val savedHistory = sharedPrefs.getStringSet("history_data", null)
        val historyList = mutableStateListOf<HistoryItem>()
        if (savedHistory != null) {
            val items =
                    savedHistory
                            .mapNotNull { data ->
                                val parts =
                                        if (data.contains(DATA_DELIMITER)) {
                                            data.split(DATA_DELIMITER)
                                        } else {
                                            data.split("|")
                                        }

                                if (parts.size >= 4) {
                                    val timestamp =
                                            parts[3].toLongOrNull() ?: System.currentTimeMillis()
                                    HistoryItem(
                                            id = parts[0],
                                            title = parts[1],
                                            url = parts[2],
                                            timestamp = timestamp
                                    )
                                } else null
                            }
                            .sortedByDescending { it.timestamp }
            historyList.addAll(items)
        }
        historyList
    }

    val bookmarkItems = remember {
        val savedBookmarks = sharedPrefs.getStringSet("bookmark_data", null)
        val bookmarkList = mutableStateListOf<BookmarkItem>()
        if (savedBookmarks != null) {
            val items =
                    savedBookmarks
                            .mapNotNull { data ->
                                val parts = data.split(DATA_DELIMITER)
                                if (parts.size >= 4) {
                                    val timestamp =
                                            parts[3].toLongOrNull() ?: System.currentTimeMillis()
                                    BookmarkItem(
                                            id = parts[0],
                                            title = parts[1],
                                            url = parts[2],
                                            timestamp = timestamp
                                    )
                                } else null
                            }
                            .sortedByDescending { it.timestamp }
            bookmarkList.addAll(items)
        }
        bookmarkList
    }

    val downloadItems = remember {
        val savedDownloads = sharedPrefs.getStringSet("download_data", null)
        val downloadList = mutableStateListOf<DownloadItem>()
        if (savedDownloads != null) {
            val items =
                    savedDownloads
                            .mapNotNull { data ->
                                val parts = data.split(DATA_DELIMITER)
                                if (parts.size >= 8) {
                                    val androidId =
                                            if (parts.size >= 9) parts[8].toLongOrNull() else null
                                    DownloadItem(
                                            id = parts[0],
                                            fileName = parts[1],
                                            url = parts[2],
                                            filePath = parts[3],
                                            totalSize = parts[4].toLongOrNull() ?: 0L,
                                            downloadedSize = parts[5].toLongOrNull() ?: 0L,
                                            status =
                                                    try {
                                                        DownloadStatus.valueOf(parts[6])
                                                    } catch (e: Exception) {
                                                        DownloadStatus.COMPLETED
                                                    },
                                            timestamp = parts[7].toLongOrNull()
                                                            ?: System.currentTimeMillis(),
                                            androidId = androidId
                                    )
                                } else null
                            }
                            .sortedByDescending { it.timestamp }
            downloadList.addAll(items)
        }
        downloadList
    }

    val webViewInstances = remember { mutableStateMapOf<String, WebView>() }

    // Logic to save/load WebView state bundles from disk
    fun saveWebViewStateToDisk(tabId: String, webView: WebView) {
        val bundle = Bundle()
        if (webView.saveState(bundle) != null) {
            try {
                val file = File(context.filesDir, "wv_state_$tabId")
                val fos = FileOutputStream(file)
                val parcel = Parcel.obtain()
                parcel.writeBundle(bundle)
                fos.write(parcel.marshall())
                fos.close()
                parcel.recycle()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadWebViewStateFromDisk(tabId: String): Bundle? {
        return try {
            val file = File(context.filesDir, "wv_state_$tabId")
            if (!file.exists()) return null
            val bytes = file.readBytes()
            val parcel = Parcel.obtain()
            parcel.unmarshall(bytes, 0, bytes.size)
            parcel.setDataPosition(0)
            val bundle = parcel.readBundle(context.classLoader)
            parcel.recycle()
            bundle
        } catch (e: Exception) {
            null
        }
    }

    val webViewStateBundles = remember {
        val bundles = mutableMapOf<String, Bundle?>()
        tabs.forEach { tab -> bundles[tab.id] = loadWebViewStateFromDisk(tab.id) }
        bundles
    }

    val activeTab =
            tabs.find { it.isActive }
                    ?: tabs.firstOrNull()
                            ?: TabItem(
                            id = "loading",
                            title = "Pingo",
                            url = "https://www.google.com"
                    )

    fun persistTabs() {
        val dataSet =
                tabs
                        .map {
                            "${it.id}$DATA_DELIMITER${it.title}$DATA_DELIMITER${it.url}$DATA_DELIMITER${it.isActive}"
                        }
                        .toSet()
        sharedPrefs.edit().putStringSet("saved_tabs_data", dataSet).apply()
    }

    fun persistHistory() {
        val dataSet =
                historyItems
                        .map {
                            "${it.id}$DATA_DELIMITER${it.title}$DATA_DELIMITER${it.url}$DATA_DELIMITER${it.timestamp}"
                        }
                        .toSet()
        sharedPrefs.edit().putStringSet("history_data", dataSet).apply()
    }

    fun persistBookmarks() {
        val dataSet =
                bookmarkItems
                        .map {
                            "${it.id}$DATA_DELIMITER${it.title}$DATA_DELIMITER${it.url}$DATA_DELIMITER${it.timestamp}"
                        }
                        .toSet()
        sharedPrefs.edit().putStringSet("bookmark_data", dataSet).apply()
    }

    fun persistDownloads() {
        val dataSet =
                downloadItems
                        .map {
                            "${it.id}$DATA_DELIMITER${it.fileName}$DATA_DELIMITER${it.url}$DATA_DELIMITER${it.filePath}$DATA_DELIMITER${it.totalSize}$DATA_DELIMITER${it.downloadedSize}$DATA_DELIMITER${it.status.name}$DATA_DELIMITER${it.timestamp}$DATA_DELIMITER${it.androidId ?: ""}"
                        }
                        .toSet()
        sharedPrefs.edit().putStringSet("download_data", dataSet).apply()
    }

    fun addToHistory(title: String, url: String) {
        if (url.isBlank() || url == "about:blank" || url.startsWith("javascript:")) return

        val lastItem = historyItems.firstOrNull()
        val now = System.currentTimeMillis()

        if (lastItem != null) {
            val isSameUrl = lastItem.url == url
            val isSimilarSearch =
                    try {
                        val lastUri = java.net.URI(lastItem.url)
                        val currentUri = java.net.URI(url)
                        lastUri.host == currentUri.host &&
                                lastUri.path == currentUri.path &&
                                (url.contains("/search") || url.contains("q=")) &&
                                (now - lastItem.timestamp < 3000)
                    } catch (e: Exception) {
                        false
                    }

            if (isSameUrl || isSimilarSearch) {
                val idx = historyItems.indexOf(lastItem)
                if (idx >= 0) {
                    val newTitle = if (title.isNotBlank() && title != url) title else lastItem.title
                    historyItems[idx] = lastItem.copy(title = newTitle, url = url, timestamp = now)
                    persistHistory()
                }
                return
            }
        }

        val newItem =
                HistoryItem(
                        id = UUID.randomUUID().toString(),
                        title = if (title.isBlank()) url else title,
                        url = url,
                        timestamp = now
                )
        historyItems.add(0, newItem)
        if (historyItems.size > 500) {
            historyItems.removeAt(historyItems.size - 1)
        }
        persistHistory()
    }

    fun toggleBookmark(tab: TabItem) {
        val existing = bookmarkItems.find { it.url == tab.url }
        if (existing != null) {
            bookmarkItems.remove(existing)
        } else {
            bookmarkItems.add(
                    0,
                    BookmarkItem(
                            id = UUID.randomUUID().toString(),
                            title = if (tab.title.isBlank()) tab.url else tab.title,
                            url = tab.url,
                            timestamp = System.currentTimeMillis()
                    )
            )
        }
        persistBookmarks()
    }

    fun openDownloadedFile(item: DownloadItem) {
        if (item.status != DownloadStatus.COMPLETED) return

        val file = File(item.filePath)
        if (!file.exists()) {
            Toast.makeText(context, "File tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val uri =
                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            val extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString())
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

            val intent =
                    Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, mimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
            context.startActivity(Intent.createChooser(intent, "Buka dengan"))
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membuka file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun handleDownload(
            url: String,
            userAgent: String,
            contentDisposition: String,
            mimetype: String,
            contentLength: Long
    ) {
        try {
            val fileName = URLUtil.guessFileName(url, contentDisposition, mimetype)
            val request = DownloadManager.Request(Uri.parse(url))
            request.setMimeType(mimetype)
            request.addRequestHeader("User-Agent", userAgent)
            request.setDescription("Mengunduh file...")
            request.setTitle(fileName)
            request.setNotificationVisibility(
                    DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
            )
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = dm.enqueue(request)

            val newItem =
                    DownloadItem(
                            id = UUID.randomUUID().toString(),
                            fileName = fileName,
                            url = url,
                            filePath =
                                    "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/$fileName",
                            totalSize = contentLength,
                            downloadedSize = 0L,
                            status = DownloadStatus.DOWNLOADING,
                            timestamp = System.currentTimeMillis(),
                            androidId = downloadId
                    )
            downloadItems.add(0, newItem)
            persistDownloads()

            Toast.makeText(context, "Mulai mengunduh $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal mengunduh: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Logic to update download progress
    LaunchedEffect(downloadItems.any { it.status == DownloadStatus.DOWNLOADING }) {
        while (downloadItems.any { it.status == DownloadStatus.DOWNLOADING }) {
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadItems.forEachIndexed { index, item ->
                if (item.status == DownloadStatus.DOWNLOADING) {
                    val androidId = item.androidId
                    if (androidId != null) {
                        val query = DownloadManager.Query().setFilterById(androidId)
                        val cursor: Cursor = dm.query(query)
                        if (cursor.moveToFirst()) {
                            val bytesDownloaded =
                                    cursor.getLong(
                                            cursor.getColumnIndexOrThrow(
                                                    DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR
                                            )
                                    )
                            val bytesTotal =
                                    cursor.getLong(
                                            cursor.getColumnIndexOrThrow(
                                                    DownloadManager.COLUMN_TOTAL_SIZE_BYTES
                                            )
                                    )
                            val status =
                                    cursor.getInt(
                                            cursor.getColumnIndexOrThrow(
                                                    DownloadManager.COLUMN_STATUS
                                            )
                                    )

                            val newStatus =
                                    when (status) {
                                        DownloadManager.STATUS_SUCCESSFUL ->
                                                DownloadStatus.COMPLETED
                                        DownloadManager.STATUS_FAILED -> DownloadStatus.FAILED
                                        DownloadManager.STATUS_PAUSED -> DownloadStatus.PAUSED
                                        else -> DownloadStatus.DOWNLOADING
                                    }

                            if (item.downloadedSize != bytesDownloaded || item.status != newStatus
                            ) {
                                downloadItems[index] =
                                        item.copy(
                                                downloadedSize = bytesDownloaded,
                                                totalSize =
                                                        if (bytesTotal > 0) bytesTotal
                                                        else item.totalSize,
                                                status = newStatus
                                        )
                                persistDownloads()
                            }
                        } else {
                            // If cursor is empty, the download might have been cancelled or removed
                            downloadItems[index] = item.copy(status = DownloadStatus.FAILED)
                            persistDownloads()
                        }
                        cursor.close()
                    }
                }
            }
            delay(1000) // Update every second
        }
    }

    val density = LocalDensity.current
    val config = LocalConfiguration.current
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }
    val statusBarPaddingPx = with(density) { statusBarPadding.toPx() }
    val navBarPaddingPx = with(density) { navBarPadding.toPx() }
    val navBarHeightPx = with(density) { 72.dp.toPx() }

    val topPos = statusBarPaddingPx + with(density) { 15.dp.toPx() }
    val bottomPos =
            screenHeightPx - navBarPaddingPx - navBarHeightPx - with(density) { 16.dp.toPx() }
    val offScreenPos = screenHeightPx + navBarHeightPx
    val topOffScreenPos = -navBarHeightPx

    LaunchedEffect(Unit) {
        delay(500)
        uiAlpha = 1f
        isAppStarting = false
    }

    LaunchedEffect(tabs.toList()) { persistTabs() }

    LaunchedEffect(isAiModeActive) {
        if (isAiModeActive) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            webViewInstances[activeTab.id]?.evaluateJavascript(
                    WebContentParser.extractionScript,
                    null
            )
        } else {
            aiViewModel.clearPageContext()
        }
    }

    fun updateTabThumbnail(tabId: String) {
        val wv = webViewInstances[tabId]
        val idx = tabs.indexOfFirst { it.id == tabId }
        if (wv != null && idx >= 0) {
            val thumbnail = captureWebViewThumbnail(wv)
            if (thumbnail != null) {
                tabs[idx] = tabs[idx].copy(thumbnail = thumbnail)
            }
        }
    }

    BackHandler(
            enabled =
                    activeTab.canGoBack ||
                            showMenu ||
                            showTabSheet ||
                            isAiModeActive ||
                            showSettings ||
                            showAdBlocker ||
                            showWhitelist ||
                            showHistory ||
                            showBookmarkSheet ||
                            showDownloads
    ) {
        when {
            showWhitelist -> showWhitelist = false
            showAdBlocker -> showAdBlocker = false
            showSettings -> showSettings = false
            showHistory -> showHistory = false
            showDownloads -> showDownloads = false
            showBookmarkSheet -> showBookmarkSheet = false
            isAiModeActive -> isAiModeActive = false
            showTabSheet -> {
                showTabSheet = false
                tabSheetDragProgress = 0f
            }
            showMenu -> showMenu = false
            else -> webViewInstances[activeTab.id]?.goBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .height(statusBarPadding)
                                    .background(MaterialTheme.colorScheme.background)
            )
            Box(modifier = Modifier.weight(1f)) {
                tabs.forEach { tab ->
                    key(tab.id) {
                        val isCurrent = tab.id == activeTab.id
                        Box(
                                modifier =
                                        Modifier.fillMaxSize().graphicsLayer {
                                            alpha = if (isCurrent) 1f else 0f
                                            translationX = if (isCurrent) 0f else 10000f
                                        }
                        ) {
                            PullToRefreshWebView(
                                    modifier = Modifier.fillMaxSize(),
                                    url = tab.url,
                                    savedState = webViewStateBundles[tab.id],
                                    onUrlChange = { url ->
                                        val idx = tabs.indexOfFirst { it.id == tab.id }
                                        if (idx >= 0) {
                                            tabs[idx] = tabs[idx].copy(url = url)
                                            persistTabs()
                                        }
                                    },
                                    onTitleChange = { title ->
                                        val idx = tabs.indexOfFirst { it.id == tab.id }
                                        if (idx >= 0) {
                                            tabs[idx] = tabs[idx].copy(title = title)
                                            persistTabs()
                                            addToHistory(title, tabs[idx].url)
                                        }
                                    },
                                    onLoadingChange = { loading ->
                                        val idx = tabs.indexOfFirst { it.id == tab.id }
                                        if (idx >= 0) {
                                            tabs[idx] = tabs[idx].copy(isLoading = loading)
                                            if (!loading) {
                                                updateTabThumbnail(tab.id)
                                                addToHistory(tabs[idx].title, tabs[idx].url)
                                                // SAVE WEBVIEW STATE TO DISK
                                                webViewInstances[tab.id]?.let {
                                                    saveWebViewStateToDisk(tab.id, it)
                                                }
                                            }
                                        }
                                    },
                                    onProgressChange = { progress ->
                                        val idx = tabs.indexOfFirst { it.id == tab.id }
                                        if (idx >= 0) {
                                            tabs[idx] = tabs[idx].copy(progress = progress)
                                        }
                                    },
                                    onCanGoBackChange = { canBack ->
                                        val idx = tabs.indexOfFirst { it.id == tab.id }
                                        if (idx >= 0) {
                                            tabs[idx] = tabs[idx].copy(canGoBack = canBack)
                                        }
                                    },
                                    onCanGoForwardChange = { canForward ->
                                        val idx = tabs.indexOfFirst { it.id == tab.id }
                                        if (idx >= 0) {
                                            tabs[idx] = tabs[idx].copy(canGoForward = canForward)
                                        }
                                    },
                                    onScrollDirectionChange = { direction ->
                                        if (tab.isActive &&
                                                        !isUrlEditingMode &&
                                                        !showMenu &&
                                                        !isAppStarting &&
                                                        !isAiModeActive
                                        ) {
                                            isNavBarCollapsed = (direction == ScrollDirection.DOWN)
                                        }
                                    },
                                    onContentExtracted = { jsonContent ->
                                        if (tab.isActive) {
                                            val pageContent =
                                                    WebContentParser.parseFromJson(jsonContent)
                                            if (pageContent != null) {
                                                val markdown =
                                                        WebContentParser.toMarkdown(pageContent)
                                                aiViewModel.setPageContext(markdown)
                                            }
                                        }
                                    },
                                    onDownloadStart = { url, ua, cd, mime, len ->
                                        handleDownload(url, ua, cd, mime, len)
                                    },
                                    webViewRef = { wv ->
                                        if (webViewInstances[tab.id] == null) {
                                            webViewInstances[tab.id] = wv
                                        }
                                    }
                            )
                        }
                    }
                }
            }
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .height(navBarPadding)
                                    .background(MaterialTheme.colorScheme.background)
            )
        }

        // --- AI EFFECTS OVERLAY ---
        val aiGlowProgress by
                animateFloatAsState(
                        targetValue = if (isAiModeActive) 1f else 0f,
                        animationSpec = tween(900),
                        label = "AiGlowProgress"
                )

        if (aiGlowProgress > 0f) {
            Box(
                    modifier =
                            Modifier.fillMaxSize().zIndex(1f).pointerInput(Unit) {
                                detectTapGestures {}
                            }
            ) {
                Box(
                        modifier =
                                Modifier.fillMaxSize()
                                        .graphicsLayer { alpha = aiGlowProgress }
                                        .background(Color.Black.copy(alpha = 0.2f))
                )
                Box(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .height(300.dp)
                                        .graphicsLayer {
                                            val startY = screenHeightPx
                                            val endY = -300.dp.toPx()
                                            translationY = startY + (endY - startY) * aiGlowProgress
                                            alpha =
                                                    if (aiGlowProgress < 0.1f) aiGlowProgress * 10f
                                                    else if (aiGlowProgress > 0.9f)
                                                            (1f - aiGlowProgress) * 10f
                                                    else 1f
                                        }
                                        .background(
                                                Brush.verticalGradient(
                                                        colors =
                                                                listOf(
                                                                        Color.Transparent,
                                                                        Secondary.copy(
                                                                                alpha = 0.5f
                                                                        ),
                                                                        Color.Transparent
                                                                )
                                                )
                                        )
                )
            }
        }

        val targetY =
                when {
                    isAiModeActive -> if (isNavBarAtTop) topOffScreenPos else offScreenPos
                    isNavBarAtTop -> topPos
                    else -> bottomPos
                }

        val animatedY by
                animateFloatAsState(
                        targetValue = targetY,
                        animationSpec =
                                spring(
                                        dampingRatio = 0.70f, // Bouncy effect
                                        stiffness = 150f // Lebih pelan
                                ),
                        label = "NavBarTranslation"
                )

        val aiAlpha by
                animateFloatAsState(
                        targetValue = if (isAiModeActive) 1f else 0f,
                        animationSpec = tween(durationMillis = 400),
                        label = "AiNavBarAlpha"
                )

        val aiTranslationY by
                animateFloatAsState(
                        targetValue = if (isAiModeActive) 0f else with(density) { 300.dp.toPx() },
                        animationSpec = tween(durationMillis = 600),
                        label = "AiNavBarTranslation"
                )

        val editModeAlpha by
                animateFloatAsState(
                        targetValue = if (isUrlEditingMode) 0f else 1f,
                        animationSpec = tween(400)
                )

        Box(
                modifier =
                        Modifier.fillMaxSize()
                                .zIndex(2f)
                                .graphicsLayer { alpha = uiAlpha * editModeAlpha }
                                .imePadding()
        ) {
            NavBar(
                    modifier =
                            Modifier.align(Alignment.TopCenter).graphicsLayer {
                                translationY = animatedY
                            },
                    currentUrl = activeTab.url,
                    isLoading = activeTab.isLoading,
                    loadingProgress = activeTab.progress,
                    canGoBack = activeTab.canGoBack,
                    canGoForward = activeTab.canGoForward,
                    isCollapsed = isNavBarCollapsed,
                    showMenu = showMenu,
                    tabCount = tabs.size,
                    onBackPressed = {
                        webViewInstances[activeTab.id]?.let { if (it.canGoBack()) it.goBack() }
                    },
                    onMenuToggle = { showMenu = !showMenu },
                    onRefresh = { webViewInstances[activeTab.id]?.reload() },
                    onForward = { webViewInstances[activeTab.id]?.goForward() },
                    onUrlBarTap = { isUrlEditingMode = true },
                    onTapToExpand = { isNavBarCollapsed = false },
                    onTabButtonClick = {
                        updateTabThumbnail(activeTab.id)
                        showTabSheet = true
                    },
                    onSwipeUpToMoveTop = { isNavBarAtTop = true },
                    onSwipeDownToMoveBottom = { isNavBarAtTop = false }
            )

            AiNavBar(
                    modifier =
                            Modifier.align(Alignment.BottomCenter)
                                    .navigationBarsPadding()
                                    .padding(bottom = 16.dp)
                                    .graphicsLayer {
                                        translationY = aiTranslationY
                                        alpha = aiAlpha
                                    },
                    isLoading = aiUiState is AiUiState.Loading,
                    onClose = { isAiModeActive = false },
                    onSend = { prompt -> aiViewModel.askAi(prompt) }
            )

            FabMenuOverlay(
                    modifier =
                            Modifier.then(
                                    if (isNavBarAtTop) Modifier.statusBarsPadding()
                                    else Modifier.navigationBarsPadding()
                            ),
                    expanded = showMenu && !isNavBarCollapsed && !isUrlEditingMode,
                    canGoForward = activeTab.canGoForward,
                    isAtTop = isNavBarAtTop,
                    onToggle = { showMenu = false },
                    onRefresh = { webViewInstances[activeTab.id]?.reload() },
                    onForward = { webViewInstances[activeTab.id]?.goForward() },
                    onPingoAI = { isAiModeActive = true },
                    onBookmark = { showBookmarkSheet = true },
                    onSettings = { showSettings = true },
                    onHistory = { showHistory = true },
                    onDownloads = { showDownloads = true }
            )
        }

        if (isUrlEditingMode) {
            UrlEditingOverlay(
                    currentUrl = activeTab.url,
                    isLoading = activeTab.isLoading,
                    isVisible = isUrlEditingMode,
                    statusBarHeight = statusBarPaddingPx,
                    onUrlSubmitted = { url ->
                        // Force loader to show immediately when URL is clicked/submitted
                        val idx = tabs.indexOfFirst { it.id == activeTab.id }
                        if (idx >= 0) {
                            tabs[idx] = tabs[idx].copy(isLoading = true, progress = 0f)
                        }
                        webViewInstances[activeTab.id]?.loadUrl(url)
                        isUrlEditingMode = false
                    },
                    onClose = { isUrlEditingMode = false }
            )
        }

        TabSwitcherBottomSheet(
                visible = showTabSheet,
                dragProgress = tabSheetDragProgress,
                tabs = tabs,
                onDismiss = {
                    showTabSheet = false
                    tabSheetDragProgress = 0f
                },
                onTabSelected = { selectedTab ->
                    updateTabThumbnail(activeTab.id)
                    tabs.replaceAll { it.copy(isActive = it.id == selectedTab.id) }
                    persistTabs()
                    showTabSheet = false
                },
                onTabClose = { tabToClose ->
                    val wasActive = tabToClose.isActive
                    webViewInstances.remove(tabToClose.id)?.apply {
                        stopLoading()
                        loadUrl("about:blank")
                        destroy()
                    }
                    File(context.filesDir, "wv_state_${tabToClose.id}").delete()
                    tabs.removeIf { it.id == tabToClose.id }
                    if (tabs.isEmpty()) {
                        val newId = UUID.randomUUID().toString()
                        tabs.add(
                                TabItem(
                                        id = newId,
                                        title = "Google",
                                        url = "https://www.google.com",
                                        isActive = true
                                )
                        )
                        showTabSheet = false
                    } else if (wasActive) {
                        val nextTab = tabs.first()
                        tabs.replaceAll { it.copy(isActive = it.id == nextTab.id) }
                    }
                    persistTabs()
                },
                onNewTab = {
                    updateTabThumbnail(activeTab.id)
                    val newId = UUID.randomUUID().toString()
                    tabs.replaceAll { it.copy(isActive = false) }
                    tabs.add(
                            TabItem(
                                    id = newId,
                                    title = "Tab Baru",
                                    url = "https://www.google.com",
                                    isActive = true
                            )
                    )
                    persistTabs()
                    showTabSheet = false
                }
        )

        if (showBookmarkSheet) {
            BookmarkBottomSheet(
                    activeTab = activeTab,
                    bookmarks = bookmarkItems,
                    isCurrentPageBookmarked = bookmarkItems.any { it.url == activeTab.url },
                    onDismiss = { showBookmarkSheet = false },
                    onToggleBookmark = { toggleBookmark(it) },
                    onBookmarkClick = { bookmark ->
                        val idx = tabs.indexOfFirst { it.id == activeTab.id }
                        if (idx >= 0) {
                            tabs[idx] = tabs[idx].copy(isLoading = true, progress = 0f)
                        }
                        webViewInstances[activeTab.id]?.loadUrl(bookmark.url)
                        showBookmarkSheet = false
                    },
                    onDeleteBookmark = { bookmark ->
                        bookmarkItems.remove(bookmark)
                        persistBookmarks()
                    }
            )
        }

        if (aiUiState is AiUiState.Success) {
            AiResponseBottomSheet(
                    response = (aiUiState as AiUiState.Success).response,
                    onDismiss = { aiViewModel.reset() }
            )
        }

        AnimatedVisibility(
                visible = showSettings,
                enter =
                        slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) +
                                fadeIn(),
                exit =
                        slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) +
                                fadeOut(),
                modifier = Modifier.zIndex(10f)
        ) {
            SettingsPage(
                    onBack = { showSettings = false },
                    onNavigateToAdBlocker = { showAdBlocker = true },
                    currentTheme = currentTheme,
                    onThemeChanged = onThemeChanged
            )
        }

        AnimatedVisibility(
                visible = showAdBlocker,
                enter =
                        slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) +
                                fadeIn(),
                exit =
                        slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) +
                                fadeOut(),
                modifier = Modifier.zIndex(11f)
        ) {
            AdBlockerPage(
                    onBack = { showAdBlocker = false },
                    onNavigateToWhitelist = { showWhitelist = true }
            )
        }

        AnimatedVisibility(
                visible = showWhitelist,
                enter =
                        slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) +
                                fadeIn(),
                exit =
                        slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) +
                                fadeOut(),
                modifier = Modifier.zIndex(12f)
        ) { WhitelistPage(onBack = { showWhitelist = false }) }

        AnimatedVisibility(
                visible = showHistory,
                enter =
                        slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) +
                                fadeIn(),
                exit =
                        slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) +
                                fadeOut(),
                modifier = Modifier.zIndex(13f)
        ) {
            HistoryPage(
                    historyItems = historyItems,
                    onBack = { showHistory = false },
                    onUrlClick = { url ->
                        // Force loader to show immediately when URL is clicked
                        val idx = tabs.indexOfFirst { it.id == activeTab.id }
                        if (idx >= 0) {
                            tabs[idx] = tabs[idx].copy(isLoading = true, progress = 0f)
                        }
                        webViewInstances[activeTab.id]?.loadUrl(url)
                        showHistory = false
                    },
                    onClearHistory = {
                        historyItems.clear()
                        persistHistory()
                    },
                    onRemoveItem = { item ->
                        historyItems.remove(item)
                        persistHistory()
                    }
            )
        }

        AnimatedVisibility(
                visible = showDownloads,
                enter =
                        slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) +
                                fadeIn(),
                exit =
                        slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) +
                                fadeOut(),
                modifier = Modifier.zIndex(14f)
        ) {
            DownloadsPage(
                    downloadItems = downloadItems,
                    onBack = { showDownloads = false },
                    onFileClick = { item -> openDownloadedFile(item) },
                    onClearDownloads = {
                        downloadItems.clear()
                        persistDownloads()
                    },
                    onRemoveItem = { item ->
                        downloadItems.remove(item)
                        persistDownloads()
                    }
            )
        }

        AnimatedVisibility(visible = isAppStarting, enter = fadeIn(), exit = fadeOut(tween(600))) {
            Box(
                    modifier =
                            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(56.dp), color = Secondary)

                // Pre-warm heavy UI components during loading to prevent jank on first use
                PrewarmComposables()
            }
        }
    }
}
