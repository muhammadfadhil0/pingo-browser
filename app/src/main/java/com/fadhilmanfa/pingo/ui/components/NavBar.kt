package com.fadhilmanfa.pingo.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fadhilmanfa.pingo.ui.theme.GreenSecure
import com.fadhilmanfa.pingo.ui.theme.GreyBorder
import com.fadhilmanfa.pingo.ui.theme.Secondary
import com.fadhilmanfa.pingo.ui.theme.TextSecondary

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
    onBackPressed: () -> Unit,
    onMenuToggle: () -> Unit,
    onRefresh: () -> Unit,
    onForward: () -> Unit,
    onUrlBarTap: () -> Unit,
    onTapToExpand: () -> Unit
) {
    val domainName = extractDomain(currentUrl)

    val navBarInteractionSource = remember { MutableInteractionSource() }
    val isNavBarPressed by navBarInteractionSource.collectIsPressedAsState()
    val navBarScale by animateFloatAsState(
        targetValue = if (isNavBarPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "navBarScale"
    )

    val transition = updateTransition(targetState = isCollapsed, label = "NavBarTransition")
    
    val heightAnim by transition.animateDp(
        transitionSpec = { tween(350) },
        label = "height"
    ) { if (it) 36.dp else 60.dp }

    val widthFraction by transition.animateFloat(
        transitionSpec = { tween(350) },
        label = "width"
    ) { if (it) 0.45f else 1f }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .height(heightAnim)
                .scale(navBarScale)
                .shadow(10.dp, RoundedCornerShape(50.dp))
                .clickable(
                    enabled = isCollapsed,
                    interactionSource = if (isCollapsed) navBarInteractionSource else remember { MutableInteractionSource() },
                    indication = null
                ) { onTapToExpand() },
            shape = RoundedCornerShape(50.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, GreyBorder.copy(alpha = 0.5f))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AnimatedVisibility(
                        visible = !isCollapsed,
                        enter = fadeIn(tween(250)),
                        exit = fadeOut(tween(200))
                    ) {
                        NavButton(
                            icon = Icons.AutoMirrored.Rounded.ArrowBackIos,
                            onClick = onBackPressed,
                            enabled = canGoBack,
                            contentDescription = "Kembali",
                            navBarInteractionSource = navBarInteractionSource
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = if (isCollapsed) 4.dp else 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = isCollapsed,
                            transitionSpec = {
                                (fadeIn(tween(300)) togetherWith fadeOut(tween(300)))
                                    .using(SizeTransform(clip = false))
                            },
                            label = "UrlDisplay"
                        ) { collapsedState ->
                            if (collapsedState) {
                                CollapsedUrlDisplay(
                                    domainName = domainName,
                                    isLoading = isLoading
                                )
                            } else {
                                UrlBarDisplay(
                                    text = currentUrl,
                                    isLoading = isLoading,
                                    interactionSource = navBarInteractionSource,
                                    onClick = onUrlBarTap
                                )
                            }
                        }
                    }

                    // Menu button - positioned inside navbar, vertically centered
                    AnimatedVisibility(
                        visible = !isCollapsed,
                        enter = fadeIn(tween(250)),
                        exit = fadeOut(tween(200))
                    ) {
                        NavButton(
                            icon = Icons.Rounded.MoreVert,
                            onClick = onMenuToggle,
                            enabled = true,
                            contentDescription = "Menu",
                            navBarInteractionSource = navBarInteractionSource
                        )
                    }
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
    navBarInteractionSource: MutableInteractionSource,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = navBarInteractionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (enabled) TextSecondary else TextSecondary.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CollapsedUrlDisplay(
    domainName: String,
    isLoading: Boolean
) {
    Box(
        modifier = Modifier
            .height(26.dp)
            .background(Color.White, RoundedCornerShape(13.dp))
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 2.dp,
                    color = Secondary
                )
                Spacer(modifier = Modifier.width(6.dp))
            } else {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = "Secure",
                    tint = GreenSecure,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = domainName,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun UrlBarDisplay(
    text: String,
    isLoading: Boolean,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit
) {
    val domainName = extractDomain(text)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(25.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, GreyBorder.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = "Secure",
                tint = GreenSecure,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = domainName,
                fontSize = 14.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Secondary
                )
            }
        }
    }
}

private fun extractDomain(url: String): String {
    return try {
        val uri = android.net.Uri.parse(url)
        var host = uri.host ?: url
        
        if (host.contains("google.com") || host.contains("google.co.")) {
            val path = uri.path ?: ""
            if (path.startsWith("/amp/s/")) {
                val ampPath = path.removePrefix("/amp/s/")
                val actualDomain = ampPath.split("/").firstOrNull() ?: host
                host = actualDomain
            }
        }
        
        if (host.startsWith("www.")) {
            host = host.substring(4)
        }
        host
    } catch (e: Exception) {
        url
    }
}
