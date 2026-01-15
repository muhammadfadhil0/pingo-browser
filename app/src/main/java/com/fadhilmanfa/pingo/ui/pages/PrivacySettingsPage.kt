package com.fadhilmanfa.pingo.ui.pages

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AdsClick
import androidx.compose.material.icons.rounded.Cookie
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@OptIn(ExperimentalMaterial3Api::class)
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
                text = "Privasi",
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
            // Ad Blocker
            SwitchItem(
                icon = Icons.Rounded.AdsClick,
                title = "Pemblokir Iklan",
                subtitle = if (adBlockerEnabled) "Aktif" else "Tidak Aktif",
                checked = adBlockerEnabled,
                onCheckedChange = { 
                    adBlockerEnabled = it
                    adBlockManager.isEnabled = it
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Site Settings & Cookies
            MenuItem(
                icon = Icons.Rounded.Settings,
                title = "Pengaturan Situs",
                subtitle = "Izin lokasi, kamera, mikrofon, dll",
                onClick = {}
            )
            MenuItem(
                icon = Icons.Rounded.Cookie,
                title = "Kuki",
                subtitle = "Kelola data kuki situs web",
                onClick = {}
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Data Collection & DNS
            MenuItem(
                icon = Icons.Rounded.InsertDriveFile,
                title = "Pengumpulan Data",
                subtitle = "Laporan penggunaan dan kesalahan",
                onClick = {}
            )
            MenuItem(
                icon = Icons.Rounded.Dns,
                title = "DNS",
                subtitle = "Atur DNS pribadi Anda",
                onClick = {}
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Switchers
            SwitchItem(
                icon = Icons.Rounded.Security,
                title = "Penjelajahan aman",
                subtitle = "Lindungi dari situs berbahaya",
                checked = safeBrowsingEnabled,
                onCheckedChange = { 
                    safeBrowsingEnabled = it
                    sharedPrefs.edit().putBoolean("safe_browsing", it).apply()
                }
            )
            SwitchItem(
                icon = Icons.Rounded.Public,
                title = "Cegah pop up",
                subtitle = "Blokir jendela pop-up yang mengganggu",
                checked = preventPopupsEnabled,
                onCheckedChange = { 
                    preventPopupsEnabled = it
                    sharedPrefs.edit().putBoolean("prevent_popups", it).apply()
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Data Cleanup
            MenuItem(
                icon = Icons.Rounded.ExitToApp,
                title = "Hapus data saat keluar",
                subtitle = "Otomatis hapus data saat aplikasi ditutup",
                onClick = {}
            )
            MenuItem(
                icon = Icons.Rounded.DeleteForever,
                title = "Hapus data jelajah",
                subtitle = "Riwayat, kuki, cache",
                onClick = {}
            )
        }
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

@Composable
private fun SwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
                    fontWeight = FontWeight.Medium,
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
                checkedTrackColor = Secondary
            )
        )
    }
}
