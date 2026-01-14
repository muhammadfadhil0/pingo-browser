package com.fadhilmanfa.pingo.data

/**
 * Data class representing extracted content from a web page
 */
data class PageContent(
    val url: String,
    val title: String,
    val description: String,
    val mainContent: String,
    val links: List<LinkInfo>,
    val headings: List<String>,
    val extractedAt: Long = System.currentTimeMillis()
)

/**
 * Represents a hyperlink found on the page
 */
data class LinkInfo(
    val text: String,
    val href: String
)
