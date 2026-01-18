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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fadhilmanfa.pingo.R
import com.fadhilmanfa.pingo.ui.theme.Secondary
import kotlinx.coroutines.delay

private data class SettingsMenuItem(
        val icon: ImageVector,
        val label: String,
        val subtitle: String,
        val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
        onBack: () -> Unit,
        onNavigateToAdBlocker: () -> Unit,
        onNavigateToGeneral: () -> Unit,
        onNavigateToAppearance: () -> Unit,
        onNavigateToStartPage: () -> Unit,
        onNavigateToPrivacy: () -> Unit,
        onNavigateToSearch: () -> Unit,
        onNavigateToNotifications: () -> Unit,
        onNavigateToDownloadSettings: () -> Unit,
        onNavigateToHelp: () -> Unit,
        onNavigateToAbout: () -> Unit,
        currentTheme: String,
        onThemeChanged: (String) -> Unit
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // Menu items data
    val menuItems =
            listOf(
                    SettingsMenuItem(
                            icon = Icons.Rounded.Language,
                            label = stringResource(R.string.settings_general),
                            subtitle = stringResource(R.string.settings_general_subtitle),
                            onClick = onNavigateToGeneral
                    ),
                    SettingsMenuItem(
                            icon = Icons.Rounded.Palette,
                            label = stringResource(R.string.settings_appearance),
                            subtitle = stringResource(R.string.settings_appearance_subtitle),
                            onClick = onNavigateToAppearance
                    ),
                    SettingsMenuItem(
                            icon = Icons.Rounded.Home,
                            label = stringResource(R.string.settings_start_page),
                            subtitle = stringResource(R.string.settings_start_page_subtitle),
                            onClick = onNavigateToStartPage
                    ),
                    SettingsMenuItem(
                            icon = Icons.Rounded.PrivacyTip,
                            label = stringResource(R.string.settings_privacy),
                            subtitle = stringResource(R.string.settings_privacy_subtitle),
                            onClick = onNavigateToPrivacy
                    ),
                    SettingsMenuItem(
                            icon = Icons.Rounded.Search,
                            label = stringResource(R.string.settings_search),
                            subtitle = stringResource(R.string.settings_search_subtitle),
                            onClick = onNavigateToSearch
                    ),
                    SettingsMenuItem(
                            icon = Icons.Rounded.Notifications,
                            label = stringResource(R.string.settings_notifications),
                            subtitle = stringResource(R.string.settings_notifications_subtitle),
                            onClick = onNavigateToNotifications
                    ),
                    SettingsMenuItem(
                            icon = Icons.Rounded.Download,
                            label = stringResource(R.string.settings_downloads),
                            subtitle = stringResource(R.string.settings_downloads_subtitle),
                            onClick = onNavigateToDownloadSettings
                    ),
                    SettingsMenuItem(
                            icon = Icons.AutoMirrored.Rounded.HelpOutline,
                            label = stringResource(R.string.settings_help),
                            subtitle = stringResource(R.string.settings_help_subtitle),
                            onClick = onNavigateToHelp
                    ),
                    SettingsMenuItem(
                            icon = Icons.Rounded.Info,
                            label = stringResource(R.string.settings_about),
                            subtitle = stringResource(R.string.settings_about_subtitle),
                            onClick = onNavigateToAbout
                    )
            )

    // Animation state for staggered appearance
    var visibleItems by remember { mutableStateOf(setOf<Int>()) }

    LaunchedEffect(Unit) {
        for (i in menuItems.indices) {
            delay(50L)
            visibleItems = visibleItems + i
        }
    }

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
                    text = stringResource(R.string.settings_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Menu Items with FabMenu-style pills
        Column(
                modifier =
                        Modifier.weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .padding(bottom = navBarPadding + 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            menuItems.forEachIndexed { index, item ->
                AnimatedVisibility(
                        visible = index in visibleItems,
                        enter =
                                fadeIn(animationSpec = tween(200)) +
                                        slideInVertically(
                                                initialOffsetY = { it / 2 },
                                                animationSpec =
                                                        spring(
                                                                dampingRatio =
                                                                        Spring.DampingRatioMediumBouncy,
                                                                stiffness = Spring.StiffnessMedium
                                                        )
                                        ),
                        exit =
                                fadeOut(animationSpec = tween(150)) +
                                        slideOutVertically(
                                                targetOffsetY = { it / 2 },
                                                animationSpec = tween(150)
                                        )
                ) {
                    SettingsMenuPill(
                            icon = item.icon,
                            label = item.label,
                            subtitle = item.subtitle,
                            onClick = item.onClick
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsMenuPill(
        icon: ImageVector,
        label: String,
        subtitle: String,
        onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by
            animateFloatAsState(
                    targetValue = if (isPressed) 0.97f else 1f,
                    animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                    label = "pillScale"
            )

    Surface(
            modifier =
                    Modifier.fillMaxWidth()
                            .scale(scale)
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                            .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = onClick
                            ),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                    modifier =
                            Modifier.size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Secondary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
            ) {
                Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = Secondary,
                        modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = label,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}
