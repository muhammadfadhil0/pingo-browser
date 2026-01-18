package com.fadhilmanfa.pingo.ui.pages

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.OpenInBrowser
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
import androidx.core.os.LocaleListCompat
import com.fadhilmanfa.pingo.R
import kotlinx.coroutines.delay

private data class GeneralMenuItem(
        val icon: ImageVector,
        val label: String,
        val subtitle: String,
        val onClick: () -> Unit
)

private data class LanguageMenuItem(
        val icon: ImageVector,
        val label: String,
        val localeCode: String,
        val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // FabMenu state
    var showFabMenu by remember { mutableStateOf(false) }
    var showLanguageMenu by remember { mutableStateOf(false) }

    // Labels
    val languageLabel = stringResource(R.string.settings_language)
    val languageSubtitle = stringResource(R.string.settings_language_subtitle)
    val defaultBrowserLabel = stringResource(R.string.settings_default_browser)
    val defaultBrowserSubtitle = stringResource(R.string.settings_default_browser_subtitle)
    val backLabel = stringResource(R.string.fab_back)
    val indonesianLabel = stringResource(R.string.language_indonesian)
    val englishLabel = stringResource(R.string.language_english)

    // Main menu items
    val mainMenuItems =
            listOf(
                    GeneralMenuItem(
                            icon = Icons.Rounded.Language,
                            label = languageLabel,
                            subtitle = languageSubtitle,
                            onClick = { showLanguageMenu = true }
                    ),
                    GeneralMenuItem(
                            icon = Icons.Rounded.OpenInBrowser,
                            label = defaultBrowserLabel,
                            subtitle = defaultBrowserSubtitle,
                            onClick = {
                                openDefaultBrowserSettings(context)
                                showFabMenu = false
                            }
                    )
            )

    // Language submenu items
    val languageMenuItems =
            listOf(
                    LanguageMenuItem(
                            icon = Icons.AutoMirrored.Rounded.ArrowBack,
                            label = backLabel,
                            localeCode = "",
                            onClick = { showLanguageMenu = false }
                    ),
                    LanguageMenuItem(
                            icon = Icons.Rounded.Language,
                            label = indonesianLabel,
                            localeCode = "id",
                            onClick = {
                                val appLocale: LocaleListCompat =
                                        LocaleListCompat.forLanguageTags("id")
                                AppCompatDelegate.setApplicationLocales(appLocale)
                                showFabMenu = false
                                showLanguageMenu = false
                            }
                    ),
                    LanguageMenuItem(
                            icon = Icons.Rounded.Language,
                            label = englishLabel,
                            localeCode = "en",
                            onClick = {
                                val appLocale: LocaleListCompat =
                                        LocaleListCompat.forLanguageTags("en")
                                AppCompatDelegate.setApplicationLocales(appLocale)
                                showFabMenu = false
                                showLanguageMenu = false
                            }
                    )
            )

    // Animation states
    var visibleItems by remember { mutableStateOf(setOf<Int>()) }
    var isTransitioning by remember { mutableStateOf(false) }
    var displayedMenu by remember { mutableStateOf(showLanguageMenu) }

    // Handle menu transition with crossfade
    LaunchedEffect(showLanguageMenu) {
        if (showFabMenu && displayedMenu != showLanguageMenu) {
            isTransitioning = true
            visibleItems = emptySet()
            delay(200)
            displayedMenu = showLanguageMenu

            val currentItems = if (showLanguageMenu) languageMenuItems else mainMenuItems
            for (i in currentItems.indices.reversed()) {
                delay(50L)
                visibleItems = visibleItems + i
            }
            isTransitioning = false
        } else if (!showFabMenu) {
            displayedMenu = showLanguageMenu
        }
    }

    // Handle initial expand and collapse
    LaunchedEffect(showFabMenu) {
        if (showFabMenu && !isTransitioning) {
            visibleItems = emptySet()
            displayedMenu = showLanguageMenu
            val currentItems = if (showLanguageMenu) languageMenuItems else mainMenuItems
            for (i in currentItems.indices.reversed()) {
                delay(50L)
                visibleItems = visibleItems + i
            }
        } else if (!showFabMenu) {
            visibleItems = emptySet()
            showLanguageMenu = false
        }
    }

    val displayedItems = if (displayedMenu) languageMenuItems else mainMenuItems

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
                        text = stringResource(R.string.settings_general),
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
                                    showLanguageMenu = false
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
                    // Language menu
                    languageMenuItems.forEachIndexed { index, item ->
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
                        ) {
                            MenuPillWithSubtitle(
                                    icon = item.icon,
                                    label = item.label,
                                    subtitle = item.subtitle,
                                    onClick = item.onClick
                            )
                        }
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
                                else Icons.Rounded.Language,
                        contentDescription = if (showFabMenu) "Close" else "Menu"
                )
            }
        }
    }
}

private fun openDefaultBrowserSettings(context: Context) {
    val alreadyDefaultMsg = context.getString(R.string.msg_default_browser_already)
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (roleManager.isRoleAvailable(RoleManager.ROLE_BROWSER)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_BROWSER)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_BROWSER)
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, alreadyDefaultMsg, Toast.LENGTH_SHORT).show()
                }
            } else {
                val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                context.startActivity(intent)
            }
        } else {
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            context.startActivity(intent)
        }
    } catch (e: Exception) {
        val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        context.startActivity(intent)
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

@Composable
private fun MenuPillWithSubtitle(
        icon: ImageVector,
        label: String,
        subtitle: String,
        onClick: () -> Unit
) {
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
