package com.bihe0832.android.base.debug.webview

import android.content.Context
import android.graphics.Rect
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.item.getTipsItem
import com.bihe0832.android.common.webview.base.BaseWebViewFragment
import com.bihe0832.android.common.webview.nativeimpl.NativeWebView
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.router.openZixieWeb
import com.bihe0832.android.framework.ui.BaseFragment
import com.bihe0832.android.lib.adapter.CardBaseAdapter
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.recycleview.ext.SafeLinearLayoutManager
import com.bihe0832.android.lib.utils.ConvertUtils
import com.bihe0832.android.lib.utils.os.DisplayUtil


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
                        textView?.setTranslationY(
                            DisplayUtil.dip2px(
                                context,
                                ConvertUtils.parseFloat(value, 0f)
                            ).toFloat()
                        );
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

        fun getRecycleView(context: Context): View {
            val contentView = View.inflate(context, R.layout.web_native_test_layout, null)
            val adList = contentView.findViewById<CuntomRecycleView>(R.id.test_list)
            val adapter: CardBaseAdapter by lazy {
                object : CardBaseAdapter(context, mutableListOf<CardBaseModule>()) {
                }.apply {
                    setHeaderFooterEmpty(true, false)
                    bindToRecyclerView(adList)
                }
            }
            adList.apply {
                layoutManager = object : SafeLinearLayoutManager(context) {
                    override fun getOrientation(): Int {
                        return LinearLayoutManager.VERTICAL
                    }

                    override fun canScrollVertically(): Boolean {
                        return false
                    }
                }
                this.adapter = adapter
                isFocusableInTouchMode = false
                (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
                addItemDecoration(object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(
                        outRect: Rect,
                        view: View,
                        parent: RecyclerView,
                        state: RecyclerView.State,
                    ) {
                        super.getItemOffsets(outRect, view, parent, state)
                        outRect.set(
                            DisplayUtil.dip2px(context, 12f),
                            DisplayUtil.dip2px(context, 12f),
                            DisplayUtil.dip2px(context, 12f),
                            0
                        )
                    }
                })

            }
            var contentViewHeight = 0
            val data = ArrayList<CardBaseModule>().apply {
                add(getTipsItem("原生内核打开JSbridge调试页面"))
                add(getTipsItem("原生内核打开JSbridge调试页面"))
                add(getTipsItem("原生内核打开JSbridge调试页面"))
            }
            adapter.data.addAll(data)
            adapter.notifyDataSetChanged()
            data.forEach {
                contentViewHeight += DisplayUtil.dip2px(
                    context, 50f
                )
                contentViewHeight += DisplayUtil.dip2px(context, 100f)
            }
            val newAdListLayoutParams = adList.layoutParams as? ConstraintLayout.LayoutParams
            newAdListLayoutParams?.height = contentViewHeight
            adList.layoutParams = newAdListLayoutParams
            contentView.setOnTouchListener(object:OnTouchListener{
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    ZLog.d(BaseWebViewFragment.TAG, "contentView onTouch:$event")
                    return false
                }

            })

            return contentView
        }

        fun getHeight(): Int {
            return ZixieContext.screenWidth * 9 / 16
        }
    }

}