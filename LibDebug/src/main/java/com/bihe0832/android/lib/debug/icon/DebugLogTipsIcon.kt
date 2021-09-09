package com.bihe0832.android.lib.debug.icon

import android.content.Context
import android.view.MotionEvent
import com.bihe0832.android.lib.debug.R
import com.bihe0832.android.lib.floatview.BaseIconView
import com.bihe0832.android.lib.text.TextFactoryUtils
import kotlinx.android.synthetic.main.log_view.view.*

/**
 * Created by lwtorlu on 2021/9/8.
 */
class DebugLogTipsIcon(context: Context) : BaseIconView(context) {

    fun updateText(text: String) {
        log_view_text.text = TextFactoryUtils.getSpannedTextByHtml(text)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }

    override fun getLayoutId(): Int {
        return R.layout.log_view
    }

    override fun getRootId(): Int {
        return R.id.log_view_text
    }

    override fun getDefaultX(): Int {
        return 0
    }

    override fun getDefaultY(): Int {
        return 0
    }
}