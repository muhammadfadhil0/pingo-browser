package com.fadhilmanfa.pingo.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fadhilmanfa.pingo.ui.theme.GreenSecure
import com.fadhilmanfa.pingo.ui.theme.GreyBorder
import com.fadhilmanfa.pingo.ui.theme.Secondary
import com.fadhilmanfa.pingo.ui.theme.TextSecondary

private fun extractDomain(url: String): String {
    return try {
        val uri = java.net.URI(url)
        val domain = uri.host ?: url
        domain.removePrefix("www.")
    } catch (e: Exception) {
        url
    }
}

@Composable
fun NavBar(
    modifier: Modifier = Modifier,
    currentUrl: String,
    isLoading: Boolean,
    loadingProgress: Float = 0f,
    canGoBack: Boolean,
    canGoForward: Boolean,
    isCollapsed: Boolean,
    showMenu: Boolean,
    tabCount: Int = 1,
    onBackPressed: () -> Unit,
    onMenuToggle: () -> Unit,
    onRefresh: () -> Unit,
    onForward: () -> Unit,
    onUrlBarTap: () -> Unit,
    onTapToExpand: () -> Unit,
    onTabButtonClick: () -> Unit = {},
    onSwipeUpToMoveTop: () -> Unit = {},
    onSwipeDownToMoveBottom: () -> Unit = {}
) {
    val domainName = remember(currentUrl) { extractDomain(currentUrl) }
    val navBarInteractionSource = remember { MutableInteractionSource() }
    val isNavBarPressed by navBarInteractionSource.collectIsPressedAsState()
    
    val navBarScale by animateFloatAsState(
        targetValue = if (isNavBarPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "scale"
    )

    val heightAnim by animateDpAsState(
        targetValue = if (isCollapsed) 36.dp else 60.dp,
        animationSpec = tween(350),
        label = "height"
    )
    val widthFraction by animateFloatAsState(
        targetValue = if (isCollapsed) 0.45f else 1f,
        animationSpec = tween(350),
        label = "width"
    )
    
    val expandedAlpha by animateFloatAsState(
        targetValue = if (isCollapsed) 0f else 1f,
        animationSpec = tween(250),
        label = "expandedAlpha"
    )
    val collapsedAlpha by animateFloatAsState(
        targetValue = if (isCollapsed) 1f else 0f,
        animationSpec = tween(250),
        label = "collapsedAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .height(heightAnim)
                .scale(navBarScale)
                .shadow(4.dp, RoundedCornerShape(50.dp)) // Reduced shadow for performance
                .pointerInput(isCollapsed) {
                    if (!isCollapsed) {
                        var dragSum = 0f
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (dragSum < -100f) onSwipeUpToMoveTop()
                                else if (dragSum > 100f) onSwipeDownToMoveBottom()
                                dragSum = 0f
                            },
                            onVerticalDrag = { _, dragAmount -> dragSum += dragAmount }
                        )
                    }
                },
            shape = RoundedCornerShape(50.dp),
            color = Color.White,
            border = BorderStroke(1.dp, GreyBorder.copy(alpha = 0.5f))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // KONTEN EXPANDED
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .align(Alignment.Center)
                        .graphicsLayer { 
                            alpha = expandedAlpha
                            // Disable hit testing when collapsed
                            translationX = if (isCollapsed) 1000f else 0f 
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    NavButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBackIos,
                        onClick = onBackPressed,
                        enabled = canGoBack,
                        contentDescription = "Back",
                        interactionSource = navBarInteractionSource
                    )

                    UrlBarDisplay(
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        text = domainName,
                        isLoading = isLoading,
                        onClick = onUrlBarTap,
                        enabled = !isCollapsed // PENTING: Matikan klik internal jika collapsed
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TabCountButton(count = tabCount, onClick = onTabButtonClick, enabled = !isCollapsed)
                        Spacer(modifier = Modifier.width(4.dp))
                        NavButton(
                            icon = Icons.Rounded.MoreVert,
                            onClick = onMenuToggle,
                            enabled = true,
                            contentDescription = "Menu",
                            interactionSource = navBarInteractionSource
                        )
                    }
                }

                // KONTEN COLLAPSED
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .graphicsLayer { alpha = collapsedAlpha },
                    contentAlignment = Alignment.Center
                ) {
                    CollapsedUrlDisplay(domainName = domainName, isLoading = isLoading)
                }

                // LAPISAN KLIK KHUSUS COLLAPSED (PENTING!)
                // Muncul hanya saat collapsed untuk menangkap tap di seluruh area bar
                if (isCollapsed) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onTapToExpand
                            )
                    )
                }

                if (isCollapsed && isLoading) {
                    LinearProgressIndicator(
                        progress = { loadingProgress },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(2.5.dp)
                            .clip(RoundedCornerShape(bottomStart = 50.dp, bottomEnd = 50.dp)),
                        color = Secondary,
                        trackColor = Color.Transparent
                    )
                }
            }
        }
    }
}

@Composable
private fun NavButton(
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean,
    contentDescription: String,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) TextSecondary.copy(alpha = 0.7f) else TextSecondary.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun TabCountButton(count: Int, onClick: () -> Unit, enabled: Boolean = true) {
    Box(
        modifier = Modifier.size(44.dp).clip(CircleShape).clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(24.dp),
            shape = RoundedCornerShape(6.dp),
            color = Color.Transparent,
            border = BorderStroke(1.2.dp, TextSecondary.copy(alpha = 0.6f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (count > 99) "99+" else count.toString(),
                    fontSize = 10.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun UrlBarDisplay(
    modifier: Modifier = Modifier,
    text: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        modifier = modifier
            .height(42.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(21.dp),
        color = GreyBorder.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, GreyBorder.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                LoadingIndicator(Modifier.size(18.dp), color = Secondary)
            } else {
                Icon(Icons.Rounded.Lock, null, tint = GreenSecure, modifier = Modifier.size(14.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, fontSize = 13.sp, color = TextSecondary, maxLines = 1, modifier = Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CollapsedUrlDisplay(domainName: String, isLoading: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isLoading) {
            LoadingIndicator(Modifier.size(14.dp), color = Secondary)
        } else {
            Icon(Icons.Rounded.Lock, null, tint = GreenSecure, modifier = Modifier.size(10.dp))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(domainName, fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
    }
}
