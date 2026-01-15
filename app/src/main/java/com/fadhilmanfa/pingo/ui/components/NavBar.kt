package com.fadhilmanfa.pingo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.fadhilmanfa.pingo.ui.theme.Secondary
import kotlinx.coroutines.launch

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

        val navBarInteractionSource = remember { MutableInteractionSource() }
        val isPressed by navBarInteractionSource.collectIsPressedAsState()
        val scope = rememberCoroutineScope()

        val horizontalOffset = remember { Animatable(0f) }

        // M3 Motion Physics (Stabil)
        val m3FastSpring =
                spring<Float>(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessHigh
                )
        val m3StandardSpring =
                spring<Float>(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                )

        // Bouncy spring for horizontal swipe - lebih mulus dan bouncy
        val bouncySpring =
                spring<Float>(
                        dampingRatio = 0.55f, // Lebih bouncy (lower = more bounce)
                        stiffness = 400f // Lebih lembut (lower = smoother)
                )

        // Extra bouncy untuk efek "snap back"
        val snapBackSpring = spring<Float>(dampingRatio = 0.5f, stiffness = 300f)

        val navBarScale by
                animateFloatAsState(
                        targetValue = if (isPressed) 0.96f else 1f,
                        animationSpec = m3FastSpring,
                        label = "navBarScale"
                )

        val heightAnim by
                animateDpAsState(
                        targetValue = if (isCollapsed) 36.dp else 60.dp,
                        animationSpec =
                                spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMediumLow
                                ),
                        label = "height"
                )
        val widthFraction by
                animateFloatAsState(
                        targetValue = if (isCollapsed) 0.45f else 1f,
                        animationSpec = m3StandardSpring,
                        label = "width"
                )

        val expandedAlpha by
                animateFloatAsState(
                        targetValue = if (isCollapsed) 0f else 1f,
                        animationSpec = m3StandardSpring,
                        label = "expandedAlpha"
                )
        val collapsedAlpha by
                animateFloatAsState(
                        targetValue = if (isCollapsed) 1f else 0f,
                        animationSpec = m3StandardSpring,
                        label = "collapsedAlpha"
                )

        Box(
                modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
        ) {
                Surface(
                        modifier =
                                Modifier.fillMaxWidth(widthFraction)
                                        .height(heightAnim)
                                        .scale(navBarScale)
                                        .graphicsLayer { translationX = horizontalOffset.value }
                                        .shadow(4.dp, RoundedCornerShape(50.dp))
                                        .pointerInput(isCollapsed, canGoBack, canGoForward) {
                                                var dragX = 0f
                                                var dragY = 0f
                                                val maxDrag =
                                                        150f // Max drag distance untuk resistance

                                                detectDragGestures(
                                                        onDragEnd = {
                                                                // Tentukan apakah gesture lebih
                                                                // dominan horizontal
                                                                // atau vertikal
                                                                val isHorizontalGesture =
                                                                        kotlin.math.abs(dragX) >
                                                                                kotlin.math.abs(
                                                                                        dragY
                                                                                )

                                                                // Vertical swipe handling - hanya
                                                                // jika gesture
                                                                // dominan vertikal
                                                                // Ini mencegah navbar pindah posisi
                                                                // saat user
                                                                // bermaksud swipe horizontal
                                                                // (back/forward)
                                                                if (!isHorizontalGesture) {
                                                                        if (dragY < -100f)
                                                                                onSwipeUpToMoveTop()
                                                                        else if (dragY > 100f)
                                                                                onSwipeDownToMoveBottom()
                                                                }

                                                                // Horizontal navigation dengan
                                                                // bounce animation
                                                                val triggered =
                                                                        when {
                                                                                dragX > 80f &&
                                                                                        canGoBack -> {
                                                                                        onBackPressed()
                                                                                        true
                                                                                }
                                                                                dragX < -80f &&
                                                                                        canGoForward -> {
                                                                                        onForward()
                                                                                        true
                                                                                }
                                                                                else -> false
                                                                        }

                                                                scope.launch {
                                                                        // Gunakan initialVelocity
                                                                        // untuk bounce yang
                                                                        // natural tanpa jeda
                                                                        // Velocity tinggi = bounce
                                                                        // lebih besar dan
                                                                        // smooth
                                                                        val velocity =
                                                                                if (triggered) {
                                                                                        // Velocity
                                                                                        // berlawanan arah drag
                                                                                        // untuk
                                                                                        // efek
                                                                                        // "snap"
                                                                                        if (dragX >
                                                                                                        0
                                                                                        )
                                                                                                -2000f
                                                                                        else 2000f
                                                                                } else {
                                                                                        // Velocity
                                                                                        // mengikuti
                                                                                        // arah drag
                                                                                        // untuk
                                                                                        // return
                                                                                        // yang
                                                                                        // natural
                                                                                        if (dragX >
                                                                                                        0
                                                                                        )
                                                                                                -800f
                                                                                        else 800f
                                                                                }

                                                                        horizontalOffset.animateTo(
                                                                                targetValue = 0f,
                                                                                animationSpec =
                                                                                        spring(
                                                                                                dampingRatio =
                                                                                                        0.45f, // Cukup
                                                                                                // bouncy
                                                                                                // tapi
                                                                                                // controlled
                                                                                                stiffness =
                                                                                                        350f // Responsive tapi smooth
                                                                                        ),
                                                                                initialVelocity =
                                                                                        velocity
                                                                        )
                                                                }
                                                                dragX = 0f
                                                                dragY = 0f
                                                        },
                                                        onDrag = { change, dragAmount ->
                                                                change.consume()
                                                                dragX += dragAmount.x
                                                                dragY += dragAmount.y

                                                                // Resistance yang smooth - semakin
                                                                // jauh drag
                                                                // semakin berat
                                                                // Menggunakan formula easing dengan
                                                                // resistance
                                                                val resistance =
                                                                        1f -
                                                                                (kotlin.math.abs(
                                                                                                dragX
                                                                                        ) /
                                                                                                (maxDrag *
                                                                                                        3f))
                                                                                        .coerceIn(
                                                                                                0f,
                                                                                                0.6f
                                                                                        )
                                                                val visualOffset =
                                                                        dragX * 0.5f * resistance

                                                                scope.launch {
                                                                        // Gunakan animateTo dengan
                                                                        // spring cepat untuk
                                                                        // smoothness
                                                                        horizontalOffset.animateTo(
                                                                                targetValue =
                                                                                        visualOffset,
                                                                                animationSpec =
                                                                                        spring(
                                                                                                dampingRatio =
                                                                                                        1f, // No bounce
                                                                                                // saat drag
                                                                                                stiffness =
                                                                                                        2000f // Responsive
                                                                                        )
                                                                        )
                                                                }
                                                        }
                                                )
                                        },
                        shape = RoundedCornerShape(50.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border =
                                BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                                // KONTEN EXPANDED
                                Row(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .padding(horizontal = 8.dp)
                                                        .align(Alignment.Center)
                                                        .graphicsLayer {
                                                                alpha = expandedAlpha
                                                                translationX =
                                                                        if (isCollapsed) 1000f
                                                                        else 0f
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
                                                modifier =
                                                        Modifier.weight(1f)
                                                                .padding(horizontal = 8.dp),
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
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .align(Alignment.Center)
                                                        .graphicsLayer { alpha = collapsedAlpha },
                                        contentAlignment = Alignment.Center
                                ) {
                                        CollapsedUrlDisplay(
                                                domainName = domainName,
                                                faviconUrl = faviconUrl,
                                                isLoading = isLoading
                                        )
                                }

                                if (isCollapsed) {
                                        Box(
                                                modifier =
                                                        Modifier.fillMaxSize()
                                                                .clickable(
                                                                        interactionSource =
                                                                                navBarInteractionSource,
                                                                        indication = null,
                                                                        onClick = onTapToExpand
                                                                )
                                        )
                                }

                                if (isCollapsed && isLoading) {
                                        LinearProgressIndicator(
                                                progress = { loadingProgress },
                                                modifier =
                                                        Modifier.align(Alignment.BottomCenter)
                                                                .fillMaxWidth()
                                                                .height(2.5.dp)
                                                                .clip(
                                                                        RoundedCornerShape(
                                                                                bottomStart = 50.dp,
                                                                                bottomEnd = 50.dp
                                                                        )
                                                                ),
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
                modifier =
                        modifier.size(44.dp)
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
                        tint =
                                if (enabled) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
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
                modifier =
                        Modifier.size(44.dp)
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
                        border =
                                BorderStroke(
                                        1.2.dp,
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.6f
                                        )
                                )
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
                modifier =
                        modifier.height(42.dp)
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
                        // Loader di posisi favicon saat loading
                        if (isLoading) {
                                CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Secondary,
                                        strokeWidth = 2.dp
                                )
                        } else if (faviconUrl.isNotEmpty()) {
                                val painter = rememberAsyncImagePainter(model = faviconUrl)
                                val painterState = painter.state

                                if (painterState is AsyncImagePainter.State.Error ||
                                                painterState is AsyncImagePainter.State.Empty
                                ) {
                                        // Fallback to globe icon when favicon fails
                                        Icon(
                                                imageVector = Icons.Rounded.Language,
                                                contentDescription = null,
                                                tint =
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                                .copy(alpha = 0.7f),
                                                modifier = Modifier.size(16.dp)
                                        )
                                } else {
                                        AsyncImage(
                                                model = faviconUrl,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp).clip(CircleShape),
                                                contentScale = ContentScale.Fit
                                        )
                                }
                        } else {
                                Icon(
                                        imageVector = Icons.Rounded.Language,
                                        contentDescription = null,
                                        tint =
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                        alpha = 0.7f
                                                ),
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
                }
        }
}

@Composable
private fun CollapsedUrlDisplay(domainName: String, faviconUrl: String, isLoading: Boolean) {
        Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
        ) {
                // Loader di posisi favicon saat loading
                if (isLoading) {
                        CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = Secondary,
                                strokeWidth = 2.dp
                        )
                } else if (faviconUrl.isNotEmpty()) {
                        val painter = rememberAsyncImagePainter(model = faviconUrl)
                        val painterState = painter.state

                        if (painterState is AsyncImagePainter.State.Error ||
                                        painterState is AsyncImagePainter.State.Empty
                        ) {
                                // Fallback to globe icon when favicon fails
                                Icon(
                                        imageVector = Icons.Rounded.Language,
                                        contentDescription = null,
                                        tint =
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                        alpha = 0.7f
                                                ),
                                        modifier = Modifier.size(14.dp)
                                )
                        } else {
                                AsyncImage(
                                        model = faviconUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp).clip(CircleShape),
                                        contentScale = ContentScale.Fit
                                )
                        }
                } else {
                        // Fallback to globe icon when no favicon URL
                        Icon(
                                imageVector = Icons.Rounded.Language,
                                contentDescription = null,
                                tint =
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.7f
                                        ),
                                modifier = Modifier.size(14.dp)
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
        }
}
