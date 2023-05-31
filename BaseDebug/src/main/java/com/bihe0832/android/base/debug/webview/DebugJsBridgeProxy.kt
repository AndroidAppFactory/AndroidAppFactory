package com.bihe0832.android.base.debug.webview

import android.app.Activity
import android.net.Uri
import android.view.View
import android.widget.AbsoluteLayout
import com.bihe0832.android.common.webview.log.MyBaseJsBridgeProxy
import com.bihe0832.android.framework.ZixieContext.applicationContext
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.ConvertUtils
import com.bihe0832.android.lib.utils.os.DisplayUtil
import com.bihe0832.android.lib.webview.BaseWebView
import com.bihe0832.android.lib.webview.jsbridge.JsResult

/**
 * Created by hardyshi on 2016/10/20.
 */
class DebugJsBridgeProxy(webView: BaseWebView, activity: Activity) : MyBaseJsBridgeProxy(webView, activity) {

    private var mNativeView: View? = null

    fun setNativeView(mNativeView: View?) {
        this.mNativeView = mNativeView
    }

    fun showNativeView(uri: Uri?, seqid: Int, method: String?, callbackFun: String?) {
        try {
            if (null != mNativeView) {
                ThreadManager.getInstance().runOnUIThread {
                    mNativeView!!.visibility = View.VISIBLE
                    resetNativeViewHeight(mNativeView!!.height)
                    mJsBridge.response(callbackFun, seqid, method, "showNativeView success")
                }
            } else {
                mJsBridge.responseFail(callbackFun, seqid, method, JsResult.Code_IllegalArgument)
            }
        } catch (var11: Exception) {
            mJsBridge.responseFail(callbackFun, seqid, method, JsResult.Code_Java_Exception)
        }
    }

    fun hideNativeView(uri: Uri?, seqid: Int, method: String?, callbackFun: String?) {
        try {
            if (null != mNativeView) {
                ThreadManager.getInstance().runOnUIThread {
                    mNativeView!!.visibility = View.GONE
                    resetNativeViewHeight(0)
                    mJsBridge.response(callbackFun, seqid, method, null)
                }
            } else {
                mJsBridge.responseFail(callbackFun, seqid, method, JsResult.Code_IllegalArgument)
            }
        } catch (var11: Exception) {
            mJsBridge.responseFail(callbackFun, seqid, method, JsResult.Code_Java_Exception)
        }
    }

    private fun resetNativeViewHeight(height: Int) {
        webView.evaluateJavascript("javaScript:getNativeViewPosition()") { value ->
            ThreadManager.getInstance().runOnUIThread {
                val top = ConvertUtils.parseFloat(value, -1f)
                val params = mNativeView!!.layoutParams as AbsoluteLayout.LayoutParams
                params.y = DisplayUtil.dip2pxWithDefaultDensity(applicationContext, top)
                mNativeView!!.layoutParams = params
                webView.loadUrl("javaScript:setNativeViewHeight(" + DisplayUtil.px2dipWithDefaultDensity(applicationContext, height.toFloat()) + ")")

            }
        }
    }
}