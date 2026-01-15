package com.fadhilmanfa.pingo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fadhilmanfa.pingo.data.TabItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabSwitcherBottomSheet(
    visible: Boolean,
    dragProgress: Float,
    tabs: List<TabItem>,
    onDismiss: () -> Unit,
    onTabSelected: (TabItem) -> Unit,
    onTabClose: (TabItem) -> Unit,
    onNewTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()
    
    val shouldShow = visible || dragProgress > 0.5f
    
    LaunchedEffect(shouldShow) {
        if (shouldShow && !sheetState.isVisible) {
            sheetState.expand()
        } else if (!shouldShow && sheetState.isVisible) {
            sheetState.hide()
        }
    }
    
    if (shouldShow) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentWindowInsets = { WindowInsets(0.dp) },
            scrimColor = Color.Black.copy(alpha = 0.32f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Header(
                    tabCount = tabs.size,
                    onNewTab = {
                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            onNewTab()
                            onDismiss()
                        }
                    }
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tabs, key = { it.id }) { tab ->
                        TabCard(
                            tab = tab,
                            onClick = {
                                scope.launch {
                                    sheetState.hide()
                                }.invokeOnCompletion {
                                    onTabSelected(tab)
                                    onDismiss()
                                }
                            },
                            onClose = { 
                                // Jika ini tab terakhir, animasikan tutup sheet
                                if (tabs.size == 1) {
                                    scope.launch {
                                        sheetState.hide()
                                    }.invokeOnCompletion {
                                        onTabClose(tab)
                                        onDismiss()
                                    }
                                } else {
                                    onTabClose(tab)
                                }
                            },
                            modifier = Modifier.height(180.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(tabCount: Int, onNewTab: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "$tabCount Tab",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        IconButton(
            onClick = onNewTab,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = "Tab Baru",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
