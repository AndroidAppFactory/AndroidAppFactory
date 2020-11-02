package com.bihe0832.android.common.praise

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.bihe0832.android.common.praise.UserPraiseManager.KEY_PRAISE_DONE
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.lib.config.Config

class UserPraiseDialog(private val activity: Activity, private val feedbackRouter: String) : Dialog(activity, R.style.userPraiseDialogTheme) {

    private var mContentView: TextView? = null

    private var mContent = ""

    fun setContent(content: String) {
        this.mContent = content
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.dialog_user_praise)
        initView()
        initEvent()
        initData()
    }

    private fun initView() {
        mContentView = findViewById(R.id.tv_text)
    }

    private fun initEvent() {
        findViewById<View>(R.id.btn_goMarket)?.setOnClickListener {
            praiseDone()
            UserPraiseManager.launchAppStore(activity)
            dismiss()
        }
        findViewById<View>(R.id.btn_goFeedback)?.setOnClickListener {
            praiseDone()
            RouterAction.openFinalURL(feedbackRouter)
            dismiss()
        }
        findViewById<View>(R.id.btn_close)?.setOnClickListener {
            this@UserPraiseDialog.dismiss()
        }
    }

    private fun initData() {
        if (mContent.isNotEmpty()) {
            this.mContentView?.text = mContent
        }
    }

    private fun praiseDone() {
        Config.readConfig(KEY_PRAISE_DONE, 1)
    }


}