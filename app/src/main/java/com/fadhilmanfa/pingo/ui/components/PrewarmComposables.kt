package com.fadhilmanfa.pingo.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.fadhilmanfa.pingo.data.BookmarkItem
import com.fadhilmanfa.pingo.data.TabItem

/**
 * Pre-warms (pre-composes) heavy UI components during the initial loading phase. This forces
 * Compose to cache the composition and layout calculations ahead of time, making subsequent
 * animations and displays much smoother.
 *
 * The components are rendered with alpha = 0 and minimal size so they're invisible to the user but
 * still trigger Compose's internal caching mechanisms.
 */
@Composable
fun PrewarmComposables(modifier: Modifier = Modifier) {
    // Invisible container that pre-warms components
    Box(modifier = modifier.alpha(0f).size(1.dp)) {
        // Pre-warm NavBar (expanded state)
        NavBar(
                currentUrl = "https://www.google.com",
                isLoading = false,
                loadingProgress = 0f,
                canGoBack = true,
                canGoForward = true,
                isCollapsed = false,
                showMenu = false,
                tabCount = 1,
                onBackPressed = {},
                onMenuToggle = {},
                onRefresh = {},
                onForward = {},
                onUrlBarTap = {},
                onTapToExpand = {},
                onTabButtonClick = {},
                onSwipeUpToMoveTop = {},
                onSwipeDownToMoveBottom = {}
        )

        // Pre-warm NavBar (collapsed state)
        NavBar(
                currentUrl = "https://www.google.com",
                isLoading = true,
                loadingProgress = 0.5f,
                canGoBack = false,
                canGoForward = false,
                isCollapsed = true,
                showMenu = false,
                tabCount = 1,
                onBackPressed = {},
                onMenuToggle = {},
                onRefresh = {},
                onForward = {},
                onUrlBarTap = {},
                onTapToExpand = {},
                onTabButtonClick = {},
                onSwipeUpToMoveTop = {},
                onSwipeDownToMoveBottom = {}
        )

        // Pre-warm AiNavBar
        AiNavBar(isLoading = false, onClose = {}, onSend = {})

        // Pre-warm AiNavBar (loading state)
        AiNavBar(isLoading = true, onClose = {}, onSend = {})

        // Pre-warm TabCard (used in TabSwitcherBottomSheet)
        TabCard(
                tab =
                        TabItem(
                                id = "prewarm",
                                title = "Prewarm Tab",
                                url = "https://www.google.com",
                                isActive = false
                        ),
                onClick = {},
                onClose = {}
        )

        // Pre-warm BookmarkCard (used in BookmarkBottomSheet)
        BookmarkCard(
                bookmark =
                        BookmarkItem(
                                id = "prewarm",
                                title = "Prewarm Bookmark",
                                url = "https://www.google.com",
                                timestamp = 0L
                        ),
                onClick = {},
                onDelete = {}
        )

        // Pre-warm FabMenuOverlay
        FabMenuOverlay(
                expanded = false,
                canGoForward = true,
                isAtTop = false,
                onToggle = {},
                onRefresh = {},
                onForward = {},
                onPingoAI = {},
                onBookmark = {},
                onHistory = {},
                onDownloads = {}
        )

        // Pre-warm UrlEditingOverlay components (partial, just the TextField patterns)
        // Note: We can't fully pre-warm this as it requires more complex state,
        // but the animation patterns are already cached via NavBar pre-warming
    }
}
