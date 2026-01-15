package com.fadhilmanfa.pingo.data

/**
 * Data class representing a bookmark entry
 */
data class BookmarkItem(
    val id: String,
    val title: String,
    val url: String,
    val timestamp: Long
)
