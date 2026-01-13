package com.fadhilmanfa.pingo.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.abs

/**
 * Custom WebView dengan multiple scroll detection methods:
 * 1. Native onScrollChanged (untuk halaman normal)
 * 2. Touch-based detection (untuk halaman sandboxed seperti AMP)
 */
@SuppressLint("ClickableViewAccessibility")
class ScrollAwareWebView(context: Context) : WebView(context) {
    var onScrollDirectionChange: ((ScrollDirection) -> Unit)? = null
    
    // For native scroll detection
    private var lastScrollY = 0
    private var lastScrollTime = 0L
    private val scrollThrottleMs = 80L
    
    // For touch-based scroll detection
    private var touchStartY = 0f
    private var lastTouchY = 0f
    private var touchMoveCount = 0
    private var lastTouchDirection: ScrollDirection? = null
    private var lastTouchScrollTime = 0L
    
    init {
        // Setup touch listener for scroll detection
        setOnTouchListener { _, event ->
            handleTouchForScroll(event)
            false // Return false to allow normal touch handling
        }
    }
    
    private fun handleTouchForScroll(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartY = event.y
                lastTouchY = event.y
                touchMoveCount = 0
                lastTouchDirection = null
            }
            MotionEvent.ACTION_MOVE -> {
                val currentY = event.y
                val delta = lastTouchY - currentY // Positive = scroll down, Negative = scroll up
                touchMoveCount++
                
                val currentTime = System.currentTimeMillis()
                
                // Trigger after a few move events to avoid false positives
                // and throttle to avoid too many callbacks
                if (touchMoveCount > 3 && abs(delta) > 8 && 
                    currentTime - lastTouchScrollTime > scrollThrottleMs) {
                    
                    val direction = if (delta > 0) ScrollDirection.DOWN else ScrollDirection.UP
                    
                    // Only trigger if direction changed or it's been a while
                    if (direction != lastTouchDirection || currentTime - lastTouchScrollTime > 200) {
                        onScrollDirectionChange?.invoke(direction)
                        lastTouchDirection = direction
                        lastTouchScrollTime = currentTime
                    }
                    
                    lastTouchY = currentY
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchMoveCount = 0
                lastTouchDirection = null
            }
        }
    }
    
    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastScrollTime < scrollThrottleMs) return
        
        val delta = t - lastScrollY
        
        // Threshold lebih rendah untuk lebih responsif
        if (abs(delta) > 5) {
            lastScrollTime = currentTime
            lastScrollY = t
            
            if (delta > 0) {
                onScrollDirectionChange?.invoke(ScrollDirection.DOWN)
            } else {
                onScrollDirectionChange?.invoke(ScrollDirection.UP)
            }
        }
    }
}

/**
 * JavaScript Interface untuk komunikasi dari webpage ke Android
 * Mendeteksi scroll dan URL changes dari dalam webpage
 */
class WebAppInterface(
    private val onScrollDirection: (ScrollDirection) -> Unit,
    private val onUrlUpdate: (String) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastScrollTime = 0L
    private val scrollThrottleMs = 100L
    
    @JavascriptInterface
    fun onScrollDetected(direction: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastScrollTime > scrollThrottleMs) {
            lastScrollTime = currentTime
            mainHandler.post {
                when (direction) {
                    "DOWN" -> onScrollDirection(ScrollDirection.DOWN)
                    "UP" -> onScrollDirection(ScrollDirection.UP)
                }
            }
        }
    }
    
    @JavascriptInterface
    fun onUrlChanged(url: String) {
        mainHandler.post {
            onUrlUpdate(url)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    modifier: Modifier = Modifier,
    url: String,
    navigationBarHeight: Int = 0,
    onUrlChange: (String) -> Unit,
    onLoadingChange: (Boolean) -> Unit,
    onProgressChange: (Float) -> Unit,
    onCanGoBackChange: (Boolean) -> Unit,
    onCanGoForwardChange: (Boolean) -> Unit,
    onScrollDirectionChange: (ScrollDirection) -> Unit,
    webViewRef: (WebView) -> Unit
) {
    // Remember the JavaScript interface to avoid recreation
    val jsInterface = remember {
        WebAppInterface(
            onScrollDirection = onScrollDirectionChange,
            onUrlUpdate = onUrlChange
        )
    }
    
    // JavaScript code to inject for scroll detection
    val scrollDetectionScript = """
        (function() {
            // Hindari double injection
            if (window._pingoScrollInjected) return;
            window._pingoScrollInjected = true;
            
            // Fungsi untuk mendapatkan scroll position dari berbagai sumber
            function getScrollY() {
                // Cek berbagai kemungkinan scroll container
                // AMP pages sering menggunakan custom scroll container
                const scrollContainers = [
                    document.scrollingElement,
                    document.documentElement,
                    document.body,
                    document.querySelector('amp-story'),
                    document.querySelector('.i-amphtml-scroll-container'),
                    document.querySelector('[overflow]'),
                    document.querySelector('main'),
                    document.querySelector('article')
                ];
                
                for (const container of scrollContainers) {
                    if (container && container.scrollTop > 0) {
                        return container.scrollTop;
                    }
                }
                
                return window.scrollY || window.pageYOffset || 0;
            }
            
            let lastScrollY = getScrollY();
            let ticking = false;
            
            function handleScroll() {
                const currentScrollY = getScrollY();
                const delta = currentScrollY - lastScrollY;
                
                // Threshold untuk menghindari micro-scroll
                if (Math.abs(delta) > 3) {
                    const direction = delta > 0 ? 'DOWN' : 'UP';
                    if (window.Android && window.Android.onScrollDetected) {
                        window.Android.onScrollDetected(direction);
                    }
                    lastScrollY = currentScrollY;
                }
                ticking = false;
            }
            
            function requestScrollCheck() {
                if (!ticking) {
                    window.requestAnimationFrame(handleScroll);
                    ticking = true;
                }
            }
            
            // Passive scroll listener pada window
            window.addEventListener('scroll', requestScrollCheck, { passive: true, capture: true });
            
            // Juga listen pada document untuk AMP dan iframe
            document.addEventListener('scroll', requestScrollCheck, { passive: true, capture: true });
            
            // Monitor semua scrollable elements
            const observeScrollContainers = () => {
                const scrollables = document.querySelectorAll('[style*="overflow"], [class*="scroll"], main, article, .content');
                scrollables.forEach(el => {
                    el.addEventListener('scroll', requestScrollCheck, { passive: true });
                });
            };
            
            // Observe setelah DOM ready dan setiap ada perubahan DOM
            if (document.readyState === 'loading') {
                document.addEventListener('DOMContentLoaded', observeScrollContainers);
            } else {
                observeScrollContainers();
            }
            
            // Re-observe ketika ada element baru
            const domObserver = new MutationObserver(observeScrollContainers);
            domObserver.observe(document.body, { childList: true, subtree: true });
            
            // Touch scroll detection - paling reliable untuk mobile
            let touchStartY = 0;
            let lastTouchDirection = null;
            let touchMoveCount = 0;
            
            document.addEventListener('touchstart', function(e) {
                touchStartY = e.touches[0].clientY;
                touchMoveCount = 0;
            }, { passive: true, capture: true });
            
            document.addEventListener('touchmove', function(e) {
                const touchY = e.touches[0].clientY;
                const delta = touchStartY - touchY;
                touchMoveCount++;
                
                // Hanya trigger setelah beberapa touchmove untuk menghindari tap salah deteksi
                if (touchMoveCount > 2 && Math.abs(delta) > 10) {
                    const direction = delta > 0 ? 'DOWN' : 'UP';
                    if (direction !== lastTouchDirection) {
                        if (window.Android && window.Android.onScrollDetected) {
                            window.Android.onScrollDetected(direction);
                        }
                        lastTouchDirection = direction;
                    }
                    touchStartY = touchY;
                }
            }, { passive: true, capture: true });
            
            document.addEventListener('touchend', function() {
                lastTouchDirection = null;
                touchMoveCount = 0;
            }, { passive: true, capture: true });
            
            // Monitor URL changes untuk SPA
            let lastUrl = window.location.href;
            const urlObserver = new MutationObserver(function() {
                if (window.location.href !== lastUrl) {
                    lastUrl = window.location.href;
                    if (window.Android && window.Android.onUrlChanged) {
                        window.Android.onUrlChanged(lastUrl);
                    }
                }
            });
            
            urlObserver.observe(document.body, { childList: true, subtree: true });
            
            // Juga gunakan History API override untuk SPA
            const originalPushState = history.pushState;
            const originalReplaceState = history.replaceState;
            
            history.pushState = function() {
                originalPushState.apply(this, arguments);
                if (window.Android && window.Android.onUrlChanged) {
                    window.Android.onUrlChanged(window.location.href);
                }
            };
            
            history.replaceState = function() {
                originalReplaceState.apply(this, arguments);
                if (window.Android && window.Android.onUrlChanged) {
                    window.Android.onUrlChanged(window.location.href);
                }
            };
            
            window.addEventListener('popstate', function() {
                if (window.Android && window.Android.onUrlChanged) {
                    window.Android.onUrlChanged(window.location.href);
                }
            });
        })();
    """.trimIndent()
    
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ScrollAwareWebView(context).apply {
                // Set native scroll detection callback (fallback for sandboxed pages like AMP)
                this.onScrollDirectionChange = onScrollDirectionChange
                
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                setBackgroundColor(Color.WHITE)
                
                // Tambahkan JavaScript Interface (untuk halaman non-sandboxed)
                addJavascriptInterface(jsInterface, "Android")

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    javaScriptCanOpenWindowsAutomatically = true
                    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        onLoadingChange(true)
                        url?.let { onUrlChange(it) }
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        onLoadingChange(false)
                        view?.let {
                            onCanGoBackChange(it.canGoBack())
                            onCanGoForwardChange(it.canGoForward())
                            // Update URL dari WebView (paling akurat)
                            it.url?.let { finalUrl -> onUrlChange(finalUrl) }
                            
                            // Inject scroll detection script (untuk halaman non-sandboxed)
                            it.evaluateJavascript(scrollDetectionScript, null)
                        }
                    }

                    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                        super.doUpdateVisitedHistory(view, url, isReload)
                        url?.let { onUrlChange(it) }
                        // Re-inject script jika navigasi internal
                        view?.evaluateJavascript(scrollDetectionScript, null)
                    }

                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        // Update URL saat navigasi dimulai
                        request?.url?.toString()?.let { onUrlChange(it) }
                        return false
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        onProgressChange(newProgress / 100f)
                    }
                }

                webViewRef(this)
                loadUrl(url)
            }
        },
        update = { _ ->
            // Navigasi ditangani oleh BrowsingMainPage melalui webView?.loadUrl()
            // Tidak perlu melakukan apapun di sini
        }
    )
}

enum class ScrollDirection {
    UP, DOWN
}
