package com.fadhilmanfa.pingo.ui.pages

import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.fadhilmanfa.pingo.ui.components.BrowserWebView
import com.fadhilmanfa.pingo.ui.components.FabMenuOverlay
import com.fadhilmanfa.pingo.ui.components.NavBar
import com.fadhilmanfa.pingo.ui.components.ScrollDirection
import com.fadhilmanfa.pingo.ui.components.UrlEditingOverlay
import com.fadhilmanfa.pingo.ui.theme.Secondary

@Composable
fun BrowsingMainPage() {
    // State
    var currentUrl by remember { mutableStateOf("https://www.google.com") }
    var isLoading by remember { mutableStateOf(true) }
    var loadingProgress by remember { mutableFloatStateOf(0f) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var isNavBarCollapsed by remember { mutableStateOf(false) }
    var isUrlEditingMode by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // WebView reference
    var webView by remember { mutableStateOf<WebView?>(null) }

    // Get system insets
    val density = LocalDensity.current
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // Handle back press
    BackHandler(enabled = canGoBack || showMenu) {
        if (showMenu) {
            showMenu = false
        } else {
            webView?.goBack()
        }
    }

    // URL processing function
    fun processUrl(input: String): String {
        val trimmed = input.trim()
        return when {
            !trimmed.contains('.') || trimmed.contains(' ') -> {
                "https://www.google.com/search?q=${android.net.Uri.encode(trimmed)}"
            }
            !trimmed.startsWith("http://") && !trimmed.startsWith("https://") -> {
                "https://$trimmed"
            }
            else -> trimmed
        }
    }

    // Gunakan Box sebagai root untuk menghindari masalah scope AnimatedVisibility
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Area Konten Utama (WebView + Spacers)
        Column(modifier = Modifier.fillMaxSize()) {
            // Kotak Spacer Atas (Transparan mengikuti background root)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(statusBarPadding)
            )

            // WebView di area tengah
            Box(modifier = Modifier.weight(1f)) {
                BrowserWebView(
                    modifier = Modifier.fillMaxSize(),
                    url = currentUrl,
                    onUrlChange = { url -> currentUrl = url },
                    onLoadingChange = { loading -> isLoading = loading },
                    onProgressChange = { progress -> loadingProgress = progress },
                    onCanGoBackChange = { canBack -> canGoBack = canBack },
                    onCanGoForwardChange = { canForward -> canGoForward = canForward },
                    onScrollDirectionChange = { direction ->
                        when (direction) {
                            ScrollDirection.DOWN -> {
                                if (!isNavBarCollapsed && !isUrlEditingMode && !showMenu) {
                                    isNavBarCollapsed = true
                                }
                            }
                            ScrollDirection.UP -> {
                                if (isNavBarCollapsed) {
                                    isNavBarCollapsed = false
                                }
                            }
                        }
                    },
                    webViewRef = { wv -> webView = wv }
                )
            }

            // Kotak Spacer Bawah (Transparan mengikuti background root)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(navBarPadding)
            )
        }

        // Layer UI Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            // Loading Progress (Top)
            AnimatedVisibility(
                visible = isLoading && !isNavBarCollapsed,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .align(Alignment.TopCenter),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LinearProgressIndicator(
                    progress = { loadingProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = Secondary,
                    trackColor = Color.LightGray.copy(alpha = 0.5f)
                )
            }

            // Bottom Navigation Bar
            AnimatedVisibility(
                visible = !isUrlEditingMode,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(350)
                ) + fadeIn(tween(350)),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(250)
                ) + fadeOut(tween(250))
            ) {
                NavBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = 16.dp),
                    currentUrl = currentUrl,
                    isLoading = isLoading,
                    loadingProgress = loadingProgress,
                    canGoBack = canGoBack,
                    canGoForward = canGoForward,
                    isCollapsed = isNavBarCollapsed,
                    showMenu = showMenu,
                    onBackPressed = { webView?.goBack() },
                    onMenuToggle = { showMenu = !showMenu },
                    onRefresh = { webView?.reload() },
                    onForward = { webView?.goForward() },
                    onUrlBarTap = {
                        if (isNavBarCollapsed) {
                            isNavBarCollapsed = false
                        }
                        isUrlEditingMode = true
                    },
                    onTapToExpand = { isNavBarCollapsed = false }
                )
            }

            // FAB Menu Overlay - positioned outside navbar
            FabMenuOverlay(
                modifier = Modifier
                    .navigationBarsPadding(),
                expanded = showMenu && !isNavBarCollapsed && !isUrlEditingMode,
                canGoForward = canGoForward,
                onToggle = { showMenu = false },
                onRefresh = { webView?.reload() },
                onForward = { webView?.goForward() }
            )
        }

        // URL Editing Overlay
        if (isUrlEditingMode) {
            UrlEditingOverlay(
                currentUrl = currentUrl,
                isLoading = isLoading,
                isVisible = isUrlEditingMode,
                statusBarHeight = with(density) { statusBarPadding.toPx() },
                onUrlSubmitted = { url ->
                    val processedUrl = processUrl(url)
                    currentUrl = processedUrl
                    webView?.loadUrl(processedUrl)
                    isUrlEditingMode = false
                },
                onClose = { isUrlEditingMode = false }
            )
        }
    }
}
