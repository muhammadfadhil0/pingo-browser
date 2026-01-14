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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fadhilmanfa.pingo.data.TabItem
import com.fadhilmanfa.pingo.ui.theme.GreyBackground
import com.fadhilmanfa.pingo.ui.theme.GreyBorder
import com.fadhilmanfa.pingo.ui.theme.Secondary
import com.fadhilmanfa.pingo.ui.theme.TextPrimary
import com.fadhilmanfa.pingo.ui.theme.TextSecondary

/**
 * Full-screen overlay for displaying tabs in a 2-column grid layout.
 * Supports swipe-down gesture to close.
 */
@Composable
fun TabGridOverlay(
    visible: Boolean,
    tabs: List<TabItem>,
    onClose: () -> Unit,
    onTabClick: (TabItem) -> Unit,
    onTabClose: (TabItem) -> Unit,
    onNewTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val dismissThreshold = 200f

    // Calculate alpha and scale based on drag
    val dragProgress = (dragOffset / dismissThreshold).coerceIn(0f, 1f)
    val overlayAlpha by animateFloatAsState(
        targetValue = if (visible) 1f - (dragProgress * 0.3f) else 0f,
        animationSpec = tween(200),
        label = "overlayAlpha"
    )
    val overlayScale by animateFloatAsState(
        targetValue = if (visible) 1f - (dragProgress * 0.05f) else 0.95f,
        animationSpec = tween(200),
        label = "overlayScale"
    )

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + fadeIn(tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(250)
        ) + fadeOut(tween(200))
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(GreyBackground)
                .scale(overlayScale)
                .alpha(overlayAlpha)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (dragOffset > dismissThreshold) {
                                onClose()
                            }
                            dragOffset = 0f
                        },
                        onDragCancel = {
                            dragOffset = 0f
                        },
                        onVerticalDrag = { _, dragAmount ->
                            // Only track downward drag
                            if (dragAmount > 0 || dragOffset > 0) {
                                dragOffset = (dragOffset + dragAmount).coerceAtLeast(0f)
                            }
                        }
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Header
                TabGridHeader(
                    tabCount = tabs.size,
                    onClose = onClose,
                    onNewTab = onNewTab
                )

                // Tab Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = tabs,
                        key = { it.id }
                    ) { tab ->
                        TabCard(
                            tab = tab,
                            onClick = { onTabClick(tab) },
                            onClose = { onTabClose(tab) },
                            modifier = Modifier.height(180.dp)
                        )
                    }
                }
            }

            // Drag indicator pill at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .statusBarsPadding(),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(GreyBorder, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

@Composable
private fun TabGridHeader(
    tabCount: Int,
    onClose: () -> Unit,
    onNewTab: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Close button
        HeaderButton(
            icon = Icons.Rounded.Close,
            contentDescription = "Tutup",
            onClick = onClose
        )

        // Tab count
        Text(
            text = "$tabCount Tab",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )

        // New tab button
        HeaderButton(
            icon = Icons.Rounded.Add,
            contentDescription = "Tab Baru",
            onClick = onNewTab
        )
    }
}

@Composable
private fun HeaderButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "headerButtonScale"
    )

    Surface(
        modifier = Modifier
            .size(40.dp)
            .scale(scale)
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, GreyBorder.copy(alpha = 0.5f))
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
