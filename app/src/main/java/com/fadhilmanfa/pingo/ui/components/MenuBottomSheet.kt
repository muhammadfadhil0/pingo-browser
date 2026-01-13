package com.fadhilmanfa.pingo.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fadhilmanfa.pingo.ui.theme.SurfaceDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuBottomSheet(
    canGoForward: Boolean,
    onDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onForward: () -> Unit,
    onBookmark: () -> Unit = {},
    onHistory: () -> Unit = {},
    onDownloads: () -> Unit = {},
    onSettings: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        contentWindowInsets = { WindowInsets(0.dp) } // Fixed: now requires a lambda in Material3 1.4.0
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .windowInsetsPadding(WindowInsets.navigationBars) // Add navigation bar padding
        ) {
            // Handle indicator
            Surface(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(2.dp),
                color = Color.Gray
            ) {}

            Spacer(modifier = Modifier.height(24.dp))

            MenuItem(
                icon = Icons.Rounded.Refresh,
                label = "Refresh",
                onClick = {
                    onDismiss()
                    onRefresh()
                }
            )

            MenuItem(
                icon = Icons.AutoMirrored.Rounded.ArrowForward,
                label = "Maju",
                enabled = canGoForward,
                onClick = {
                    onDismiss()
                    onForward()
                }
            )

            MenuItem(
                icon = Icons.Rounded.BookmarkBorder,
                label = "Bookmark",
                onClick = {
                    onDismiss()
                    onBookmark()
                }
            )

            MenuItem(
                icon = Icons.Rounded.History,
                label = "Riwayat",
                onClick = {
                    onDismiss()
                    onHistory()
                }
            )

            MenuItem(
                icon = Icons.Rounded.Download,
                label = "Unduhan",
                onClick = {
                    onDismiss()
                    onDownloads()
                }
            )

            MenuItem(
                icon = Icons.Rounded.Settings,
                label = "Pengaturan",
                onClick = {
                    onDismiss()
                    onSettings()
                }
            )
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 0.7f else 0.3f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White.copy(alpha = alpha),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            color = Color.White.copy(alpha = alpha),
            fontSize = 16.sp
        )
    }
}
