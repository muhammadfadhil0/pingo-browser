package com.fadhilmanfa.pingo.data.adblock

import android.content.Context
import android.content.SharedPreferences

/**
 * Manager untuk mengelola daftar situs yang dikecualikan dari pemblokiran iklan
 */
class WhitelistManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Get semua domain yang di-whitelist
     */
    fun getWhitelist(): Set<String> {
        return prefs.getStringSet(KEY_WHITELIST, emptySet()) ?: emptySet()
    }
    
    /**
     * Tambah domain ke whitelist
     */
    fun addToWhitelist(domain: String) {
        val currentList = getWhitelist().toMutableSet()
        currentList.add(normalizeHost(domain))
        prefs.edit().putStringSet(KEY_WHITELIST, currentList).apply()
    }
    
    /**
     * Hapus domain dari whitelist
     */
    fun removeFromWhitelist(domain: String) {
        val currentList = getWhitelist().toMutableSet()
        currentList.remove(normalizeHost(domain))
        prefs.edit().putStringSet(KEY_WHITELIST, currentList).apply()
    }
    
    /**
     * Cek apakah domain ada di whitelist
     */
    fun isWhitelisted(host: String): Boolean {
        val whitelist = getWhitelist()
        val normalizedHost = normalizeHost(host)
        
        return whitelist.any { whitelistedDomain ->
            normalizedHost == whitelistedDomain || 
            normalizedHost.endsWith(".$whitelistedDomain")
        }
    }
    
    /**
     * Clear semua whitelist
     */
    fun clearWhitelist() {
        prefs.edit().remove(KEY_WHITELIST).apply()
    }
    
    /**
     * Normalize host (hapus www. dan lowercase)
     */
    private fun normalizeHost(host: String): String {
        return host.lowercase()
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .substringBefore("/")
            .substringBefore("?")
            .trim()
    }
    
    companion object {
        private const val PREFS_NAME = "pingo_whitelist_prefs"
        private const val KEY_WHITELIST = "whitelisted_domains"
    }
}
