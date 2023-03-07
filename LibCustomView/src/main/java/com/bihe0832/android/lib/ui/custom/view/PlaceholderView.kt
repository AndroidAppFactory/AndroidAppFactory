package com.bihe0832.android.lib.ui.custom.view;


import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.ui.textview.TextViewWithBackground

/**
 * @author zixie code@bihe0832.com Created on 2021/12/30.
 */
class PlaceholderView : ConstraintLayout {
    private lateinit var mHeadIcon: ImageView
    private lateinit var mTipsTextView: TextViewWithBackground
    private lateinit var mActionTextView: TextViewWithBackground

    constructor(context: Context) : super(context) {
        initView(context)
    }

    private fun initView(context: Context) {
        inflate(context, R.layout.com_bihe0832_custom_view_placeholder_view, this)
        initViewInfo()
    }

    private fun initViewInfo() {
        mHeadIcon = findViewById(R.id.list_empty_icon)
        mTipsTextView = findViewById(R.id.list_empty_tips)
        mActionTextView = findViewById(R.id.list_empty_action)
    }

    fun getHeadIcon(): ImageView {
        return mHeadIcon
    }

    fun getTipsTextView(): TextViewWithBackground {
        return mTipsTextView
    }

    fun getActionText(): TextViewWithBackground {
        return mActionTextView
    }

    fun setEmptyTips(tipsText: String?) {
        if (!TextUtils.isEmpty(tipsText)) {
            mTipsTextView.apply {
                text = TextFactoryUtils.getSpannedTextByHtml(tipsText)
                visibility = View.VISIBLE
            }
        }
    }

    fun setEmptyAction(actionText: String?, listener: OnClickListener?) {
        if (!TextUtils.isEmpty(actionText)) {
            mActionTextView.apply {
                text = TextFactoryUtils.getSpannedTextByHtml(actionText)
                setOnClickListener(listener)
                visibility = View.VISIBLE
            }
        }
    }
}