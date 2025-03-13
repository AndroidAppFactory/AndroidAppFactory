package com.bihe0832.android.base.debug.webview

import android.app.Activity
import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.AbsoluteLayout
import com.bihe0832.android.common.webview.nativeimpl.NativeJsBridgeProxy
import com.bihe0832.android.common.webview.nativeimpl.NativeWebView
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.jsbridge.JsResult
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.ConvertUtils
import com.bihe0832.android.lib.utils.os.DisplayUtil

/**
 * Created by zixie on 2016/10/20.
 */
class DebugJsBridgeProxy(activity: Activity, webView: NativeWebView) : NativeJsBridgeProxy(activity, webView) {

    private val mViewMap = HashMap<String?, View?>()

    override fun onRefresh() {
        super.onRefresh()
        removeAll()
    }
    private fun addNativeView(key: String, mNativeView: View) {
        mViewMap[key] = mNativeView
    }

    private fun getViewID(uri: Uri?): String {
        return uri?.getQueryParameter("viewKey") ?: ""
    }

    private fun hideView(finalViewID: String?, mNativeView: View?) {
        mNativeView?.visibility = View.INVISIBLE
        resetADHeight(finalViewID, -1)
    }

    private fun removeView(finalViewID: String?) {
        try {
            val mNativeView = mViewMap[finalViewID]
            removeView(finalViewID, mNativeView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeView(finalViewID: String?, mNativeView: View?) {
        try {
            hideView(finalViewID, mNativeView)
            mNativeView?.parent?.let {
                (it as ViewGroup).removeView(mNativeView)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun resetADHeight(key: String?, height: Int) {
        if (!TextUtils.isEmpty(key)) {
            webView?.evaluateJavascript("javaScript:getNativeViewPosition('$key')") { value ->
                ThreadManager.getInstance().runOnUIThread {
                    val mNativeView = mViewMap[key]
                    if (mNativeView != null) {
                        val top = ConvertUtils.parseFloat(value, -1f)
                        if (top > 0) {
                            val params = mNativeView.layoutParams as AbsoluteLayout.LayoutParams
                            params.y = DisplayUtil.dip2pxWithDefaultDensity(ZixieContext.applicationContext, top)
                            mNativeView.layoutParams = params
                            webView.loadUrl("javaScript:setNativeViewHeight('" + key + "'," + DisplayUtil.px2dipWithDefaultDensity(ZixieContext.applicationContext, height.toFloat()) + ")")
                            if (height > 0) {
                                mNativeView.visibility = View.VISIBLE
                            } else {
                                mNativeView.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }

    }

    private fun resetAll() {
        ThreadManager.getInstance().start({
            ThreadManager.getInstance().runOnUIThread {
                val viewInfo: MutableIterator<Map.Entry<String?, View?>> = mViewMap.entries.iterator()
                while (viewInfo.hasNext()) {
                    val (key, view) = viewInfo.next()
                    try {
                        view?.let {
                            if (it.visibility == View.VISIBLE) {
                                resetADHeight(key, it.height)
                            } else if (it.parent == null) {
                                viewInfo.remove()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }, 500L)
    }

    fun removeAll() {
        ThreadManager.getInstance().runOnUIThread {
            val viewInfo: MutableIterator<Map.Entry<String?, View?>> = mViewMap.entries.iterator()
            while (viewInfo.hasNext()) {
                val (key, value) = viewInfo.next()
                try {
                    removeView(key, value)
                    viewInfo.remove()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun addNativeView(uri: Uri, seqid: Int, method: String?, callbackFun: String?) {
        try {
            var finalViewID = getViewID(uri)
            if (!TextUtils.isEmpty(finalViewID)) {
                if (mViewMap[finalViewID] == null) {
                    ThreadManager.getInstance().runOnUIThread {
                        val view = DebugH5NativeWebFragment.getRecycleView(webView.context)
                        webView.addView(view)
                        addNativeView(finalViewID, view)
                        view.visibility = View.GONE
                        resetADHeight(finalViewID, DebugH5NativeWebFragment.getHeight())
                        mJsBridge.response(callbackFun, seqid, method, "")
                    }
                } else {
                    mJsBridge.responseFail(callbackFun, seqid, method, JsResult.Code_IllegalArgument)
                }
            } else {
                mJsBridge.responseFail(callbackFun, seqid, method, JsResult.Code_IllegalArgument)
            }
        } catch (e: Exception) {
            mJsBridge.responseFail(callbackFun, seqid, method, JsResult.Code_Java_Exception)
            e.printStackTrace()
        }
    }

    fun showNativeView(uri: Uri, seqid: Int, method: String?, callbackFun: String?) {
        try {
            var finalViewID = getViewID(uri)
            val mNativeView = mViewMap[finalViewID]
            if (null != mNativeView) {
                ThreadManager.getInstance().runOnUIThread {
                    resetADHeight(finalViewID, mNativeView.height)
                    resetAll()
                    mJsBridge.response(callbackFun, seqid, method, "showNativeView success")
                }
            } else {
                mJsBridge.responseFail(callbackFun, seqid, method, JsResult.Code_IllegalArgument)
            }
        } catch (var11: Exception) {
            mJsBridge.responseFail(callbackFun, seqid, method, JsResult.Code_Java_Exception)
        }
    }

    fun hideNativeView(uri: Uri, seqid: Int, method: String?, callbackFun: String?) {
        try {
            var finalViewID = getViewID(uri)
            val mNativeView = mViewMap[finalViewID]
            if (null != mNativeView) {
                ThreadManager.getInstance().runOnUIThread {
                    hideView(finalViewID, mNativeView)
                    resetAll()
                    mJsBridge.response(callbackFun, seqid, method, null)
                }
            } else {
                mJsBridge.responseFail(callbackFun, seqid, method, JsResult.Code_IllegalArgument)
            }
        } catch (var11: Exception) {
            mJsBridge.responseFail(callbackFun, seqid, method, JsResult.Code_Java_Exception)
        }
    }

    fun removeNativeView(uri: Uri, seqid: Int, method: String?, callbackFun: String?) {
        try {
            var finalViewID = getViewID(uri)
            val mNativeView = mViewMap[finalViewID]
            if (null != mNativeView) {
                ThreadManager.getInstance().runOnUIThread {
                    removeView(finalViewID, mNativeView)
                    resetAll()
                    mJsBridge.response(callbackFun, seqid, method, null)
                }
            } else {
                mJsBridge.responseFail(callbackFun, seqid, method, JsResult.Code_IllegalArgument)
            }
        } catch (var11: Exception) {
            mJsBridge.responseFail(callbackFun, seqid, method, JsResult.Code_Java_Exception)
        }
    }


}