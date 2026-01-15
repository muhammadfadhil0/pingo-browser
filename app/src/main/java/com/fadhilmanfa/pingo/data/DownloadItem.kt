package com.fadhilmanfa.pingo.data

/**
 * Data class representing a download entry
 */
data class DownloadItem(
    val id: String,
    val fileName: String,
    val url: String,
    val filePath: String,
    val totalSize: Long,
    val downloadedSize: Long,
    val status: DownloadStatus,
    val timestamp: Long,
    val androidId: Long? = null
)

enum class DownloadStatus {
    DOWNLOADING,
    COMPLETED,
    FAILED,
    PAUSED
}
