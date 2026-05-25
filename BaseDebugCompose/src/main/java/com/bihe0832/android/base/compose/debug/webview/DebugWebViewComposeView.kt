package com.bihe0832.android.base.compose.debug.webview

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.item.DebugTips
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.lib.log.ZLog

/**
 * WebView 调试页面
 *
 * 用于验证 onRenderProcessGone 的处理逻辑。
 *
 * 测试原理：
 * 利用 Chromium 内置的 `chrome://crash` 页面触发 Renderer 崩溃。
 * WebView 加载该 URL 时，Renderer 子进程会立即崩溃，
 * 从而触发 WebViewClient.onRenderProcessGone() 回调。
 *
 * 替代方案（如 chrome://crash 被禁用）：
 * 通过加载一个极度消耗内存的 JS 页面，使 Renderer 被 OOM 杀掉：
 * ```javascript
 * // 在 WebView 中执行：分配巨量内存触发 OOM
 * var a = []; while(true) { a.push(new ArrayBuffer(1024*1024*100)); }
 * ```
 */
@Composable
fun DebugWebViewComposeView() {
    val context = LocalContext.current
    var showWebView by remember { mutableStateOf(false) }

    if (showWebView) {
        DebugWebViewPage(
            onBack = { showWebView = false }
        )
    } else {
        DebugContent {
            DebugItem("打开 WebView 测试页面") {
                showWebView = true
            }
            DebugTips(
                "测试 onRenderProcessGone 流程：\n\n" +
                        "1. 点击「打开 WebView 测试页面」，等待页面正常加载\n" +
                        "2. 点击页面内的「触发 Renderer 崩溃」按钮\n" +
                        "3. 观察：\n" +
                        "   - 应用不闪退 ✓\n" +
                        "   - 页面展示「onRenderProcessGone 触发成功」✓\n" +
                        "   - logcat 有 onRenderProcessGone 日志 ✓\n" +
                        "4. 点击「重试」验证 WebView 重建是否正常"
            )
        }
    }
}

private const val TAG = "DebugWebView"

/**
 * 触发 Renderer 崩溃的 URL。
 *
 * Chromium 内置的 chrome://crash 会导致 Renderer 子进程立即崩溃。
 * 在 Android WebView 中，该 URL 会被映射为 "chrome://crash/"。
 * 部分系统版本可能禁用了该 URL，备选方案是通过 JS 分配巨量内存触发 OOM。
 */
private const val CRASH_RENDERER_URL = "chrome://crash"

/**
 * 备选方案：通过 JS 分配大量内存触发 Renderer OOM 崩溃。
 * 这是在 chrome://crash 被禁用时的 fallback 方案。
 */
private const val OOM_CRASH_JS = """
    javascript:void(function(){
        var a=[];
        try{
            while(true){a.push(new ArrayBuffer(128*1024*1024));}
        }catch(e){}
    })()
"""

/**
 * WebView 测试页面
 *
 * 内嵌一个真实 WebView，当 Renderer 进程被杀后展示错误页，验证 onRenderProcessGone 处理逻辑。
 */
@Composable
private fun DebugWebViewPage(onBack: () -> Unit) {
    val context = LocalContext.current
    var isRendererGone by remember { mutableStateOf(false) }
    var rendererGoneInfo by remember { mutableStateOf("") }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    if (isRendererGone) {
        // Renderer 已死亡，展示错误页（模拟真实业务中 BaseWebViewFragment 的错误页行为）
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "✅ onRenderProcessGone 触发成功",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "页面加载失败 — WebView Renderer 进程已崩溃",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (rendererGoneInfo.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = rendererGoneInfo,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = {
                    isRendererGone = false
                    rendererGoneInfo = ""
                    webViewRef = null
                }) {
                    Text("重试（重建 WebView）")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onBack) {
                    Text("返回")
                }
            }
        }
    } else {
        // 正常展示 WebView + 触发按钮
        Column(modifier = Modifier.fillMaxSize()) {
            // 触发按钮区域
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Button(
                    onClick = {
                        webViewRef?.loadUrl(CRASH_RENDERER_URL)
                        ZLog.i(TAG, "Loading chrome://crash to trigger Renderer crash")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("触发 Renderer 崩溃（chrome://crash）")
                }
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = {
                        webViewRef?.loadUrl(OOM_CRASH_JS.trimIndent().replace("\n", ""))
                        ZLog.i(TAG, "Executing OOM JS to trigger Renderer crash")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("备选：JS OOM 触发崩溃")
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "页面加载完成后，点击上方按钮触发 Renderer 崩溃",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            // WebView
            AndroidView(
                factory = { ctx ->
                    createDebugWebView(ctx) { view, detail ->
                        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            "didCrash=${detail.didCrash()}, rendererPriority=${detail.rendererPriorityAtExit()}"
                        } else {
                            "API < 26, detail unavailable"
                        }
                        ZLog.e(TAG, "onRenderProcessGone: $info")

                        // 将出问题的 WebView 从父布局摘掉并销毁
                        view?.let {
                            (it.parent as? android.view.ViewGroup)?.removeView(it)
                            it.destroy()
                        }
                        webViewRef = null

                        // 在主线程更新 UI 状态
                        (ctx as? android.app.Activity)?.runOnUiThread {
                            rendererGoneInfo = info
                            isRendererGone = true
                        }

                        // 返回 true 告诉 Chromium "已处理"，阻止主进程被 kill -9
                        true
                    }.also { webViewRef = it }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

/**
 * 创建用于调试的 WebView 实例
 */
@SuppressLint("SetJavaScriptEnabled")
private fun createDebugWebView(
    context: Context,
    onRenderProcessGone: (WebView?, RenderProcessGoneDetail) -> Boolean
): WebView {
    return WebView(context).apply {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        webViewClient = object : WebViewClient() {
            override fun onRenderProcessGone(
                view: WebView?,
                detail: RenderProcessGoneDetail?
            ): Boolean {
                if (detail != null) {
                    return onRenderProcessGone(view, detail)
                }
                // detail 为 null 时的降级处理
                view?.let {
                    (it.parent as? android.view.ViewGroup)?.removeView(it)
                    it.destroy()
                }
                return true
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                ZLog.e(TAG, "onReceivedError: ${error?.description}")
            }
        }
        webChromeClient = WebChromeClient()
        // 先加载正常页面，确保 Renderer 进程存活
        loadUrl("https://www.qq.com")
    }
}
