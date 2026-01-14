package com.fadhilmanfa.pingo.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.rounded.AccountCircle
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fadhilmanfa.pingo.ui.theme.Primary
import com.fadhilmanfa.pingo.ui.theme.Secondary
import com.fadhilmanfa.pingo.ui.theme.TextPrimary
import com.fadhilmanfa.pingo.ui.theme.TextSecondary

@Composable
fun SettingsPage(
    onBack: () -> Unit
) {
    var pingoAiEnabled by remember { mutableStateOf(true) }
    var adBlockerEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
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
                    tint = TextPrimary
                )
            }
            Text(
                text = "Pengaturan",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
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
                    onCheckedChange = { adBlockerEnabled = it }
                )
                SwitchItem(
                    icon = Icons.Rounded.DarkMode,
                    title = "Dark Mode",
                    subtitle = if (darkModeEnabled) "Aktif" else "Tidak Aktif",
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF0F0F0))

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
}

@Composable
fun SettingsSection(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        content = content
    )
}

@Composable
fun SwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = TextSecondary
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
                uncheckedTrackColor = Color.LightGray,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
fun MenuItem(
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
            tint = TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

import androidx.compose.layout.ColumnScope
