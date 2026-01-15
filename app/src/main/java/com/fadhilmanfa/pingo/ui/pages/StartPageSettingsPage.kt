package com.fadhilmanfa.pingo.ui.pages

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.fadhilmanfa.pingo.ui.theme.Secondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartPageSettingsPage(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("pingo_settings", Context.MODE_PRIVATE) }
    
    var showSpeedDials by remember { 
        mutableStateOf(sharedPrefs.getBoolean("show_speed_dials", true)) 
    }
    var showRecommendations by remember { 
        mutableStateOf(sharedPrefs.getBoolean("show_recommendations", true)) 
    }
    var selectedWallpaper by remember { 
        mutableStateOf(sharedPrefs.getString("selected_wallpaper", "none") ?: "none") 
    }

    // List of sample wallpapers (could be resource IDs or URLs)
    val defaultWallpapers = remember { listOf("none", "wallpaper_1", "wallpaper_2", "wallpaper_3") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedWallpaper = it.toString()
            sharedPrefs.edit().putString("selected_wallpaper", it.toString()).apply()
        }
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
                text = "Halaman Mulai",
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
            // SECTION 1: WALLPAPER
            SettingsGroup(title = "Wallpaper", icon = Icons.Rounded.Wallpaper) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Custom Image Picker Card
                    item {
                        WallpaperPickerCard(onClick = { launcher.launch("image/*") })
                    }
                    
                    // Default Wallpaper Cards
                    items(defaultWallpapers) { wp ->
                        WallpaperCard(
                            label = if (wp == "none") "Default" else wp,
                            selected = selectedWallpaper == wp,
                            onClick = {
                                selectedWallpaper = wp
                                sharedPrefs.edit().putString("selected_wallpaper", wp).apply()
                            }
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // SECTION 2: SPEED DIALS
            SwitchItem(
                icon = Icons.Rounded.Star,
                title = "Speed dials",
                subtitle = "Tampilkan situs favorit di halaman mulai",
                checked = showSpeedDials,
                onCheckedChange = { 
                    showSpeedDials = it
                    sharedPrefs.edit().putBoolean("show_speed_dials", it).apply()
                }
            )

            // SECTION 3: REKOMENDASI
            SwitchItem(
                icon = Icons.Rounded.Link,
                title = "Situs rekomendasi",
                subtitle = "Tampilkan saran situs populer",
                checked = showRecommendations,
                onCheckedChange = { 
                    showRecommendations = it
                    sharedPrefs.edit().putBoolean("show_recommendations", it).apply()
                }
            )
        }
    }
}

@Composable
private fun WallpaperPickerCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(width = 100.dp, height = 150.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Pick Image",
                tint = Secondary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun WallpaperCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .size(width = 100.dp, height = 150.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) Secondary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
        border = if (selected) BorderStroke(2.dp, Secondary) else null
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Placeholder for wallpaper image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (label == "Default") MaterialTheme.colorScheme.surface
                        else Color.Gray.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (label == "Default") {
                    Text("No Image", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            
            // Selection indicator overlay
            if (selected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Secondary.copy(alpha = 0.1f))
                )
            }
        }
    }
}

@Composable
private fun SettingsGroup(title: String, icon: ImageVector? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        content()
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
