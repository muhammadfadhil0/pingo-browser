package com.fadhilmanfa.pingo.ui.pages

import android.content.Context
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
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fadhilmanfa.pingo.R
import com.fadhilmanfa.pingo.ui.theme.Secondary
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartPageSettingsPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = remember {
        context.getSharedPreferences("pingo_browser_prefs", Context.MODE_PRIVATE)
    }

    var showSpeedDials by remember {
        mutableStateOf(sharedPrefs.getBoolean("show_speed_dials", true))
    }
    var showRecommendations by remember {
        mutableStateOf(sharedPrefs.getBoolean("show_recommendations", true))
    }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // Animation state
    var visibleItems by remember { mutableStateOf(setOf<Int>()) }

    LaunchedEffect(Unit) {
        for (i in 0..2) {
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
                    text = "Halaman Mulai",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Settings Pills
        Column(
                modifier =
                        Modifier.weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .padding(bottom = navBarPadding + 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Wallpaper Pill
            AnimatedVisibility(
                    visible = 0 in visibleItems,
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
                                    )
            ) {
                StartPageMenuPill(
                        icon = Icons.Rounded.Wallpaper,
                        label = "Wallpaper",
                        subtitle = "Ubah latar belakang halaman mulai",
                        onClick = { /* TODO: Implement Wallpaper sub-page or picker */ }
                )
            }

            // Speed Dials Pill
            AnimatedVisibility(
                    visible = 1 in visibleItems,
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
                                    )
            ) {
                StartPageMenuPill(
                        icon = Icons.Rounded.Star,
                        label = "Speed Dials",
                        subtitle = "Tampilkan situs favorit di awal",
                        trailingContent = {
                            Switch(
                                    checked = showSpeedDials,
                                    onCheckedChange = {
                                        showSpeedDials = it
                                        sharedPrefs.edit().putBoolean("show_speed_dials", it).apply()
                                    },
                                    colors =
                                            SwitchDefaults.colors(
                                                    checkedThumbColor = Color.White,
                                                    checkedTrackColor = Secondary
                                            )
                            )
                        },
                        onClick = {
                            showSpeedDials = !showSpeedDials
                            sharedPrefs.edit().putBoolean("show_speed_dials", showSpeedDials).apply()
                        }
                )
            }

            // Recommendations Pill
            AnimatedVisibility(
                    visible = 2 in visibleItems,
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
                                    )
            ) {
                StartPageMenuPill(
                        icon = Icons.Rounded.Link,
                        label = "Situs Rekomendasi",
                        subtitle = "Tampilkan saran situs populer",
                        trailingContent = {
                            Switch(
                                    checked = showRecommendations,
                                    onCheckedChange = {
                                        showRecommendations = it
                                        sharedPrefs
                                                .edit()
                                                .putBoolean("show_recommendations", it)
                                                .apply()
                                    },
                                    colors =
                                            SwitchDefaults.colors(
                                                    checkedThumbColor = Color.White,
                                                    checkedTrackColor = Secondary
                                            )
                            )
                        },
                        onClick = {
                            showRecommendations = !showRecommendations
                            sharedPrefs
                                    .edit()
                                    .putBoolean("show_recommendations", showRecommendations)
                                    .apply()
                        }
                )
            }
        }
    }
}

@Composable
private fun StartPageMenuPill(
        icon: ImageVector,
        label: String,
        subtitle: String,
        onClick: () -> Unit,
        trailingContent: @Composable (() -> Unit)? = null
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
            if (trailingContent != null) {
                trailingContent()
            } else {
                Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}
