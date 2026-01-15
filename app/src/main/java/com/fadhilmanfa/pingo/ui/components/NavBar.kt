package com.fadhilmanfa.pingo.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fadhilmanfa.pingo.ui.theme.Secondary

private fun extractDomain(url: String): String {
    return try {
        val uri = java.net.URI(url)
        val domain = uri.host ?: url
        domain.removePrefix("www.")
    } catch (e: Exception) {
        url
    }
}

private fun getFaviconUrl(url: String): String {
    return try {
        val uri = java.net.URI(url)
        val domain = uri.host ?: return ""
        "https://www.google.com/s2/favicons?domain=$domain&sz=64"
    } catch (e: Exception) {
        ""
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
    val faviconUrl = remember(currentUrl) { getFaviconUrl(currentUrl) }
    
    // Interaction Source tunggal untuk seluruh NavBar
    val navBarInteractionSource = remember { MutableInteractionSource() }
    val isPressed by navBarInteractionSource.collectIsPressedAsState()
    
    val navBarScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "navBarScale"
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
                .scale(navBarScale) // Seluruh Surface yang kena animasi scale
                .shadow(4.dp, RoundedCornerShape(50.dp))
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
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
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
                        faviconUrl = faviconUrl,
                        isLoading = isLoading,
                        onClick = onUrlBarTap,
                        enabled = !isCollapsed,
                        interactionSource = navBarInteractionSource
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TabCountButton(
                            count = tabCount, 
                            onClick = onTabButtonClick, 
                            enabled = !isCollapsed,
                            interactionSource = navBarInteractionSource
                        )
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
                    CollapsedUrlDisplay(domainName = domainName, faviconUrl = faviconUrl, isLoading = isLoading)
                }

                if (isCollapsed) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = navBarInteractionSource,
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
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled, 
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun TabCountButton(
    count: Int, 
    onClick: () -> Unit, 
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled, 
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(24.dp),
            shape = RoundedCornerShape(6.dp),
            color = Color.Transparent,
            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (count > 99) "99+" else count.toString(),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    faviconUrl: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource
) {
    Surface(
        modifier = modifier
            .height(42.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled, 
                onClick = onClick
            ),
        shape = RoundedCornerShape(21.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (faviconUrl.isNotEmpty()) {
                AsyncImage(
                    model = faviconUrl,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp).clip(CircleShape),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isLoading) {
                Spacer(modifier = Modifier.width(8.dp))
                LoadingIndicator(
                    modifier = Modifier.size(14.dp),
                    color = Secondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CollapsedUrlDisplay(
    domainName: String,
    faviconUrl: String,
    isLoading: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 12.dp)
    ) {
        if (faviconUrl.isNotEmpty()) {
            AsyncImage(
                model = faviconUrl,
                contentDescription = null,
                modifier = Modifier.size(14.dp).clip(CircleShape),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = domainName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (isLoading) {
            Spacer(modifier = Modifier.width(6.dp))
            LoadingIndicator(
                modifier = Modifier.size(12.dp),
                color = Secondary
            )
        }
    }
}
