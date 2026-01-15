package com.fadhilmanfa.pingo.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fadhilmanfa.pingo.data.DownloadItem
import com.fadhilmanfa.pingo.data.DownloadStatus
import java.util.*

@Composable
fun DownloadsPage(
    downloadItems: List<DownloadItem>,
    onBack: () -> Unit,
    onFileClick: (DownloadItem) -> Unit,
    onClearDownloads: () -> Unit,
    onRemoveItem: (DownloadItem) -> Unit
) {
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
                text = "Unduhan",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
            IconButton(onClick = onClearDownloads) {
                Icon(
                    imageVector = Icons.Rounded.ClearAll,
                    contentDescription = "Hapus Semua",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }

        if (downloadItems.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorSurfaceVariant().copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Belum ada unduhan",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = navBarPadding + 16.dp)
            ) {
                items(downloadItems.sortedByDescending { it.timestamp }) { item ->
                    DownloadListItem(
                        item = item,
                        onClick = { onFileClick(item) },
                        onRemove = { onRemoveItem(item) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun MaterialTheme.colorSurfaceVariant() = MaterialTheme.colorScheme.onSurfaceVariant

@Composable
private fun DownloadListItem(
    item: DownloadItem,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val sizeText = if (item.status == DownloadStatus.COMPLETED) {
        formatFileSize(if (item.totalSize > 0) item.totalSize else item.downloadedSize)
    } else if (item.totalSize > 0) {
        formatFileSize(item.downloadedSize) + " / " + formatFileSize(item.totalSize)
    } else {
        formatFileSize(item.downloadedSize)
    }

    val fileIcon = getFileIcon(item.fileName)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = fileIcon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = when(item.status) {
                        DownloadStatus.COMPLETED -> Color(0xFF4CAF50)
                        DownloadStatus.FAILED -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.fileName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.url,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sizeText,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                if (item.status == DownloadStatus.DOWNLOADING) {
                    val progress = if (item.totalSize > 0) item.downloadedSize.toFloat() / item.totalSize else 0f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .width(60.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                } else {
                    Text(
                        text = if (item.status == DownloadStatus.COMPLETED) "Selesai" else item.status.name.lowercase().replaceFirstChar { it.uppercase() },
                        fontSize = 11.sp,
                        color = when(item.status) {
                            DownloadStatus.COMPLETED -> Color(0xFF4CAF50)
                            DownloadStatus.FAILED -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
        
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = "Opsi",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun getFileIcon(fileName: String): ImageVector {
    val extension = fileName.substringAfterLast('.', "").lowercase()
    return when (extension) {
        "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg" -> Icons.Rounded.Image
        "mp4", "mkv", "webm", "avi", "flv", "mov" -> Icons.Rounded.Movie
        "mp3", "wav", "ogg", "m4a", "flac" -> Icons.Rounded.MusicNote
        "pdf" -> Icons.Rounded.PictureAsPdf
        "zip", "rar", "7z", "tar", "gz" -> Icons.Rounded.FolderZip
        "doc", "docx", "txt", "rtf", "odt" -> Icons.Rounded.Description
        "xls", "xlsx", "csv" -> Icons.Rounded.TableChart
        "ppt", "pptx" -> Icons.Rounded.Slideshow
        "apk" -> Icons.Rounded.Android
        else -> Icons.Rounded.InsertDriveFile
    }
}

private fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
