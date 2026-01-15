package com.fadhilmanfa.pingo.ui.pages

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AdsClick
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fadhilmanfa.pingo.data.adblock.AdBlockManager
import com.fadhilmanfa.pingo.ui.theme.Secondary
import com.fadhilmanfa.pingo.ui.theme.TextPrimary
import com.fadhilmanfa.pingo.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    onBack: () -> Unit,
    onNavigateToAdBlocker: () -> Unit,
    currentTheme: String,
    onThemeChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val adBlockManager = remember { AdBlockManager.getInstance(context) }
    
    var pingoAiEnabled by remember { mutableStateOf(true) }
    var adBlockerEnabled by remember { mutableStateOf(adBlockManager.isEnabled) }
    
    // Theme states
    var showThemeSheet by remember { mutableStateOf(false) }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

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
                text = "Pengaturan",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = navBarPadding + 16.dp)
        ) {
            // Quick Toggles Section
            SettingsSection {
                SwitchItem(
                    icon = Icons.Rounded.AutoAwesome,
                    title = "Pingo AI",
                    subtitle = if (pingoAiEnabled) "Aktif" else "Tidak Aktif",
                    checked = pingoAiEnabled,
                    onCheckedChange = { pingoAiEnabled = it }
                )
                SwitchItem(
                    icon = Icons.Rounded.AdsClick,
                    title = "Pemblokir Iklan",
                    subtitle = if (adBlockerEnabled) "Aktif" else "Tidak Aktif",
                    checked = adBlockerEnabled,
                    onCheckedChange = { 
                        adBlockerEnabled = it
                        adBlockManager.isEnabled = it
                    },
                    onClick = onNavigateToAdBlocker
                )
                SwitchItem(
                    icon = Icons.Rounded.DarkMode,
                    title = "Tema Gelap",
                    subtitle = when(currentTheme) {
                        "light" -> "Terang"
                        "dark" -> "Gelap"
                        else -> "Ikuti Perangkat"
                    },
                    checked = currentTheme == "dark",
                    onCheckedChange = { isDark ->
                        onThemeChanged(if (isDark) "dark" else "light")
                    },
                    onClick = { showThemeSheet = true }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp), 
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Menu Items Section
            MenuItem(
                icon = Icons.Rounded.Language,
                title = "1. Umum",
                subtitle = "Bahasa, Default browser",
                onClick = {}
            )
            MenuItem(
                icon = Icons.Rounded.Palette,
                title = "2. Tampilan",
                subtitle = "Tema, layout, ukuran teks",
                onClick = {}
            )
            MenuItem(
                icon = Icons.Rounded.Home,
                title = "3. Halaman Mulai",
                onClick = {}
            )
            MenuItem(
                icon = Icons.Rounded.PrivacyTip,
                title = "4. Privasi",
                subtitle = "Pengaturan situs, perizinan, kuki",
                onClick = {}
            )
            MenuItem(
                icon = Icons.Rounded.Search,
                title = "5. Pencarian",
                subtitle = "Mesin pencarian Anda",
                onClick = {}
            )
            MenuItem(
                icon = Icons.Rounded.Notifications,
                title = "6. Notifikasi",
                subtitle = "Update, unduhan",
                onClick = {}
            )
            MenuItem(
                icon = Icons.Rounded.Download,
                title = "7. Unduhan",
                subtitle = "Lokasi unduhan, pengaturan unduhan",
                onClick = {}
            )
            MenuItem(
                icon = Icons.Rounded.HelpOutline,
                title = "8. Bantuan",
                subtitle = "FAQs, lapor kesalahan, sosial",
                onClick = {}
            )
            MenuItem(
                icon = Icons.Rounded.Info,
                title = "9. Tentang Pingo",
                subtitle = "Pembaruan, versi",
                onClick = {}
            )
        }
    }

    if (showThemeSheet) {
        ThemeSelectionBottomSheet(
            selectedTheme = currentTheme,
            onThemeSelected = { 
                onThemeChanged(it)
                showThemeSheet = false
            },
            onDismiss = { showThemeSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSelectionBottomSheet(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 32.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Pilih Tema",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            
            ThemeOptionItem(
                label = "Terang",
                selected = selectedTheme == "light",
                onClick = { onThemeSelected("light") }
            )
            ThemeOptionItem(
                label = "Gelap",
                selected = selectedTheme == "dark",
                onClick = { onThemeSelected("dark") }
            )
            ThemeOptionItem(
                label = "Ikuti Perangkat",
                selected = selectedTheme == "system",
                onClick = { onThemeSelected("system") }
            )
        }
    }
}

@Composable
private fun ThemeOptionItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = Secondary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun SettingsSection(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        content = content
    )
}

@Composable
private fun SwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Secondary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Secondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Secondary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
