package com.bihe0832.android.common.ui.bottom.bar

import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.LinearLayout
import android.widget.TextView

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2022/9/16.
 * Description: Description
 *
 */

fun setUnreadCount(mTipsView: TextView, num: Int) {
    if (num < 1) {
        mTipsView.setText(0.toString())
        mTipsView.setVisibility(View.GONE)
    } else {
        mTipsView.setVisibility(View.VISIBLE)
        if (num > 99) {
            mTipsView.setText("99+")
        } else {
            mTipsView.setText(num.toString())
        }
    }
}

fun setUnreadDot(mTipsView: View, visible: Boolean) {
    if (visible) {
        mTipsView.setVisibility(View.VISIBLE)
    } else {
        mTipsView.setVisibility(View.GONE)
    }
}


fun resetReadDotRightMargin(mTipsView: TextView, totalWidth: Int, iconWidth: Int, textWidth: Int) {
    if (mTipsView.getVisibility() == View.VISIBLE) {
        val text = mTipsView.text
        var lp = mTipsView.getLayoutParams() as MarginLayoutParams
        if (null == lp) {
            lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        var space = (totalWidth - iconWidth) / 2 - textWidth
        if (space < 1) {
            space = 0
        }
        lp.setMargins(lp.leftMargin, lp.topMargin, space, lp.bottomMargin)
        mTipsView.setLayoutParams(lp)
    }
}