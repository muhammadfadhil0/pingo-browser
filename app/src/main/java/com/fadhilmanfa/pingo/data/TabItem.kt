package com.fadhilmanfa.pingo.data

import android.graphics.Bitmap

/**
 * Data class representing a single browser tab
 */
data class TabItem(
    val id: String,
    val title: String,
    val url: String,
    val faviconUrl: String? = null,
    val thumbnail: Bitmap? = null,
    val isActive: Boolean = false,
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val historyStack: List<String> = listOf(),
    val forwardStack: List<String> = listOf()
)
