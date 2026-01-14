package com.fadhilmanfa.pingo.data.adblock

/**
 * Daftar domain iklan yang akan diblokir
 * Dikelompokkan berdasarkan tingkat pemblokiran
 */
object AdBlockHosts {
    
    /**
     * Domain paling umum untuk ads (Level: RINGAN)
     * ~50 domain populer yang paling sering digunakan untuk iklan
     */
    val BASIC_HOSTS = setOf(
        // Google Ads
        "googlesyndication.com",
        "googleadservices.com",
        "pagead2.googlesyndication.com",
        "adservice.google.com",
        "doubleclick.net",
        "doubleclick.com",
        "googleads.g.doubleclick.net",
        
        // Facebook Ads
        "facebook.com/tr",
        "pixel.facebook.com",
        "an.facebook.com",
        
        // Other Major Ad Networks
        "ads.yahoo.com",
        "advertising.com",
        "admob.com",
        "adnxs.com",
        "adsrvr.org",
        "adtechus.com",
        "amazon-adsystem.com",
        "adobedtm.com",
        "adsafeprotected.com",
        "adroll.com",
        "criteo.com",
        "criteo.net",
        "outbrain.com",
        "taboola.com",
        "revcontent.com",
        "mgid.com",
        "zergnet.com",
        "contentad.net",
        
        // Tracking
        "scorecardresearch.com",
        "quantserve.com",
        "chartbeat.com",
        "optimizely.com",
        "hotjar.com",
        "mouseflow.com",
        "fullstory.com",
        "crazyegg.com",
        
        // Mobile Ads
        "appsflyer.com",
        "adjust.com",
        "branch.io",
        "kochava.com",
        "singular.net",
        
        // Indonesian Ads
        "detikads.com",
        "kompasads.com"
    )
    
    /**
     * Extended domain list (Level: SEDANG)
     * ~150 domain tambahan
     */
    val EXTENDED_HOSTS = setOf(
        // More Google
        "googletagmanager.com",
        "googletagservices.com",
        "googlesyndication.com",
        "google-analytics.com",
        
        // Programmatic Ads
        "pubmatic.com",
        "rubiconproject.com",
        "openx.net",
        "spotxchange.com",
        "smartadserver.com",
        "adcolony.com",
        "unity3d.com",
        "vungle.com",
        "applovin.com",
        "inmobi.com",
        "mopub.com",
        "ironsource.com",
        "fyber.com",
        "smaato.net",
        
        // Video Ads
        "teads.tv",
        "springserve.com",
        "spotx.tv",
        "jwpltx.com",
        
        // Native Ads
        "sharethrough.com",
        "nativo.com",
        "triplelift.com",
        "yieldmo.com",
        
        // Retargeting
        "rtmark.net",
        "adform.net",
        "mathtag.com",
        "mediamath.com",
        "casalemedia.com",
        "bidswitch.net",
        "lijit.com",
        
        // Data & Tracking
        "bluekai.com",
        "krxd.net",
        "exelator.com",
        "rlcdn.com",
        "demdex.net",
        "omtrdc.net",
        "everesttech.net",
        "rfihub.com",
        "tapad.com",
        "adsymptotic.com",
        
        // Social Widgets Tracking
        "platform.twitter.com",
        "connect.facebook.net",
        "platform.linkedin.com",
        
        // Popup/Overlay Ads
        "popads.net",
        "popcash.net",
        "propellerads.com",
        "adcash.com",
        "adf.ly",
        
        // Indonesian Publisher Ads
        "mediadnasolutions.com",
        "telkomsel.com/ads",
        "xlaxiata.co.id/ads"
    )
    
    /**
     * Aggressive hosts (Level: KUAT)
     * Domain yang mungkin break beberapa situs
     */
    val AGGRESSIVE_HOSTS = setOf(
        // Semua widget tracking
        "assets.pinterest.com",
        "widgets.pinterest.com",
        "buttons.github.io",
        "platform.instagram.com",
        
        // Semua analitik
        "analytics.twitter.com",
        "analytics.facebook.com",
        "analytics.tiktok.com",
        "analytics.google.com",
        "stats.wp.com",
        "pixel.wp.com",
        
        // CDN yang sering dipakai ads
        "cdn.taboola.com",
        "cdn.outbrain.com",
        "cdn.ampproject.org",
        
        // Payment/Paywall (kontroversial)
        "checkout.stripe.com",
        "js.stripe.com",
        
        // Newsletter popups
        "mailchimp.com",
        "klaviyo.com",
        "convertkit.com",
        "hubspot.com",
        "pardot.com",
        
        // Chat widgets (bisa dianggap spam)
        "widget.intercom.io",
        "embed.tawk.to",
        "static.zdassets.com",
        "cdn.livechatinc.com",
        "wchat.freshchat.com"
    )
    
    /**
     * URL patterns untuk matching (Level: SEDANG)
     */
    val MEDIUM_PATTERNS = listOf(
        // URL patterns
        ".*/ads/.*",
        ".*/ad/.*",
        ".*/advertisement/.*",
        ".*/banner/.*",
        ".*/banners/.*",
        ".*/sponsor.*",
        ".*/promo/.*",
        ".*/pixel\\.gif.*",
        ".*/pixel\\.png.*",
        ".*/tracking.*",
        ".*/beacon.*",
        ".*/analytics.*",
        
        // Parameter patterns
        ".*[?&]ad_.*",
        ".*[?&]adid=.*",
        ".*[?&]utm_.*",
        ".*[?&]gclid=.*",
        ".*[?&]fbclid=.*",
        
        // File patterns
        ".*ads\\.js.*",
        ".*ad\\.js.*",
        ".*banner\\.js.*",
        ".*tracking\\.js.*",
        ".*pixel\\.js.*"
    )
    
    /**
     * Aggressive patterns (Level: KUAT)
     * Bisa break fitur di beberapa situs
     */
    val AGGRESSIVE_PATTERNS = listOf(
        // Semua iframe iklan
        ".*iframe.*ad.*",
        ".*ad.*iframe.*",
        
        // Popup patterns
        ".*popup.*",
        ".*modal.*newsletter.*",
        ".*subscribe.*popup.*",
        
        // Social sharing (bisa dianggap tracking)
        ".*share.*button.*",
        ".*social.*widget.*",
        
        // Video preroll
        ".*preroll.*",
        ".*midroll.*",
        ".*postroll.*",
        
        // Overlay/interstitial
        ".*interstitial.*",
        ".*overlay.*ad.*"
    )
}
