package com.bihe0832.android.base.debug.webview

import android.app.Activity
import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.AbsoluteLayout
import com.bihe0832.android.common.webview.base.BaseWebViewFragment.TAG
import com.bihe0832.android.common.webview.nativeimpl.NativeJsBridgeProxy
import com.bihe0832.android.common.webview.nativeimpl.NativeWebView
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback
import com.bihe0832.android.lib.jsbridge.JsResult
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.ConvertUtils
import com.bihe0832.android.lib.utils.os.DisplayUtil
import com.tencent.smtt.sdk.ValueCallback

/**
 * Created by zixie on 2016/10/20.
 */
class DebugJsBridgeProxy(activity: Activity, webView: NativeWebView) :
    NativeJsBridgeProxy(activity, webView) {

    private val nativeViewMap = HashMap<String?, View?>()

    override fun onRefresh() {
        super.onRefresh()
        removeAllNativeViewItem()
    }

    fun addToNativeViewRecord(key: String?, mNativeView: View?) {
        nativeViewMap[key] = mNativeView
    }

    private fun resetADHeight(
        key: String?,
        showView: Boolean,
        callback: AAFDataCallback<Boolean>?
    ) {
        ZLog.d(TAG, "resetADHeight  key=$key; showView=$showView")
        val mNativeView = nativeViewMap[key]
        if (mNativeView != null) {
            if (showView) {
                ThreadManager.getInstance().runOnUIThread {
                    webView.evaluateJavascript(
                        "javaScript:getNativeViewPosition('$key')",
                        ValueCallback<String> { h5Position ->
                            ZLog.d(TAG, "resetADHeight key=$key; h5Position=$h5Position")
                            val topPosition = ConvertUtils.parseFloat(h5Position, -1f)
                            ZLog.d(TAG, "resetADHeight  key=$key; top=$topPosition")
                            if (topPosition > 0) {
                                val params = mNativeView.layoutParams as AbsoluteLayout.LayoutParams
                                params.y =
                                    DisplayUtil.dip2px(ZixieContext.applicationContext, topPosition)
                                mNativeView.layoutParams = params
                                webView.loadUrl(
                                    "javaScript:setNativeViewHeight('$key'," + DisplayUtil.px2dip(
                                        ZixieContext.applicationContext,
                                        mNativeView.height.toFloat()
                                    ) + ")"
                                )
                                mNativeView.visibility = View.VISIBLE
                                callback?.onSuccess(true)
                            } else {
                                webView.loadUrl("javaScript:setNativeViewHeight('$key',0)")
                                callback?.onError(
                                    -2, "resetADHeight : key=" + key
                                            + " getNativeViewPosition should more than 0,current  is "
                                            + topPosition
                                )
                            }
                        })
                }
            } else {
                ThreadManager.getInstance().runOnUIThread {
                    webView.loadUrl("javaScript:setNativeViewHeight('$key',0)")
                    mNativeView.visibility = View.GONE
                    callback?.onSuccess(true)
                }
            }
        } else {
            ZLog.d(TAG, "resetADHeight : key=" + key + "mNativeView is bad")
            ThreadManager.getInstance().runOnUIThread {
                webView.loadUrl("javaScript:setNativeViewHeight('$key',0)")
                callback?.onError(-1, "resetADHeight : key=$key mNativeView is bad")
            }
        }
    }

    fun addNativeView(uri: Uri, seqid: Int, method: String?, callbackFun: String?) {
        var viewID1: String? = ""
        try {
            viewID1 = uri.getQueryParameter("viewKey")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val finalViewID = viewID1
        if (TextUtils.isEmpty(finalViewID)) {
            mJsBridge.responseFail(
                callbackFun,
                seqid,
                method,
                JsResult.Code_IllegalArgument,
                "viewKey is bad"
            )
            return
        } else {
            ThreadManager.getInstance().runOnUIThread {
                webView.loadUrl("javaScript:document.getElementById('$finalViewID').height")
                val jsCode =
                    ("(function() {" + "var div = document.getElementById('" + finalViewID + "');"
                            + "return div ? 'Div IS OK' : 'Div not found';" + "})()")
                webView.evaluateJavascript(jsCode,
                    ValueCallback<String> { result ->
                        ZLog.d(TAG, "addNativeView Div $finalViewID 检查结果: $result")
                    })
            }
        }


        try {
            if (nativeViewMap[finalViewID] == null) {
                ThreadManager.getInstance().runOnUIThread {
                    val view = DebugH5NativeWebFragment.getTextView(mActivity)
                    webView.addView(view)
                    addToNativeViewRecord(finalViewID, view)
                    val widthMeasureSpec =
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    val heightMeasureSpec =
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    view.measure(widthMeasureSpec, heightMeasureSpec)
                    view.layout(
                        0,
                        0,
                        view.measuredWidth,
                        view.measuredHeight
                    )
                    view.visibility = View.GONE
                    resetADHeight(finalViewID, true, object : AAFDataCallback<Boolean>() {
                        override fun onSuccess(aBoolean: Boolean?) {
                            mJsBridge.response(callbackFun, seqid, method, "")
                        }

                        override fun onError(errorCode: Int, msg: String) {
                            mJsBridge.responseFail(
                                callbackFun,
                                seqid,
                                method,
                                JsResult.Code_Busy,
                                msg
                            )
                        }
                    })
                }
            } else {
                mJsBridge.responseFail(callbackFun, seqid, method, JsResult.Code_IllegalArgument)
            }
        } catch (e: Exception) {
            mJsBridge.responseFail(
                callbackFun, seqid, method, JsResult.Code_Java_Exception,
                e.stackTrace.toString()
            )
            e.printStackTrace()
        }
    }

    private fun changeNativeViewVisibility(
        uri: Uri,
        seqid: Int,
        method: String,
        callbackFun: String,
        isVisiable: Boolean
    ) {
        try {
            var viewID: String? = ""
            try {
                viewID = uri.getQueryParameter("viewKey")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            resetADHeight(viewID, isVisiable, object : AAFDataCallback<Boolean>() {
                override fun onSuccess(aBoolean: Boolean?) {
                    mJsBridge.response(callbackFun, seqid, method, "showNativeView success")
                    resetAll()
                }

                override fun onError(errorCode: Int, msg: String) {
                    mJsBridge.responseFail(callbackFun, seqid, method, JsResult.Code_Busy, msg)
                }
            })
        } catch (var11: Exception) {
            mJsBridge.responseFail(callbackFun, seqid, method, JsResult.Code_Java_Exception)
        }
    }

    fun showNativeView(uri: Uri, seqid: Int, method: String, callbackFun: String) {
        changeNativeViewVisibility(uri, seqid, method, callbackFun, true)
    }

    fun hideNativeView(uri: Uri, seqid: Int, method: String, callbackFun: String) {
        changeNativeViewVisibility(uri, seqid, method, callbackFun, false)
    }

    private fun removeView(
        finalViewID: String?,
        mNativeView: View?,
        callback: AAFDataCallback<Boolean>?
    ) {
        try {
            if (mNativeView != null) {
                resetADHeight(finalViewID, false, callback)
                if (null != mNativeView.parent) {
                    (mNativeView.parent as ViewGroup).removeView(mNativeView)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removeNativeView(uri: Uri, seqid: Int, method: String?, callbackFun: String?) {
        try {
            var viewID: String? = ""
            try {
                viewID = uri.getQueryParameter("viewKey")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val mNativeView = nativeViewMap[viewID]
            if (null != mNativeView) {
                val finalViewID = viewID
                ThreadManager.getInstance().runOnUIThread {
                    removeView(finalViewID, mNativeView, null)
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

    private fun removeAllNativeViewItem() {
        ThreadManager.getInstance().runOnUIThread {
            val it = nativeViewMap.entries.iterator()
            while (it.hasNext()) {
                val entry = it.next()
                try {
                    removeView(entry.key, entry.value, null)
                    it.remove()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun removeAllNativeView(uri: Uri?, seqid: Int, method: String?, callbackFun: String?) {
        try {
            removeAllNativeViewItem()
            resetAll()
            mJsBridge.response(callbackFun, seqid, method, null)
        } catch (var11: Exception) {
            mJsBridge.responseFail(callbackFun, seqid, method, JsResult.Code_Java_Exception)
        }
    }


    fun resetAll() {
        ThreadManager.getInstance().start({
            ThreadManager.getInstance().runOnUIThread {
                val it = nativeViewMap.entries.iterator()
                while (it.hasNext()) {
                    val entry = it.next()
                    try {
                        entry.value?.let { view ->
                            if (view.visibility == View.VISIBLE) {
                                resetADHeight(entry.key, true, null)
                            } else if (view.parent == null) {
                                resetADHeight(entry.key, false, null)
                                it.remove()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }, 500L)
    }
}