package com.bihe0832.android.lib.debug.icon

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.bihe0832.android.lib.debug.R
import com.bihe0832.android.lib.floatview.BaseIconView
import com.bihe0832.android.lib.text.TextFactoryUtils

open class DebugLogTipsIcon(context: Context) : BaseIconView(context) {

    private var currentText = ""

    private fun updateText(text: String) {
        getTextView()?.text = TextFactoryUtils.getSpannedTextByHtml(text)
    }


    fun getTextView(): TextView? {
        return findViewById(R.id.log_view_text) as? TextView
    }

    override fun ignoreStatusBar(): Boolean {
        return true
    }

    override fun getLayoutId(): Int {
        return R.layout.com_bihe0832_lib_log_view
    }

    override fun getRootId(): Int {
        return R.id.log_root
    }

    override fun getDefaultX(): Int {
        return 0
    }

    override fun getDefaultY(): Int {
        return 0
    }

    fun show(text: String) {
        currentText = text
        showResult()
    }

    fun append(text: String) {
        currentText += text
        showResult()
    }

    private fun showResult() {
        if (TextUtils.isEmpty(currentText)) {
            hide()
        } else {

            updateText(currentText)
            visibility = View.VISIBLE
        }
    }

    private fun hide() {
        this.visibility = View.GONE
    }
}