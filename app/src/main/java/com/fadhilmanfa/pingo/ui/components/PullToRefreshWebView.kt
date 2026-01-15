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

/** WebView dengan dukungan Pull-To-Refresh terintegrasi yang lebih mulus. */
@SuppressLint("ClickableViewAccessibility")
class PullRefreshWebView(context: Context) : WebView(context) {
    var onScrollDirectionChange: ((ScrollDirection) -> Unit)? = null
    var onScrollYChange: ((Int) -> Unit)? = null
    var onPullProgress: ((Float) -> Unit)? = null
    var onRefreshTriggered: (() -> Unit)? = null

    // Flag untuk mengabaikan scroll direction change saat navigasi (back/forward)
    var ignoreScrollDirectionChange = false

    private var startY = 0f
    private var isPulling = false
    private var pullDistance = 0f

    private val pullThresholdDp = 80f
    private val maxPullDistanceDp = 180f
    private var pullThresholdPx = 0f
    private var maxPullDistancePx = 0f

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private var lastScrollYPos = 0
    private var lastScrollTime = 0L
    private val scrollThrottleMs = 100L

    init {
        overScrollMode = View.OVER_SCROLL_NEVER
        val density = context.resources.displayMetrics.density
        pullThresholdPx = pullThresholdDp * density
        maxPullDistancePx = maxPullDistanceDp * density
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        val y = event.y
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startY = y
                isPulling = false
            }
            MotionEvent.ACTION_MOVE -> {
                val isAtTop = scrollY == 0
                val totalDeltaY = y - startY
                // Cegah WebView scroll jika kita mulai menarik ke bawah dari posisi paling atas
                if (isAtTop && totalDeltaY > touchSlop) {
                    return true
                }
            }
        }
        return super.onInterceptTouchEvent(event)
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

                if (isAtTop && !isPulling && totalDeltaY > touchSlop) {
                    isPulling = true
                    startY = y - touchSlop // Hindari loncatan saat mulai menarik
                }

                if (isPulling) {
                    val currentDeltaY = y - startY
                    if (currentDeltaY < 0) {
                        isPulling = false
                        onPullProgress?.invoke(0f)
                        return super.onTouchEvent(event)
                    }

                    // Resistance logic (semakin ditarik semakin berat)
                    val rawPull = currentDeltaY.coerceAtLeast(0f)
                    pullDistance = min(rawPull * 0.55f, maxPullDistancePx)

                    onPullProgress?.invoke(pullDistance / pullThresholdPx)
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

        // Abaikan jika sedang navigasi (back/forward/load halaman baru)
        if (ignoreScrollDirectionChange) {
            lastScrollYPos = t // Sync posisi agar tidak trigger saat flag di-reset
            return
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastScrollTime < scrollThrottleMs) return

        val delta = t - lastScrollYPos
        if (abs(delta) > 10) { // Lebih toleran agar tidak terlalu sensitif
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
    fun onUrlChanged(url: String) {
        mainHandler.post { onUrlUpdate(url) }
    }

    @JavascriptInterface
    fun onContentExtracted(jsonContent: String) {
        mainHandler.post { onContentExtracted?.invoke(jsonContent) }
    }
}

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
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
        onDownloadStart:
                ((
                        url: String,
                        userAgent: String,
                        contentDisposition: String,
                        mimetype: String,
                        contentLength: Long) -> Unit)? =
                null,
        webViewRef: (WebView) -> Unit
) {
    val density = LocalDensity.current

    var pullProgress by remember { mutableFloatStateOf(0f) }
    var isRefreshing by remember { mutableStateOf(false) }
    var shouldBounceBack by remember { mutableStateOf(false) }
    var loadingStartTime by remember { mutableLongStateOf(0L) }
    val minLoadingDuration = 600L

    // Kita gunakan State object agar pembacaan value di graphicsLayer tidak memicu recomposition
    // seluruh WebView
    val animatedProgressState =
            animateFloatAsState(
                    targetValue =
                            when {
                                isRefreshing -> 1f
                                shouldBounceBack -> 0f
                                else -> pullProgress
                            },
                    animationSpec =
                            if (shouldBounceBack || isRefreshing) {
                                spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                )
                            } else {
                                snap()
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
    val spinRotation by
            infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec =
                            infiniteRepeatable(
                                    animation = tween(durationMillis = 1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                            ),
                    label = "SpinRotation"
            )

    val jsInterface = remember {
        PullRefreshWebAppInterface(onScrollDirectionChange, onUrlChange, onContentExtracted)
    }

    Box(modifier = modifier) {
        // Indikator Pull-to-Refresh
        if (animatedProgressState.value > 0.01f || isRefreshing) {
            Box(
                    modifier =
                            Modifier.fillMaxWidth().zIndex(10f).graphicsLayer {
                                val progress = animatedProgressState.value
                                val thresholdPx = 80.dp.toPx()

                                // Gunakan translationY daripada Modifier.offset agar lebih mulus
                                translationY = (min(progress, 1.5f) * thresholdPx) - 60.dp.toPx()

                                scaleX = min(progress, 1f)
                                scaleY = min(progress, 1f)
                                alpha = (progress * 2f).coerceIn(0f, 1f)
                            },
                    contentAlignment = Alignment.TopCenter
            ) {
                Box(
                        modifier =
                                Modifier.graphicsLayer {
                                    rotationZ =
                                            if (isRefreshing) spinRotation
                                            else animatedProgressState.value * 160f
                                }
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                                modifier = Modifier.size(38.dp),
                                color = Secondary,
                                strokeWidth = 3.dp
                        )
                    } else {
                        CircularProgressIndicator(
                                progress = { min(animatedProgressState.value, 1f) },
                                modifier = Modifier.size(38.dp),
                                color = Secondary,
                                strokeWidth = 3.dp
                        )
                    }
                }
            }
        }

        AndroidView(
                modifier =
                        Modifier.fillMaxSize().graphicsLayer {
                            val progress = animatedProgressState.value
                            // Halaman ikut turun dengan damping yang lebih natural
                            translationY = min(progress * 45.dp.toPx(), 90.dp.toPx())
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

                        layoutParams =
                                ViewGroup.LayoutParams(
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

                        setDownloadListener {
                                url,
                                userAgent,
                                contentDisposition,
                                mimetype,
                                contentLength ->
                            onDownloadStart?.invoke(
                                    url,
                                    userAgent,
                                    contentDisposition,
                                    mimetype,
                                    contentLength
                            )
                        }

                        val adBlockManager = AdBlockManager.getInstance(context)
                        webViewClient =
                                object : WebViewClient() {
                                    override fun onPageStarted(
                                            view: WebView?,
                                            url: String?,
                                            favicon: Bitmap?
                                    ) {
                                        // Aktifkan flag saat halaman mulai loading untuk mencegah
                                        // navbar collapse/expand
                                        ignoreScrollDirectionChange = true
                                        onLoadingChange(true)
                                        url?.let { onUrlChange(it) }
                                    }
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        onLoadingChange(false)
                                        if (isRefreshing) {
                                            val elapsed =
                                                    System.currentTimeMillis() - loadingStartTime
                                            val remainingDelay =
                                                    (minLoadingDuration - elapsed).coerceAtLeast(0)

                                            Handler(Looper.getMainLooper())
                                                    .postDelayed(
                                                            {
                                                                isRefreshing = false
                                                                shouldBounceBack = true
                                                            },
                                                            remainingDelay
                                                    )
                                        }
                                        view?.let {
                                            onCanGoBackChange(it.canGoBack())
                                            onCanGoForwardChange(it.canGoForward())
                                            it.url?.let { u -> onUrlChange(u) }
                                            it.title?.let { t -> onTitleChange(t) }
                                        }
                                        // Reset flag setelah halaman selesai loading dengan sedikit
                                        // delay
                                        // agar scroll restoration tidak trigger navbar
                                        // collapse/expand
                                        Handler(Looper.getMainLooper())
                                                .postDelayed(
                                                        { ignoreScrollDirectionChange = false },
                                                        300
                                                )
                                    }
                                    override fun shouldInterceptRequest(
                                            view: WebView?,
                                            request: WebResourceRequest?
                                    ): WebResourceResponse? {
                                        val u = request?.url?.toString() ?: return null
                                        if (adBlockManager.shouldBlock(u)) {
                                            adBlockManager.incrementBlockedCount()
                                            return WebResourceResponse(
                                                    "text/plain",
                                                    "utf-8",
                                                    ByteArrayInputStream(byteArrayOf())
                                            )
                                        }
                                        return null
                                    }
                                }
                        webChromeClient =
                                object : WebChromeClient() {
                                    override fun onProgressChanged(
                                            view: WebView?,
                                            newProgress: Int
                                    ) {
                                        onProgressChange(newProgress / 100f)
                                    }
                                }
                        webViewRef(this)
                        if (savedState != null) restoreState(savedState) else loadUrl(url)
                    }
                },
                update = { view ->
                    view.setDownloadListener {
                            url,
                            userAgent,
                            contentDisposition,
                            mimetype,
                            contentLength ->
                        onDownloadStart?.invoke(
                                url,
                                userAgent,
                                contentDisposition,
                                mimetype,
                                contentLength
                        )
                    }
                }
        )
    }
}
