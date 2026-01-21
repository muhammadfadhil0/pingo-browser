package com.fadhilmanfa.pingo.ui.pages

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.fadhilmanfa.pingo.R
import com.fadhilmanfa.pingo.data.BookmarkItem
import java.text.SimpleDateFormat
import java.util.*

data class QuickAccessItem(
        val id: String,
        val title: String,
        val url: String,
        val faviconUrl: String = ""
)

@Composable
fun StartPage(
        bookmarks: List<BookmarkItem> = emptyList(),
        onQuickAccessClick: (String) -> Unit = {}
) {
        val currentTime = remember { mutableStateOf(Calendar.getInstance()) }

        // Update time every second
        LaunchedEffect(Unit) {
                while (true) {
                        currentTime.value = Calendar.getInstance()
                        kotlinx.coroutines.delay(1000)
                }
        }

        val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
        val dateFormat = remember { SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()) }

        val timeString = timeFormat.format(currentTime.value.time)
        val dateString = dateFormat.format(currentTime.value.time)

        // Quick access items from bookmarks (max 8)
        val quickAccessItems =
                remember(bookmarks) {
                        bookmarks.take(8).map { bookmark ->
                                QuickAccessItem(
                                        id = bookmark.id,
                                        title = bookmark.title,
                                        url = bookmark.url,
                                        faviconUrl = getFaviconUrl(bookmark.url)
                                )
                        }
                }

        Box(modifier = Modifier.fillMaxSize()) {
                // Background Wallpaper
                Image(
                        painter = painterResource(id = R.drawable.main_page_wallpaper),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                )

                // Gradient Overlay - dari atas transparan ke bawah semi-gelap
                Box(
                        modifier =
                                Modifier.fillMaxSize()
                                        .background(
                                                Brush.verticalGradient(
                                                        colors =
                                                                listOf(
                                                                        Color.Transparent,
                                                                        Color.Black.copy(
                                                                                alpha = 0.3f
                                                                        ),
                                                                        Color.Black.copy(
                                                                                alpha = 0.7f
                                                                        )
                                                                ),
                                                        startY = 0f,
                                                        endY = Float.POSITIVE_INFINITY
                                                )
                                        )
                )

                // Content
                Column(
                        modifier =
                                Modifier.fillMaxSize()
                                        .statusBarsPadding()
                                        .navigationBarsPadding()
                                        .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                        Spacer(modifier = Modifier.height(48.dp))

                        // Logo Pingo
                        Text(
                                text = "Pingo",
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Tagline
                        Text(
                                text = stringResource(R.string.start_page_tagline),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.White.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.weight(0.4f))

                        // Clock Display
                        Text(
                                text = timeString,
                                fontSize = 72.sp,
                                fontWeight = FontWeight.Light,
                                color = Color.White,
                                letterSpacing = 4.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Date Display
                        Text(
                                text = dateString,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.White.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.weight(0.5f))

                        // Quick Access Section
                        if (quickAccessItems.isNotEmpty()) {
                                Text(
                                        text = stringResource(R.string.start_page_quick_access),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.align(Alignment.Start)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                QuickAccessGrid(
                                        items = quickAccessItems,
                                        onItemClick = onQuickAccessClick
                                )
                        }

                        Spacer(modifier = Modifier.height(120.dp))
                }
        }
}

@Composable
private fun QuickAccessGrid(items: List<QuickAccessItem>, onItemClick: (String) -> Unit) {
        LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
        ) {
                items(items) { item ->
                        QuickAccessTile(item = item, onClick = { onItemClick(item.url) })
                }
        }
}

@Composable
private fun QuickAccessTile(item: QuickAccessItem, onClick: () -> Unit) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by
                animateFloatAsState(
                        targetValue = if (isPressed) 0.92f else 1f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                        label = "tileScale"
                )

        Column(
                modifier =
                        Modifier.scale(scale)
                                .clickable(
                                        interactionSource = interactionSource,
                                        indication = null,
                                        onClick = onClick
                                ),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
                Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        border =
                                BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                )
                ) {
                        Box(contentAlignment = Alignment.Center) {
                                if (item.faviconUrl.isNotEmpty()) {
                                        SubcomposeAsyncImage(
                                                model = item.faviconUrl,
                                                contentDescription = item.title,
                                                modifier = Modifier.size(28.dp).clip(CircleShape),
                                                contentScale = ContentScale.Fit,
                                                loading = {
                                                        Icon(
                                                                imageVector =
                                                                        Icons.Rounded.Language,
                                                                contentDescription = null,
                                                                tint =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurfaceVariant
                                                                                .copy(alpha = 0.4f),
                                                                modifier = Modifier.size(28.dp)
                                                        )
                                                },
                                                error = {
                                                        Icon(
                                                                imageVector =
                                                                        Icons.Rounded.Language,
                                                                contentDescription = null,
                                                                tint =
                                                                        MaterialTheme.colorScheme
                                                                                .onSurfaceVariant
                                                                                .copy(alpha = 0.7f),
                                                                modifier = Modifier.size(28.dp)
                                                        )
                                                },
                                                success = { SubcomposeAsyncImageContent() }
                                        )
                                } else {
                                        Icon(
                                                imageVector = Icons.Rounded.Language,
                                                contentDescription = null,
                                                tint =
                                                        MaterialTheme.colorScheme.onSurfaceVariant
                                                                .copy(alpha = 0.7f),
                                                modifier = Modifier.size(28.dp)
                                        )
                                }
                        }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                        text = item.title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(64.dp)
                )
        }
}

private fun getFaviconUrl(url: String): String {
        return try {
                val uri = java.net.URI(url)
                val domain = uri.host ?: return ""
                "https://www.google.com/s2/favicons?domain=$domain&sz=64"
        } catch (e: Exception) {
                ""
        }
}
