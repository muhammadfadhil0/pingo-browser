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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AdsClick
import androidx.compose.material.icons.rounded.Cookie
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fadhilmanfa.pingo.data.adblock.AdBlockManager
import kotlinx.coroutines.delay

private data class PrivacyMenuItem(
    val icon: ImageVector,
    val label: String,
    val subtitle: String? = null,
    val onClick: () -> Unit
)

@Composable
fun PrivacySettingsPage(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("pingo_settings", Context.MODE_PRIVATE) }
    val adBlockManager = remember { AdBlockManager.getInstance(context) }

    var adBlockerEnabled by remember { mutableStateOf(adBlockManager.isEnabled) }
    var safeBrowsingEnabled by remember { 
        mutableStateOf(sharedPrefs.getBoolean("safe_browsing", true)) 
    }
    var preventPopupsEnabled by remember { 
        mutableStateOf(sharedPrefs.getBoolean("prevent_popups", true)) 
    }

    // FabMenu state
    var showFabMenu by remember { mutableStateOf(false) }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val menuItems = listOf(
        PrivacyMenuItem(
            icon = Icons.Rounded.AdsClick,
            label = "Pemblokir Iklan",
            subtitle = if (adBlockerEnabled) "Aktif" else "Tidak Aktif",
            onClick = {
                adBlockerEnabled = !adBlockerEnabled
                adBlockManager.isEnabled = adBlockerEnabled
            }
        ),
        PrivacyMenuItem(
            icon = Icons.Rounded.Security,
            label = "Penjelajahan aman",
            subtitle = if (safeBrowsingEnabled) "Aktif" else "Tidak Aktif",
            onClick = {
                safeBrowsingEnabled = !safeBrowsingEnabled
                sharedPrefs.edit().putBoolean("safe_browsing", safeBrowsingEnabled).apply()
            }
        ),
        PrivacyMenuItem(
            icon = Icons.Rounded.Public,
            label = "Cegah Pop up",
            subtitle = if (preventPopupsEnabled) "Aktif" else "Tidak Aktif",
            onClick = {
                preventPopupsEnabled = !preventPopupsEnabled
                sharedPrefs.edit().putBoolean("prevent_popups", preventPopupsEnabled).apply()
            }
        ),
        PrivacyMenuItem(
            icon = Icons.Rounded.Settings,
            label = "Pengaturan situs",
            subtitle = "Izin lokasi, kamera, mikrofon, dll",
            onClick = { /* Implement logic if needed */ }
        ),
        PrivacyMenuItem(
            icon = Icons.Rounded.Cookie,
            label = "Kuki",
            subtitle = "Kelola data kuki situs web",
            onClick = { /* Implement logic if needed */ }
        ),
        PrivacyMenuItem(
            icon = Icons.Rounded.Dns,
            label = "DNS",
            subtitle = "Atur DNS pribadi Anda",
            onClick = { /* Implement logic if needed */ }
        ),
        PrivacyMenuItem(
            icon = Icons.AutoMirrored.Rounded.ExitToApp,
            label = "Hapus data saat keluar",
            subtitle = "Otomatis hapus data saat aplikasi ditutup",
            onClick = { /* Implement logic if needed */ }
        ),
        PrivacyMenuItem(
            icon = Icons.Rounded.DeleteForever,
            label = "Hapus data jelajah",
            subtitle = "Riwayat, kuki, cache",
            onClick = { /* Implement logic if needed */ }
        )
    )

    // Animation states
    var visibleItems by remember { mutableStateOf(setOf<Int>()) }

    LaunchedEffect(showFabMenu) {
        if (showFabMenu) {
            visibleItems = emptySet()
            for (i in menuItems.indices.reversed()) {
                delay(50L)
                visibleItems = visibleItems + i
            }
        } else {
            visibleItems = emptySet()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(top = statusBarPadding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
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
                    text = "Privasi",
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
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(end = 24.dp, bottom = 24.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            // FAB Menu Overlay (clickable scrim)
            if (showFabMenu) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            showFabMenu = false
                        }
                )
            }

            // Menu Pills
            Column(
                modifier = Modifier.padding(bottom = 72.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                menuItems.forEachIndexed { index, item ->
                    AnimatedVisibility(
                        visible = showFabMenu && index in visibleItems,
                        enter = fadeIn(animationSpec = tween(200)) +
                                slideInVertically(
                                    initialOffsetY = { it / 2 },
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                ),
                        exit = fadeOut(animationSpec = tween(150)) +
                                slideOutVertically(
                                    targetOffsetY = { it / 2 },
                                    animationSpec = tween(150)
                                )
                    ) {
                        if (item.subtitle != null) {
                            MenuPillWithSubtitle(
                                icon = item.icon,
                                label = item.label,
                                subtitle = item.subtitle,
                                onClick = item.onClick
                            )
                        } else {
                            MenuPill(
                                icon = item.icon,
                                label = item.label,
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
                    imageVector = if (showFabMenu) Icons.AutoMirrored.Rounded.ArrowBack else Icons.Rounded.Security,
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
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "pillScale"
    )

    Surface(
        modifier = Modifier
            .scale(scale)
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
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "pillScale"
    )

    Surface(
        modifier = Modifier
            .scale(scale)
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
