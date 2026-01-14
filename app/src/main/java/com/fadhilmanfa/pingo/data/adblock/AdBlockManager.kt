package com.fadhilmanfa.pingo.data.adblock

import android.content.Context
import android.content.SharedPreferences
import java.util.regex.Pattern

/**
 * Tingkat kekuatan pemblokiran iklan
 */
enum class AdBlockStrength(val key: String, val displayTitle: String, val displayDescription: String) {
    RINGAN("ringan", "Ringan", "Block berdasarkan daftar domain populer"),
    SEDANG("sedang", "Sedang", "Menggunakan metode parsing per halaman web"),
    KUAT("kuat", "Kuat", "Full AdBlock Plus filter syntax support (mungkin beberapa halaman web akan rusak)")
}

/**
 * Manager untuk mengelola logika pemblokiran iklan
 */
class AdBlockManager private constructor(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val whitelistManager = WhitelistManager(context)
    
    // Cached patterns untuk performa
    private var cachedBlockedDomains: Set<String> = emptySet()
    private var cachedBlockedPatterns: List<Pattern> = emptyList()
    private var currentStrength: AdBlockStrength = AdBlockStrength.SEDANG
    
    init {
        loadSettings()
        loadFilters()
    }
    
    /**
     * Cek apakah AdBlocker aktif
     */
    var isEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, true)
        set(value) {
            prefs.edit().putBoolean(KEY_ENABLED, value).apply()
        }
    
    /**
     * Cek apakah Cookie Blocker aktif
     */
    var isCookieBlockerEnabled: Boolean
        get() = prefs.getBoolean(KEY_COOKIE_BLOCKER, true)
        set(value) {
            prefs.edit().putBoolean(KEY_COOKIE_BLOCKER, value).apply()
        }
    
    /**
     * Get/Set strength level
     */
    var strength: AdBlockStrength
        get() = currentStrength
        set(value) {
            currentStrength = value
            prefs.edit().putString(KEY_STRENGTH, value.key).apply()
            loadFilters()
        }
    
    /**
     * Jumlah iklan yang telah diblokir
     */
    var blockedCount: Int
        get() = prefs.getInt(KEY_BLOCKED_COUNT, 0)
        private set(value) {
            prefs.edit().putInt(KEY_BLOCKED_COUNT, value).apply()
        }
    
    /**
     * Increment counter iklan yang diblokir
     */
    fun incrementBlockedCount() {
        blockedCount = blockedCount + 1
    }
    
    /**
     * Reset counter
     */
    fun resetBlockedCount() {
        blockedCount = 0
    }
    
    /**
     * Cek apakah URL harus diblokir
     */
    fun shouldBlock(url: String): Boolean {
        if (!isEnabled) return false
        
        val host = extractHost(url)
        
        // Cek whitelist dulu
        if (whitelistManager.isWhitelisted(host)) return false
        
        // Cek domain blacklist
        if (cachedBlockedDomains.any { blockedDomain ->
            host == blockedDomain || host.endsWith(".$blockedDomain")
        }) {
            return true
        }
        
        // Cek pattern (untuk SEDANG dan KUAT)
        if (currentStrength != AdBlockStrength.RINGAN) {
            if (cachedBlockedPatterns.any { it.matcher(url).find() }) {
                return true
            }
        }
        
        return false
    }
    
    /**
     * Load settings dari SharedPreferences
     */
    private fun loadSettings() {
        val strengthKey = prefs.getString(KEY_STRENGTH, AdBlockStrength.SEDANG.key)
        currentStrength = AdBlockStrength.entries.find { it.key == strengthKey } ?: AdBlockStrength.SEDANG
    }
    
    /**
     * Load filter lists berdasarkan strength level
     */
    private fun loadFilters() {
        cachedBlockedDomains = when (currentStrength) {
            AdBlockStrength.RINGAN -> AdBlockHosts.BASIC_HOSTS
            AdBlockStrength.SEDANG -> AdBlockHosts.BASIC_HOSTS + AdBlockHosts.EXTENDED_HOSTS
            AdBlockStrength.KUAT -> AdBlockHosts.BASIC_HOSTS + AdBlockHosts.EXTENDED_HOSTS + AdBlockHosts.AGGRESSIVE_HOSTS
        }
        
        cachedBlockedPatterns = when (currentStrength) {
            AdBlockStrength.RINGAN -> emptyList()
            AdBlockStrength.SEDANG -> AdBlockHosts.MEDIUM_PATTERNS.map { Pattern.compile(it, Pattern.CASE_INSENSITIVE) }
            AdBlockStrength.KUAT -> (AdBlockHosts.MEDIUM_PATTERNS + AdBlockHosts.AGGRESSIVE_PATTERNS).map { 
                Pattern.compile(it, Pattern.CASE_INSENSITIVE) 
            }
        }
    }
    
    /**
     * JavaScript untuk block cookie consent dialogs
     */
    fun getCookieBlockerScript(): String {
        if (!isCookieBlockerEnabled) return ""
        return """
            (function() {
                if (window._pingoCookieBlockerInjected) return;
                window._pingoCookieBlockerInjected = true;
                
                const selectors = [
                    '[class*="cookie-consent"]',
                    '[class*="cookie-banner"]',
                    '[class*="cookie-notice"]',
                    '[class*="cookie-popup"]',
                    '[class*="gdpr"]',
                    '[class*="consent-banner"]',
                    '[class*="privacy-banner"]',
                    '[id*="cookie-consent"]',
                    '[id*="cookie-banner"]',
                    '[id*="gdpr"]',
                    '[id*="onetrust"]',
                    '[class*="onetrust"]',
                    '[class*="CookieConsent"]',
                    '[class*="cc-window"]',
                    '[class*="cc-banner"]',
                    '.qc-cmp2-container',
                    '#qc-cmp2-container',
                    '[aria-label*="cookie"]',
                    '[aria-label*="consent"]'
                ];
                
                function hideElements() {
                    selectors.forEach(selector => {
                        document.querySelectorAll(selector).forEach(el => {
                            el.style.display = 'none';
                            el.style.visibility = 'hidden';
                            el.remove();
                        });
                    });
                    
                    // Reset body overflow jika di-lock
                    document.body.style.overflow = '';
                    document.documentElement.style.overflow = '';
                }
                
                // Run immediately
                hideElements();
                
                // Run after DOM ready
                if (document.readyState === 'loading') {
                    document.addEventListener('DOMContentLoaded', hideElements);
                }
                
                // Watch for dynamically added elements
                const observer = new MutationObserver(hideElements);
                observer.observe(document.body, { childList: true, subtree: true });
                
                // Run periodically for stubborn popups
                setTimeout(hideElements, 500);
                setTimeout(hideElements, 1500);
                setTimeout(hideElements, 3000);
            })();
        """.trimIndent()
    }
    
    /**
     * Extract host dari URL
     */
    private fun extractHost(url: String): String {
        return try {
            val cleanUrl = url.lowercase()
            val withoutProtocol = cleanUrl
                .removePrefix("https://")
                .removePrefix("http://")
                .removePrefix("www.")
            withoutProtocol.substringBefore("/").substringBefore("?").substringBefore(":")
        } catch (e: Exception) {
            ""
        }
    }
    
    companion object {
        private const val PREFS_NAME = "pingo_adblock_prefs"
        private const val KEY_ENABLED = "adblock_enabled"
        private const val KEY_STRENGTH = "adblock_strength"
        private const val KEY_BLOCKED_COUNT = "blocked_count"
        private const val KEY_COOKIE_BLOCKER = "cookie_blocker_enabled"
        
        @Volatile
        private var instance: AdBlockManager? = null
        
        fun getInstance(context: Context): AdBlockManager {
            return instance ?: synchronized(this) {
                instance ?: AdBlockManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
