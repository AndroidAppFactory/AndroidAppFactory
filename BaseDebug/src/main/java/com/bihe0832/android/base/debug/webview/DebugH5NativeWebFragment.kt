package com.bihe0832.android.base.debug.webview

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.lib.aaf.res.R as ResR
import com.bihe0832.android.common.webview.nativeimpl.NativeWebView
import com.bihe0832.android.framework.ui.BaseFragment


class DebugH5NativeWebFragment : BaseFragment() {
    private var mWebView: NativeWebView? = null

    override fun getLayoutID(): Int {
        return R.layout.web_fragment_h5_native
    }


    override fun initView(view: View) {
        mWebView = view.findViewById<NativeWebView>(R.id.webview)
        val settings = mWebView!!.settings
        settings.javaScriptEnabled = true
        setLocalWeb()
    }

    private fun setLocalWeb() {
        mWebView!!.loadUrl("file:///android_asset/webview_test.html")
    }

    companion object {
        fun getTextView(context: Context): TextView {
            val textView = TextView(context)
            textView.setBackgroundColor(context.resources.getColor(ResR.color.bihe0832_common_toast_background_color))
            textView.text = "Zixie AAF Webview Debug TextView2 "
            textView.gravity = Gravity.CENTER
            textView.layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
            return textView
        }
    }

}