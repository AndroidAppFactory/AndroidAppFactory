package com.bihe0832.android.common.praise

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.bihe0832.android.lib.router.Routers
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.lib.ui.dialog.OnDialogListener

class UserPraiseDialog(context: Context?, private val feedbackRouter: String) : CommonDialog(context) {

    private var mHeadTitleContent = ""
    override fun getLayoutID(): Int {
        return R.layout.com_bihe0832_dialog_user_praise
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShouldCanceled(true)
        setOnClickBottomListener(object : OnDialogListener {
            override fun onPositiveClick() {
                UserPraiseManager.doPraiseAction()
                UserPraiseManager.launchAppStore(context)
                dismiss()
            }

            override fun onNegativeClick() {
                UserPraiseManager.doPraiseAction()
                Routers.open(context, feedbackRouter, Intent.FLAG_ACTIVITY_SINGLE_TOP)
                dismiss()
            }

            override fun onCancel() {
                dismiss()
            }

        })
        findViewById<View>(R.id.close).setOnClickListener {
            dismiss()
        }
    }


    fun setHeadTitleContent(content: String) {
        mHeadTitleContent = content
    }

    override fun refreshView() {
        super.refreshView()
        if (mHeadTitleContent.isNotBlank()) {
            findViewById<TextView>(R.id.head).text = mHeadTitleContent
        }

    }

}