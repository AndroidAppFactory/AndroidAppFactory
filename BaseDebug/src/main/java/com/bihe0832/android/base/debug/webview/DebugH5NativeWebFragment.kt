package com.bihe0832.android.base.debug.webview

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.os.DisplayUtil
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient


class DebugH5NativeWebFragment : BaseFragment() {
    private val TV_HEIGHT = 50
    private var mWebView: WebView? = null
    private var textView: TextView? = null

    override fun getLayoutID(): Int {
        return R.layout.web_fragment_h5_native
    }


    override fun initView(view: View) {
        mWebView = view.findViewById<WebView>(R.id.webview)
        val settings = mWebView!!.getSettings()
        settings.javaScriptEnabled = true
        textView = getTextView()
        mWebView!!.addView(textView)
        setLocalWeb()
    }

    private fun setLocalWeb() {
        mWebView!!.loadUrl("file:///android_asset/webview_test.html")
        mWebView!!.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                mWebView!!.evaluateJavascript("javaScript:getNativeViewPosition()") { value ->
                    ThreadManager.getInstance().runOnUIThread {
                        textView?.setTranslationY(DisplayUtil.dip2px(context, value.toFloat()).toFloat());
                        mWebView!!.loadUrl("javaScript:setNativeViewHeight(" + TV_HEIGHT + ")")
                    }
                }
            }
        }
    }

    private fun getTextView(): TextView {
        val textView = TextView(context)
        textView.setTextColor(Color.GRAY)
        textView.textSize = 20f
        textView.setBackgroundColor(Color.YELLOW)
        textView.text = "Zixie AAF Webview Debug TextView "
        textView.gravity = Gravity.CENTER
        textView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(context, TV_HEIGHT.toFloat()))
        return textView
    }
}