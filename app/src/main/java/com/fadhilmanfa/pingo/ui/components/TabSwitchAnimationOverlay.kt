package com.fadhilmanfa.pingo.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.fadhilmanfa.pingo.ui.theme.GreyBackground

/**
 * Overlay component that handles the Safari-style hero animation.
 * Shows the screenshot transitioning from fullscreen to its position in the tab grid.
 *
 * @param visible Whether the animation overlay is visible
 * @param screenshot The captured WebView screenshot
 * @param dragProgress Current drag progress (0-1), used for interactive animation
 * @param isAnimatingToGrid Whether we're animating to the final grid position
 * @param targetGridPosition Index position in the grid (0 = top-left, 1 = top-right, etc.)
 * @param onAnimationComplete Called when the animation to grid is complete
 */
@Composable
fun TabSwitchAnimationOverlay(
    visible: Boolean,
    screenshot: Bitmap?,
    dragProgress: Float,
    isAnimatingToGrid: Boolean,
    targetGridPosition: Int = 0,
    onAnimationComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible || screenshot == null) return

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Animation values
    val animatedProgress = remember { Animatable(0f) }

    // When isAnimatingToGrid becomes true, animate to final position
    LaunchedEffect(isAnimatingToGrid) {
        if (isAnimatingToGrid) {
            animatedProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            onAnimationComplete()
        }
    }

    // Reset when not animating to grid
    LaunchedEffect(visible, isAnimatingToGrid) {
        if (visible && !isAnimatingToGrid) {
            animatedProgress.snapTo(0f)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
    ) {
        val screenWidthPx = with(density) { screenWidth.toPx() }
        val screenHeightPx = with(density) { screenHeight.toPx() }

        // Grid calculations (2 columns) to align with TabGridOverlay
        val gridPaddingPx = with(density) { 16.dp.toPx() }
        val gridSpacingPx = with(density) { 12.dp.toPx() }
        val cardWidthPx = (screenWidthPx - gridPaddingPx * 2 - gridSpacingPx) / 2f
        val cardHeightPx = with(density) { 180.dp.toPx() }
        val headerHeightPx = with(density) { 64.dp.toPx() }
        val statusBarHeightPx = with(density) { 48.dp.toPx() } // rough estimate for hero alignment

        // Calculate target center in grid
        val column = targetGridPosition % 2
        val row = targetGridPosition / 2
        val targetCenterX = gridPaddingPx + (cardWidthPx + gridSpacingPx) * column + cardWidthPx / 2f
        val targetCenterY = statusBarHeightPx + headerHeightPx + gridPaddingPx + (cardHeightPx + gridSpacingPx) * row + cardHeightPx / 2f

        // Current animation progress (drag for interactive, spring for final)
        val currentProgress = if (isAnimatingToGrid) animatedProgress.value else dragProgress

        // Scale toward the real card size instead of a fixed 0.5
        val targetScaleX = cardWidthPx / screenWidthPx
        val targetScaleY = cardHeightPx / screenHeightPx
        val currentScaleX = lerpFloat(1f, targetScaleX, currentProgress)
        val currentScaleY = lerpFloat(1f, targetScaleY, currentProgress)

        // Translate center-to-center toward the grid slot
        val translateX = lerpFloat(0f, targetCenterX - screenWidthPx / 2f, currentProgress)
        val translateYBase = lerpFloat(0f, targetCenterY - screenHeightPx / 2f, currentProgress)

        // Small upward lift during interactive drag for a "pulling out" feel
        val dragLift = if (isAnimatingToGrid) 0f else with(density) { 80.dp.toPx() } * (1f - currentProgress)
        val translateY = translateYBase - dragLift

        val cornerRadius = lerpFloat(0f, 18f, currentProgress)
        val shadowPx = lerpFloat(24f, 8f, currentProgress)
        val overlayAlpha = lerpFloat(0f, 0.88f, currentProgress.coerceIn(0f, 1f))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GreyBackground.copy(alpha = overlayAlpha))
        ) {
            Image(
                bitmap = screenshot.asImageBitmap(),
                contentDescription = "Tab Preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = currentScaleX
                        scaleY = currentScaleY
                        translationX = translateX
                        translationY = translateY
                        transformOrigin = TransformOrigin.Center
                        shadowElevation = with(density) { shadowPx }
                    }
                    .clip(RoundedCornerShape(cornerRadius.dp))
            )
        }
    }
}

/**
 * Extension function to capture a screenshot from WebView
 */
fun android.webkit.WebView.captureScreenshot(): Bitmap? {
    return try {
        val width = width.takeIf { it > 0 } ?: return null
        val height = height.takeIf { it > 0 } ?: return null
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        bitmap
    } catch (e: Exception) {
        null
    }
}

private fun lerpFloat(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction
}
