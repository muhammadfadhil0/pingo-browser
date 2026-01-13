package com.fadhilmanfa.pingo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fadhilmanfa.pingo.ui.theme.GreyBorder
import com.fadhilmanfa.pingo.ui.theme.TextSecondary
import kotlinx.coroutines.delay

private data class FabMenuItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true
)

/**
 * FabMenuOverlay with staggered fade animation from bottom to top.
 * Pills match the navbar styling (white background with grey border).
 */
@Composable
fun FabMenuOverlay(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    canGoForward: Boolean,
    onToggle: () -> Unit,
    onRefresh: () -> Unit,
    onForward: () -> Unit,
    onBookmark: () -> Unit = {},
    onHistory: () -> Unit = {},
    onDownloads: () -> Unit = {},
    onSettings: () -> Unit = {}
) {
    val menuItems = listOf(
        FabMenuItem(Icons.Rounded.Settings, "Pengaturan", { onSettings(); onToggle() }),
        FabMenuItem(Icons.Rounded.Download, "Unduhan", { onDownloads(); onToggle() }),
        FabMenuItem(Icons.Rounded.History, "Riwayat", { onHistory(); onToggle() }),
        FabMenuItem(Icons.Rounded.BookmarkBorder, "Bookmark", { onBookmark(); onToggle() }),
        FabMenuItem(Icons.AutoMirrored.Rounded.ArrowForward, "Maju", { if (canGoForward) { onForward(); onToggle() } }, canGoForward),
        FabMenuItem(Icons.Rounded.Refresh, "Refresh", { onRefresh(); onToggle() })
    )

    // Track visibility state for each item
    var visibleItems by remember { mutableStateOf(setOf<Int>()) }

    // Staggered animation effect
    LaunchedEffect(expanded) {
        if (expanded) {
            // Show items one by one from bottom (last item) to top (first item)
            for (i in menuItems.indices.reversed()) {
                delay(50L) // 50ms delay between each item
                visibleItems = visibleItems + i
            }
        } else {
            // Hide all items quickly
            visibleItems = emptySet()
        }
    }

    // Backdrop to close menu when clicking outside
    if (expanded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onToggle() }
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        Column(
            modifier = Modifier.padding(end = 24.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            menuItems.forEachIndexed { index, item ->
                AnimatedVisibility(
                    visible = expanded && index in visibleItems,
                    enter = fadeIn(
                        animationSpec = tween(200)
                    ) + slideInVertically(
                        initialOffsetY = { it / 2 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ),
                    exit = fadeOut(animationSpec = tween(150)) + slideOutVertically(
                        targetOffsetY = { it / 2 },
                        animationSpec = tween(150)
                    )
                ) {
                    MenuPill(
                        icon = item.icon,
                        label = item.label,
                        onClick = item.onClick,
                        enabled = item.enabled
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "pillScale"
    )

    Surface(
        modifier = Modifier
            .scale(scale)
            .shadow(8.dp, RoundedCornerShape(25.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        shape = RoundedCornerShape(25.dp),
        color = Color.White,
        border = BorderStroke(1.dp, GreyBorder.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) TextSecondary else TextSecondary.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                color = if (enabled) TextSecondary else TextSecondary.copy(alpha = 0.4f)
            )
        }
    }
}
