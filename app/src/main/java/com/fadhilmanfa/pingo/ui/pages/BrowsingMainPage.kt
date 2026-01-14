package com.fadhilmanfa.pingo.ui.pages

import android.content.Context
import android.graphics.Bitmap
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fadhilmanfa.pingo.data.HistoryItem
import com.fadhilmanfa.pingo.data.TabItem
import com.fadhilmanfa.pingo.data.remote.GroqRepository
import com.fadhilmanfa.pingo.ui.components.AiNavBar
import com.fadhilmanfa.pingo.ui.components.AiResponseBottomSheet
import com.fadhilmanfa.pingo.ui.components.PullToRefreshWebView
import com.fadhilmanfa.pingo.ui.components.FabMenuOverlay
import com.fadhilmanfa.pingo.ui.components.NavBar
import com.fadhilmanfa.pingo.ui.components.ScrollDirection
import com.fadhilmanfa.pingo.ui.components.TabSwitcherBottomSheet
import com.fadhilmanfa.pingo.ui.components.UrlEditingOverlay
import com.fadhilmanfa.pingo.ui.theme.Secondary
import com.fadhilmanfa.pingo.ui.viewmodels.AiUiState
import com.fadhilmanfa.pingo.ui.viewmodels.AiViewModel
import com.fadhilmanfa.pingo.util.Config
import com.fadhilmanfa.pingo.util.WebContentParser
import kotlinx.coroutines.delay
import java.util.UUID

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

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BrowsingMainPage() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val sharedPrefs = remember { context.getSharedPreferences("pingo_browser_prefs", Context.MODE_PRIVATE) }
    
    // AI ViewModel setup
    val aiViewModel: AiViewModel = viewModel(
        factory = remember {
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
    var tabSheetDragProgress by remember { mutableFloatStateOf(0f) }
    var uiAlpha by remember { mutableFloatStateOf(0f) }
    var isAppStarting by remember { mutableStateOf(true) }
    
    val tabs = remember {
        val savedTabs = sharedPrefs.getStringSet("saved_tabs_data", null)
        val tabList = mutableStateListOf<TabItem>()
        if (savedTabs != null) {
            savedTabs.forEach { data ->
                val parts = data.split("|")
                if (parts.size >= 4) {
                    tabList.add(TabItem(id = parts[0], title = parts[1], url = parts[2], isActive = parts[3].toBoolean()))
                }
            }
        }
        if (tabList.isEmpty()) {
            tabList.add(TabItem(id = UUID.randomUUID().toString(), title = "Google", url = "https://www.google.com", isActive = true))
        } else if (tabList.none { it.isActive }) {
            tabList[0] = tabList[0].copy(isActive = true)
        }
        tabList
    }

    val historyItems = remember {
        val savedHistory = sharedPrefs.getStringSet("history_data", null)
        val historyList = mutableStateListOf<HistoryItem>()
        if (savedHistory != null) {
            val items = savedHistory.mapNotNull { data ->
                val parts = data.split("|")
                if (parts.size >= 4) {
                    HistoryItem(id = parts[0], title = parts[1], url = parts[2], timestamp = parts[3].toLong())
                } else null
            }.sortedByDescending { it.timestamp }
            historyList.addAll(items)
        }
        historyList
    }
    
    val webViewInstances = remember { mutableStateMapOf<String, WebView>() }
    
    val activeTab = tabs.find { it.isActive } ?: tabs.firstOrNull() ?: TabItem(id = "loading", title = "Pingo", url = "https://www.google.com")
    
    fun persistTabs() {
        val dataSet = tabs.map { "${it.id}|${it.title}|${it.url}|${it.isActive}" }.toSet()
        sharedPrefs.edit().putStringSet("saved_tabs_data", dataSet).apply()
    }

    fun persistHistory() {
        val dataSet = historyItems.map { "${it.id}|${it.title}|${it.url}|${it.timestamp}" }.toSet()
        sharedPrefs.edit().putStringSet("history_data", dataSet).apply()
    }

    fun addToHistory(title: String, url: String) {
        if (url.isBlank() || url == "about:blank" || url.startsWith("javascript:")) return
        
        val lastItem = historyItems.firstOrNull()
        if (lastItem?.url == url) {
            // Update title if it was generic and now we have a better one
            if ((lastItem.title == lastItem.url || lastItem.title == "Google") && title.isNotBlank() && title != url) {
                val idx = historyItems.indexOf(lastItem)
                if (idx >= 0) {
                    historyItems[idx] = lastItem.copy(title = title)
                    persistHistory()
                }
            }
            return
        }

        val newItem = HistoryItem(
            id = UUID.randomUUID().toString(),
            title = if (title.isBlank()) url else title,
            url = url,
            timestamp = System.currentTimeMillis()
        )
        historyItems.add(0, newItem)
        if (historyItems.size > 500) {
            historyItems.removeAt(historyItems.size - 1)
        }
        persistHistory()
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
    val bottomPos = screenHeightPx - navBarPaddingPx - navBarHeightPx - with(density) { 16.dp.toPx() }
    val offScreenPos = screenHeightPx + navBarHeightPx
    val topOffScreenPos = -navBarHeightPx

    LaunchedEffect(Unit) {
        delay(500)
        uiAlpha = 1f
        isAppStarting = false
    }

    LaunchedEffect(tabs.toList()) {
        persistTabs()
    }
    
    // Extract page content when AI mode is activated
    LaunchedEffect(isAiModeActive) {
        if (isAiModeActive) {
            // Berikan efek getar saat AI diaktifkan
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            
            // Trigger JavaScript extraction
            webViewInstances[activeTab.id]?.evaluateJavascript(
                WebContentParser.extractionScript,
                null
            )
        } else {
            // Clear context when AI mode is deactivated
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

    BackHandler(enabled = activeTab.canGoBack || showMenu || showTabSheet || isAiModeActive || showSettings || showAdBlocker || showWhitelist || showHistory) {
        when {
            showWhitelist -> showWhitelist = false
            showAdBlocker -> showAdBlocker = false
            showSettings -> showSettings = false
            showHistory -> showHistory = false
            isAiModeActive -> isAiModeActive = false
            showTabSheet -> { showTabSheet = false; tabSheetDragProgress = 0f }
            showMenu -> showMenu = false
            else -> webViewInstances[activeTab.id]?.goBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Main Content (WebView)
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().height(statusBarPadding).background(Color.White))
            Box(modifier = Modifier.weight(1f)) {
                tabs.forEach { tab ->
                    key(tab.id) {
                        val isCurrent = tab.id == activeTab.id
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { 
                                    alpha = if (isCurrent) 1f else 0f
                                    translationX = if (isCurrent) 0f else 10000f 
                                }
                        ) {
                            PullToRefreshWebView(
                                modifier = Modifier.fillMaxSize(),
                                url = tab.url,
                                onUrlChange = { url ->
                                    val idx = tabs.indexOfFirst { it.id == tab.id }
                                    if (idx >= 0) {
                                        tabs[idx] = tabs[idx].copy(url = url)
                                        persistTabs()
                                        addToHistory(tabs[idx].title, url)
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
                                    if (tab.isActive && !isUrlEditingMode && !showMenu && !isAppStarting && !isAiModeActive) {
                                        isNavBarCollapsed = (direction == ScrollDirection.DOWN)
                                    }
                                },
                                onContentExtracted = { jsonContent ->
                                    // Parse JSON and convert to Markdown for AI
                                    if (tab.isActive) {
                                        val pageContent = WebContentParser.parseFromJson(jsonContent)
                                        if (pageContent != null) {
                                            val markdown = WebContentParser.toMarkdown(pageContent)
                                            aiViewModel.setPageContext(markdown)
                                        }
                                    }
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
            Box(modifier = Modifier.fillMaxWidth().height(navBarPadding).background(Color.White))
        }

        // --- AI EFFECTS OVERLAY (FREEZE LAYER) ---
        val aiGlowProgress by animateFloatAsState(
            targetValue = if (isAiModeActive) 1f else 0f,
            animationSpec = tween(900),
            label = "AiGlowProgress"
        )
        
        if (aiGlowProgress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
                    .pointerInput(Unit) {
                        // Mematikan interaksi di halaman belakang (Freeze)
                        detectTapGestures { }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = aiGlowProgress }
                        .background(Color.Black.copy(alpha = 0.2f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .graphicsLayer { 
                            val startY = screenHeightPx
                            val endY = -300.dp.toPx()
                            translationY = startY + (endY - startY) * aiGlowProgress
                            alpha = if (aiGlowProgress < 0.1f) aiGlowProgress * 10f 
                                    else if (aiGlowProgress > 0.9f) (1f - aiGlowProgress) * 10f 
                                    else 1f
                        }
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Secondary.copy(alpha = 0.5f), Color.Transparent)
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f)
                        .align(Alignment.BottomCenter)
                        .graphicsLayer { 
                            alpha = aiGlowProgress
                            translationY = (1f - aiGlowProgress) * 200.dp.toPx()
                        }
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Secondary.copy(alpha = 0.3f))
                            )
                        )
                )
            }
        }

        // Overlay UI
        val targetY = when {
            isAiModeActive -> if (isNavBarAtTop) topOffScreenPos else offScreenPos
            isNavBarAtTop -> topPos
            else -> bottomPos
        }
        
        val animatedY by animateFloatAsState(
            targetValue = targetY,
            animationSpec = tween(durationMillis = 600),
            label = "NavBarTranslation"
        )
        
        val aiAlpha by animateFloatAsState(
            targetValue = if (isAiModeActive) 1f else 0f,
            animationSpec = tween(durationMillis = 400),
            label = "AiNavBarAlpha"
        )

        val aiTranslationY by animateFloatAsState(
            targetValue = if (isAiModeActive) 0f else with(density) { 300.dp.toPx() },
            animationSpec = tween(durationMillis = 600),
            label = "AiNavBarTranslation"
        )

        val editModeAlpha by animateFloatAsState(
            targetValue = if (isUrlEditingMode) 0f else 1f,
            animationSpec = tween(400)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(2f)
                .graphicsLayer { alpha = uiAlpha * editModeAlpha }
                .imePadding()
        ) {
            NavBar(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .graphicsLayer { translationY = animatedY },
                currentUrl = activeTab.url,
                isLoading = activeTab.isLoading,
                loadingProgress = activeTab.progress,
                canGoBack = activeTab.canGoBack,
                canGoForward = activeTab.canGoForward,
                isCollapsed = isNavBarCollapsed,
                showMenu = showMenu,
                tabCount = tabs.size,
                onBackPressed = { webViewInstances[activeTab.id]?.goBack() },
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
                modifier = Modifier
                    .align(Alignment.BottomCenter)
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
                modifier = Modifier.then(
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
                onSettings = { showSettings = true },
                onHistory = { showHistory = true }
            )
        }

        if (isUrlEditingMode) {
            UrlEditingOverlay(
                currentUrl = activeTab.url,
                isLoading = activeTab.isLoading,
                isVisible = isUrlEditingMode,
                statusBarHeight = statusBarPaddingPx,
                onUrlSubmitted = { url ->
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
                tabs.removeIf { it.id == tabToClose.id }
                if (tabs.isEmpty()) {
                    val newId = UUID.randomUUID().toString()
                    tabs.add(TabItem(id = newId, title = "Google", url = "https://www.google.com", isActive = true))
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
                tabs.add(TabItem(id = newId, title = "Tab Baru", url = "https://www.google.com", isActive = true))
                persistTabs()
                showTabSheet = false
            }
        )

        // Show AI Response
        if (aiUiState is AiUiState.Success) {
            AiResponseBottomSheet(
                response = (aiUiState as AiUiState.Success).response,
                onDismiss = { aiViewModel.reset() }
            )
        }

        // Settings Page Overlay
        AnimatedVisibility(
            visible = showSettings,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) + fadeOut(),
            modifier = Modifier.zIndex(10f)
        ) {
            SettingsPage(
                onBack = { showSettings = false },
                onNavigateToAdBlocker = { showAdBlocker = true }
            )
        }

        // AdBlocker Page Overlay
        AnimatedVisibility(
            visible = showAdBlocker,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) + fadeOut(),
            modifier = Modifier.zIndex(11f)
        ) {
            AdBlockerPage(
                onBack = { showAdBlocker = false },
                onNavigateToWhitelist = { showWhitelist = true }
            )
        }

        // Whitelist Page Overlay
        AnimatedVisibility(
            visible = showWhitelist,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) + fadeOut(),
            modifier = Modifier.zIndex(12f)
        ) {
            WhitelistPage(onBack = { showWhitelist = false })
        }

        // History Page Overlay
        AnimatedVisibility(
            visible = showHistory,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) + fadeOut(),
            modifier = Modifier.zIndex(13f)
        ) {
            HistoryPage(
                historyItems = historyItems,
                onBack = { showHistory = false },
                onUrlClick = { url ->
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
            visible = isAppStarting,
            enter = fadeIn(),
            exit = fadeOut(tween(600))
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
                LoadingIndicator(modifier = Modifier.size(56.dp), color = Secondary)
            }
        }
    }
}
