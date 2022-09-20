package com.bihe0832.android.lib.ui.textview.ext;

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.bihe0832.android.lib.ui.textview.TextViewWithBackground

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2019-09-17.
 * Description: Description
 *
 */

fun TextViewWithBackground.changeStatusWithUnreadMsg(num: Int, circleWidth: Int) {
    var lp = layoutParams
    if (null == lp) {
        lp = ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    if (num < 0) {
        visibility = View.GONE
    } else {
        visibility = View.VISIBLE
        if (num == 0) { //圆点,设置默认宽高
            text = ""
            lp.width = circleWidth
            lp.height = circleWidth
            layoutParams = lp
        } else {
            text = if (num < 100) {
                num.toString()
            } else {
                //数字超过两位,显示99+
                "99+"
            }
        }
    }

}