package com.bihe0832.android.common.praise

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.model.res.R as ModelResR

class UserPraiseDialog(context: Context?, private val feedbackRouter: String) : CommonDialog(context) {

    private var mHeadTitleContent = ""
    override fun getLayoutID(): Int {
        return R.layout.com_bihe0832_dialog_user_praise
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShouldCanceled(true)
        setPositive(ThemeResourcesManager.getString(ModelResR.string.com_bihe0832_praise_positive))
        setNegative(ThemeResourcesManager.getString(ModelResR.string.com_bihe0832_praise_negative))
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
