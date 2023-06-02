package com.bihe0832.android.base.debug.webview

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.os.DisplayUtil
import com.bihe0832.android.common.webview.nativeimpl.NativeWebView


class DebugH5NativeWebFragment : BaseFragment() {
    private var mWebView: NativeWebView? = null
    private var textView: TextView? = null

    override fun getLayoutID(): Int {
        return R.layout.web_fragment_h5_native
    }


    override fun initView(view: View) {
        mWebView = view.findViewById<NativeWebView>(R.id.webview)
        val settings = mWebView!!.getSettings()
        settings.javaScriptEnabled = true
        textView = getTextView(context!!)
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
                        mWebView!!.loadUrl("javaScript:setNativeViewHeight(" + getHeight() + ")")
                    }
                }
            }
        }
    }

    companion object {
        fun getTextView(context: Context): TextView {
            val textView = TextView(context)
            textView.setBackgroundColor(context.resources.getColor(R.color.bihe0832_common_toast_background_color))
            textView.text = "Zixie AAF Webview Debug TextView2 " as CharSequence
            textView.gravity = Gravity.CENTER
            textView.visibility = View.GONE
            textView.layoutParams = ViewGroup.MarginLayoutParams(-1, getHeight())
            return textView
        }

        fun getHeight(): Int {
            return ZixieContext.screenWidth * 9 / 16
        }
    }

}