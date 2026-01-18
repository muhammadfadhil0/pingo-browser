package com.fadhilmanfa.pingo.ui.pages

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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fadhilmanfa.pingo.R
import kotlinx.coroutines.delay

private data class AppearanceMenuItem(
        val icon: ImageVector,
        val label: String,
        val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsPage(
        onBack: () -> Unit,
        currentTheme: String,
        onThemeChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // FabMenu state
    var showFabMenu by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }

    // Labels
    val themeLabel = stringResource(R.string.settings_appearance)
    val backLabel = stringResource(R.string.fab_back)
    val systemLabel = stringResource(R.string.theme_system)
    val lightLabel = stringResource(R.string.theme_light)
    val darkLabel = stringResource(R.string.theme_dark)

    // Main menu items
    val mainMenuItems =
            listOf(
                    AppearanceMenuItem(
                            icon = Icons.Rounded.Palette,
                            label = themeLabel,
                            onClick = { showThemeMenu = true }
                    )
            )

    // Theme submenu items
    val themeMenuItems =
            listOf(
                    AppearanceMenuItem(
                            icon = Icons.AutoMirrored.Rounded.ArrowBack,
                            label = backLabel,
                            onClick = { showThemeMenu = false }
                    ),
                    AppearanceMenuItem(
                            icon = Icons.Rounded.Smartphone,
                            label = systemLabel,
                            onClick = {
                                onThemeChanged("system")
                                showFabMenu = false
                                showThemeMenu = false
                            }
                    ),
                    AppearanceMenuItem(
                            icon = Icons.Rounded.WbSunny,
                            label = lightLabel,
                            onClick = {
                                onThemeChanged("light")
                                showFabMenu = false
                                showThemeMenu = false
                            }
                    ),
                    AppearanceMenuItem(
                            icon = Icons.Rounded.DarkMode,
                            label = darkLabel,
                            onClick = {
                                onThemeChanged("dark")
                                showFabMenu = false
                                showThemeMenu = false
                            }
                    )
            )

    // Animation states
    var visibleItems by remember { mutableStateOf(setOf<Int>()) }
    var isTransitioning by remember { mutableStateOf(false) }
    var displayedMenu by remember { mutableStateOf(showThemeMenu) }

    // Handle menu transition with crossfade
    LaunchedEffect(showThemeMenu) {
        if (showFabMenu && displayedMenu != showThemeMenu) {
            isTransitioning = true
            visibleItems = emptySet()
            delay(200)
            displayedMenu = showThemeMenu

            val currentItems = if (showThemeMenu) themeMenuItems else mainMenuItems
            for (i in currentItems.indices.reversed()) {
                delay(50L)
                visibleItems = visibleItems + i
            }
            isTransitioning = false
        } else if (!showFabMenu) {
            displayedMenu = showThemeMenu
        }
    }

    // Handle initial expand and collapse
    LaunchedEffect(showFabMenu) {
        if (showFabMenu && !isTransitioning) {
            visibleItems = emptySet()
            displayedMenu = showThemeMenu
            val currentItems = if (showThemeMenu) themeMenuItems else mainMenuItems
            for (i in currentItems.indices.reversed()) {
                delay(50L)
                visibleItems = visibleItems + i
            }
        } else if (!showFabMenu) {
            visibleItems = emptySet()
            showThemeMenu = false
        }
    }

    val displayedItems = if (displayedMenu) themeMenuItems else mainMenuItems

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(top = statusBarPadding)
        ) {
            // Header
            Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                        text = stringResource(R.string.settings_appearance),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Empty content area - menu is in fab style
            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(bottom = navBarPadding))
        }

        // FAB button to open menu
        Box(
                modifier =
                        Modifier.fillMaxSize()
                                .navigationBarsPadding()
                                .padding(end = 24.dp, bottom = 24.dp),
                contentAlignment = Alignment.BottomEnd
        ) {
            // FAB Menu Overlay (clickable scrim)
            if (showFabMenu) {
                Box(
                        modifier =
                                Modifier.fillMaxSize().clickable(
                                                interactionSource =
                                                        remember { MutableInteractionSource() },
                                                indication = null
                                        ) {
                                    showFabMenu = false
                                    showThemeMenu = false
                                }
                )
            }

            // Menu Pills
            Column(
                    modifier = Modifier.padding(bottom = 72.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (displayedMenu) {
                    // Theme menu
                    themeMenuItems.forEachIndexed { index, item ->
                        AnimatedVisibility(
                                visible = showFabMenu && index in visibleItems,
                                enter =
                                        fadeIn(animationSpec = tween(200)) +
                                                slideInVertically(
                                                        initialOffsetY = { it / 2 },
                                                        animationSpec =
                                                                spring(
                                                                        dampingRatio =
                                                                                Spring.DampingRatioMediumBouncy,
                                                                        stiffness =
                                                                                Spring.StiffnessMedium
                                                                )
                                                ),
                                exit =
                                        fadeOut(animationSpec = tween(150)) +
                                                slideOutVertically(
                                                        targetOffsetY = { it / 2 },
                                                        animationSpec = tween(150)
                                                )
                        ) { MenuPill(icon = item.icon, label = item.label, onClick = item.onClick) }
                    }
                } else {
                    // Main menu
                    mainMenuItems.forEachIndexed { index, item ->
                        AnimatedVisibility(
                                visible = showFabMenu && index in visibleItems,
                                enter =
                                        fadeIn(animationSpec = tween(200)) +
                                                slideInVertically(
                                                        initialOffsetY = { it / 2 },
                                                        animationSpec =
                                                                spring(
                                                                        dampingRatio =
                                                                                Spring.DampingRatioMediumBouncy,
                                                                        stiffness =
                                                                                Spring.StiffnessMedium
                                                                )
                                                ),
                                exit =
                                        fadeOut(animationSpec = tween(150)) +
                                                slideOutVertically(
                                                        targetOffsetY = { it / 2 },
                                                        animationSpec = tween(150)
                                                )
                        ) { MenuPill(icon = item.icon, label = item.label, onClick = item.onClick) }
                    }
                }
            }

            // FAB Button
            FloatingActionButton(
                    onClick = { showFabMenu = !showFabMenu },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                        imageVector =
                                if (showFabMenu) Icons.AutoMirrored.Rounded.ArrowBack
                                else Icons.Rounded.Palette,
                        contentDescription = if (showFabMenu) "Close" else "Menu"
                )
            }
        }
    }
}

@Composable
private fun MenuPill(icon: ImageVector, label: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by
            animateFloatAsState(
                    targetValue = if (isPressed) 0.94f else 1f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                    label = "pillScale"
            )

    Surface(
            modifier =
                    Modifier.scale(scale)
                            .shadow(3.dp, RoundedCornerShape(25.dp))
                            .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = onClick
                            ),
            shape = RoundedCornerShape(25.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
