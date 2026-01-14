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
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.fadhilmanfa.pingo.data.adblock.AdBlockManager
import com.fadhilmanfa.pingo.ui.theme.Secondary
import java.io.ByteArrayInputStream
import kotlin.math.abs
import kotlin.math.min

/**
 * WebView dengan dukungan Pull-To-Refresh terintegrasi yang responsif.
 */
@SuppressLint("ClickableViewAccessibility")
class PullRefreshWebView(context: Context) : WebView(context) {
    var onScrollDirectionChange: ((ScrollDirection) -> Unit)? = null
    var onScrollYChange: ((Int) -> Unit)? = null
    var onPullProgress: ((Float) -> Unit)? = null 
    var onRefreshTriggered: (() -> Unit)? = null
    
    private var startY = 0f
    private var isPulling = false
    private var pullDistance = 0f
    
    private val pullThresholdDp = 75f 
    private val maxPullDistanceDp = 160f
    private var pullThresholdPx = 0f
    private var maxPullDistancePx = 0f
    
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    
    private var lastScrollYPos = 0
    private var lastScrollTime = 0L
    private val scrollThrottleMs = 80L
    
    init {
        overScrollMode = View.OVER_SCROLL_NEVER
        val density = context.resources.displayMetrics.density
        pullThresholdPx = pullThresholdDp * density
        maxPullDistancePx = maxPullDistanceDp * density
    }
    
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val y = event.y
        
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startY = y
                isPulling = false
                pullDistance = 0f
            }
            
            MotionEvent.ACTION_MOVE -> {
                val isAtTop = scrollY == 0
                val totalDeltaY = y - startY
                
                // Deteksi inisiasi pull
                if (isAtTop && !isPulling && totalDeltaY > touchSlop) {
                    isPulling = true
                }
                
                if (isPulling) {
                    // Batalkan jika ditarik balik ke atas melewati titik awal
                    if (totalDeltaY < 0) {
                        isPulling = false
                        onPullProgress?.invoke(0f)
                        return super.onTouchEvent(event)
                    }
                    
                    // Hitung jarak tarik dengan resistance (0.5f agar terasa stabil)
                    val rawPull = (totalDeltaY - touchSlop).coerceAtLeast(0f)
                    pullDistance = min(rawPull * 0.5f, maxPullDistancePx)
                    
                    // Beri tahu progress ke UI
                    onPullProgress?.invoke(pullDistance / pullThresholdPx)
                    
                    // Konsumsi event agar WebView tidak scrolling
                    return true 
                }
            }
            
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isPulling) {
                    if (pullDistance >= pullThresholdPx) {
                        onRefreshTriggered?.invoke()
                    }
                    isPulling = false
                    onPullProgress?.invoke(-1f) // Signal untuk start bounce-back animation
                    return true
                }
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
}

class PullRefreshWebAppInterface(
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PullToRefreshWebView(
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
    val density = LocalDensity.current
    
    var pullProgress by remember { mutableFloatStateOf(0f) }
    var isRefreshing by remember { mutableStateOf(false) }
    var shouldBounceBack by remember { mutableStateOf(false) }
    var loadingStartTime by remember { mutableStateOf(0L) }
    val minLoadingDuration = 600L
    
    val animatedProgress by animateFloatAsState(
        targetValue = when {
            isRefreshing -> 1f
            shouldBounceBack -> 0f
            else -> pullProgress
        },
        animationSpec = if (shouldBounceBack || isRefreshing) {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        } else {
            snap() // Snap agar indikator mengikuti jari secara instan saat drag
        },
        label = "PullProgress",
        finishedListener = { finalValue ->
            if (finalValue == 0f && shouldBounceBack) {
                shouldBounceBack = false
                pullProgress = 0f
            }
        }
    )
    
    val infiniteTransition = rememberInfiniteTransition(label = "SpinnerRotation")
    val spinRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SpinRotation"
    )
    
    val jsInterface = remember {
        PullRefreshWebAppInterface(onScrollDirectionChange, onUrlChange, onContentExtracted)
    }
    
    Box(modifier = modifier) {
        val indicatorOffset = with(density) { (min(animatedProgress, 1.4f) * 85f).dp }
        val indicatorScale = min(animatedProgress, 1f)
        val indicatorAlpha = min(animatedProgress * 2f, 1f)
        
        if (animatedProgress > 0.01f || isRefreshing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(10f)
                    .offset(y = indicatorOffset - 48.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = indicatorScale
                            scaleY = indicatorScale
                            alpha = indicatorAlpha
                            rotationZ = if (isRefreshing) spinRotation else animatedProgress * 160f
                        }
                ) {
                    if (isRefreshing) {
                        LoadingIndicator(
                            modifier = Modifier.size(38.dp),
                            color = Secondary
                        )
                    } else {
                        CircularProgressIndicator(
                            progress = { min(animatedProgress, 1f) },
                            modifier = Modifier.size(38.dp),
                            color = Secondary,
                            strokeWidth = 3.dp
                        )
                    }
                }
            }
        }
        
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = min(animatedProgress * with(density) { 20.dp.toPx() }, with(density) { 50.dp.toPx() })
                },
            factory = { context ->
                PullRefreshWebView(context).apply {
                    this.onScrollDirectionChange = onScrollDirectionChange
                    this.onScrollYChange = onScrollYChange
                    
                    this.onPullProgress = { progress ->
                        if (progress == -1f) {
                            if (!isRefreshing) shouldBounceBack = true
                        } else {
                            shouldBounceBack = false
                            pullProgress = progress
                        }
                    }
                    
                    this.onRefreshTriggered = {
                        isRefreshing = true
                        loadingStartTime = System.currentTimeMillis()
                        pullProgress = 1f
                        reload()
                    }
                    
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
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
                            if (isRefreshing) {
                                val elapsed = System.currentTimeMillis() - loadingStartTime
                                val remainingDelay = (minLoadingDuration - elapsed).coerceAtLeast(0)
                                
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    isRefreshing = false
                                    shouldBounceBack = true
                                }, remainingDelay)
                            }
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
}
