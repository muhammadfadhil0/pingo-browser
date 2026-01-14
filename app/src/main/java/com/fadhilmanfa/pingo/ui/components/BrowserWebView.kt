package com.fadhilmanfa.pingo.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.NestedScrollingChild3
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat
import com.fadhilmanfa.pingo.data.adblock.AdBlockManager
import java.io.ByteArrayInputStream
import kotlin.math.abs

/**
 * Custom WebView dengan dukungan Nested Scrolling penuh untuk Pull-To-Refresh Compose
 */
@SuppressLint("ClickableViewAccessibility")
class ScrollAwareWebView(context: Context) : WebView(context), NestedScrollingChild3 {
    var onScrollDirectionChange: ((ScrollDirection) -> Unit)? = null
    var onScrollYChange: ((Int) -> Unit)? = null
    
    private val childHelper = NestedScrollingChildHelper(this)
    private var lastTouchY = 0
    private val scrollConsumed = IntArray(2)
    private val scrollOffset = IntArray(2)
    
    // Untuk mendeteksi arah scroll
    private var lastScrollYPos = 0
    private var lastScrollTime = 0L
    private val scrollThrottleMs = 80L
    
    init {
        // Matikan efek glow bawaan Android agar tidak menutupi PullToRefresh
        overScrollMode = View.OVER_SCROLL_NEVER
        isNestedScrollingEnabled = true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.actionMasked
        val y = event.y.toInt()
        
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchY = y
                // Mulai nested scroll secara vertikal
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, ViewCompat.TYPE_TOUCH)
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaY = lastTouchY - y
                lastTouchY = y
                
                // Jika WebView sudah di paling atas DAN user menarik ke bawah (deltaY negatif/menarik ke bawah)
                // Langsung teruskan semua scroll ke parent untuk memicu PullToRefresh
                val isAtTop = scrollY == 0
                val isPullingDown = deltaY < 0
                
                if (isAtTop && isPullingDown) {
                    // Dispatch langsung ke parent tanpa consume internal
                    scrollConsumed[0] = 0
                    scrollConsumed[1] = 0
                    dispatchNestedPreScroll(0, deltaY, scrollConsumed, scrollOffset, ViewCompat.TYPE_TOUCH)
                    
                    // Teruskan semua unconsumed scroll ke parent
                    val unconsumedY = deltaY - scrollConsumed[1]
                    dispatchNestedScroll(0, 0, 0, unconsumedY, scrollOffset, ViewCompat.TYPE_TOUCH)
                    
                    return true
                }
                
                // Untuk scroll normal (ke atas atau saat tidak di top)
                var adjustedDeltaY = deltaY
                
                // 1. Tawarkan ke Parent (Compose) sebelum kita scroll sendiri
                if (dispatchNestedPreScroll(0, adjustedDeltaY, scrollConsumed, scrollOffset, ViewCompat.TYPE_TOUCH)) {
                    adjustedDeltaY -= scrollConsumed[1]
                }
                
                val oldScrollY = scrollY
                // 2. WebView melakukan scroll internal
                val result = super.onTouchEvent(event)
                
                val scrollByWebView = scrollY - oldScrollY
                val unconsumedY = adjustedDeltaY - scrollByWebView
                
                // 3. Dispatch sisa scroll ke Parent (Ini yang memicu PullToRefresh)
                dispatchNestedScroll(0, scrollByWebView, 0, unconsumedY, scrollOffset, ViewCompat.TYPE_TOUCH)
                
                return result
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                stopNestedScroll(ViewCompat.TYPE_TOUCH)
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        onScrollYChange?.invoke(t)
        
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastScrollTime < scrollThrottleMs) return
        
        val delta = t - lastScrollYPos
        if (abs(delta) > 5) {
            lastScrollTime = currentTime
            lastScrollYPos = t
            if (delta > 0) onScrollDirectionChange?.invoke(ScrollDirection.DOWN)
            else onScrollDirectionChange?.invoke(ScrollDirection.UP)
        }
    }

    // --- Implementasi Delegasi NestedScrollingChild3 ---
    override fun setNestedScrollingEnabled(enabled: Boolean) { childHelper.isNestedScrollingEnabled = enabled }
    override fun isNestedScrollingEnabled(): Boolean = childHelper.isNestedScrollingEnabled
    override fun startNestedScroll(axes: Int, type: Int): Boolean = childHelper.startNestedScroll(axes, type)
    override fun stopNestedScroll(type: Int) = childHelper.stopNestedScroll(type)
    override fun hasNestedScrollingParent(type: Int): Boolean = childHelper.hasNestedScrollingParent(type)
    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?, type: Int, consumed: IntArray) {
        childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type, consumed)
    }
    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?, type: Int): Boolean {
        return childHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type)
    }
    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?, type: Int): Boolean {
        return childHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
    }
}

class WebAppInterface(
    private val onScrollDirection: (ScrollDirection) -> Unit,
    private val onUrlUpdate: (String) -> Unit,
    private val onContentExtracted: ((String) -> Unit)? = null
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    
    @JavascriptInterface
    fun onScrollDetected(direction: String) {
        mainHandler.post {
            when (direction) {
                "DOWN" -> onScrollDirection(ScrollDirection.DOWN)
                "UP" -> onScrollDirection(ScrollDirection.UP)
            }
        }
    }
    
    @JavascriptInterface
    fun onUrlChanged(url: String) { mainHandler.post { onUrlUpdate(url) } }
    
    @JavascriptInterface
    fun onContentExtracted(jsonContent: String) { mainHandler.post { onContentExtracted?.invoke(jsonContent) } }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserWebView(
    modifier: Modifier = Modifier,
    url: String,
    savedState: Bundle? = null,
    onUrlChange: (String) -> Unit,
    onTitleChange: (String) -> Unit = {},
    onLoadingChange: (Boolean) -> Unit,
    onProgressChange: (Float) -> Unit,
    onCanGoBackChange: (Boolean) -> Unit,
    onCanGoForwardChange: (Boolean) -> Unit,
    onScrollDirectionChange: (ScrollDirection) -> Unit,
    onScrollYChange: (Int) -> Unit = {},
    onContentExtracted: ((String) -> Unit)? = null,
    webViewRef: (WebView) -> Unit
) {
    val jsInterface = remember {
        WebAppInterface(onScrollDirectionChange, onUrlChange, onContentExtracted)
    }
    
    AndroidView(
        modifier = modifier,
        factory = { context ->
            ScrollAwareWebView(context).apply {
                this.onScrollDirectionChange = onScrollDirectionChange
                this.onScrollYChange = onScrollYChange
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                setBackgroundColor(Color.WHITE)
                addJavascriptInterface(jsInterface, "Android")

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }

                val adBlockManager = AdBlockManager.getInstance(context)
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
                            it.url?.let { u -> onUrlChange(u) }
                            it.title?.let { t -> onTitleChange(t) }
                        }
                    }
                    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                        val u = request?.url?.toString() ?: return null
                        if (adBlockManager.shouldBlock(u)) {
                            adBlockManager.incrementBlockedCount()
                            return WebResourceResponse("text/plain", "utf-8", ByteArrayInputStream(byteArrayOf()))
                        }
                        return null
                    }
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        onProgressChange(newProgress / 100f)
                    }
                }
                webViewRef(this)
                if (savedState != null) restoreState(savedState) else loadUrl(url)
            }
        }
    )
}

enum class ScrollDirection { UP, DOWN }
