package com.fadhilmanfa.pingo.ui.pages

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoMode
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Height
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.VerticalAlignBottom
import androidx.compose.material.icons.rounded.VerticalAlignTop
import androidx.compose.material.icons.rounded.ViewHeadline
import androidx.compose.material.icons.rounded.WbSunny
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
import com.fadhilmanfa.pingo.ui.theme.Secondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsPage(
    onBack: () -> Unit,
    currentTheme: String,
    onThemeChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("pingo_settings", Context.MODE_PRIVATE) }
    
    var forceDarkWeb by remember { 
        mutableStateOf(sharedPrefs.getBoolean("force_dark_web", false)) 
    }
    var addressBarPos by remember { 
        mutableStateOf(sharedPrefs.getString("address_bar_position", "auto") ?: "auto") 
    }
    var preventMiniNavbar by remember { 
        mutableStateOf(sharedPrefs.getBoolean("prevent_mini_navbar", false)) 
    }

    var showAddressBarSheet by remember { mutableStateOf(false) }

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
                text = "Tampilan",
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
            // SECTION 1: TEMA
            SettingsGroup(title = "Tema") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ThemeCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Smartphone,
                        label = "Sistem",
                        selected = currentTheme == "system",
                        onClick = { onThemeChanged("system") }
                    )
                    ThemeCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.WbSunny,
                        label = "Terang",
                        selected = currentTheme == "light",
                        onClick = { onThemeChanged("light") }
                    )
                    ThemeCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.DarkMode,
                        label = "Gelap",
                        selected = currentTheme == "dark",
                        onClick = { onThemeChanged("dark") }
                    )
                }
                
                SwitchItem(
                    title = "Paksa halaman mode gelap",
                    subtitle = "Mencoba memaksa situs web menggunakan tema gelap",
                    checked = forceDarkWeb,
                    onCheckedChange = { 
                        forceDarkWeb = it
                        sharedPrefs.edit().putBoolean("force_dark_web", it).apply()
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // SECTION 2: LAYOUT DAN GESTUR
            SettingsGroup(title = "Layout dan Gestur") {
                MenuItem(
                    icon = Icons.Rounded.ViewHeadline,
                    title = "Posisi Bar Alamat",
                    subtitle = when(addressBarPos) {
                        "top" -> "Atas"
                        "bottom" -> "Bawah"
                        else -> "Otomatis"
                    },
                    onClick = { showAddressBarSheet = true }
                )

                SwitchItem(
                    icon = Icons.Rounded.Height,
                    title = "Cegah address bar dalam mode mini",
                    subtitle = "Paksa mode normal",
                    checked = preventMiniNavbar,
                    onCheckedChange = { 
                        preventMiniNavbar = it
                        sharedPrefs.edit().putBoolean("prevent_mini_navbar", it).apply()
                    }
                )

                MenuItem(
                    icon = Icons.Rounded.Edit,
                    title = "Edit bar alamat",
                    subtitle = "Kostumisasi fungsi dalam bar alamat",
                    onClick = { /* Navigate to Edit Page later */ }
                )
            }
        }
    }

    if (showAddressBarSheet) {
        AddressBarPositionBottomSheet(
            selectedPos = addressBarPos,
            onPosSelected = { pos ->
                addressBarPos = pos
                sharedPrefs.edit().putString("address_bar_position", pos).apply()
                showAddressBarSheet = false
            },
            onDismiss = { showAddressBarSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddressBarPositionBottomSheet(
    selectedPos: String,
    onPosSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Posisi Bar Alamat",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            
            PositionOptionItem(
                icon = Icons.Rounded.VerticalAlignTop,
                label = "Atas",
                selected = selectedPos == "top",
                onClick = { onPosSelected("top") }
            )
            PositionOptionItem(
                icon = Icons.Rounded.VerticalAlignBottom,
                label = "Bawah",
                selected = selectedPos == "bottom",
                onClick = { onPosSelected("bottom") }
            )
            PositionOptionItem(
                icon = Icons.Rounded.AutoMode,
                label = "Otomatis (Default)",
                selected = selectedPos == "auto",
                onClick = { onPosSelected("auto") }
            )
        }
    }
}

@Composable
private fun PositionOptionItem(
    icon: ImageVector,
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                fontSize = 16.sp,
                color = if (selected) Secondary else MaterialTheme.colorScheme.onSurface
            )
        }
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = Secondary)
        )
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
private fun ThemeCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) Secondary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (selected) BorderStroke(2.dp, Secondary) else null
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Secondary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) Secondary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SwitchItem(
    icon: ImageVector? = null,
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
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
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
