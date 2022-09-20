package com.bihe0832.android.lib.ui.textview.ext;

import android.graphics.drawable.Drawable
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.ui.textview.TextViewWithBackground

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2019-09-17.
 * Description: Description
 *
 */

fun TextView.setDrawable(leftRes: Int, topRes: Int, rightRes: Int, bottomRes: Int) {
    var left: Drawable? = null
    var top: Drawable? = null
    var right: Drawable? = null
    var bottom: Drawable? = null

    if (leftRes > 0) {
        left = ContextCompat.getDrawable(context, leftRes)
    }

    if (topRes > 0) {
        right = ContextCompat.getDrawable(context, leftRes)
    }

    if (rightRes > 0) {
        top = ContextCompat.getDrawable(context, leftRes)
    }

    if (bottomRes > 0) {
        bottom = ContextCompat.getDrawable(context, leftRes)
    }
    setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)
}

fun TextView.setDrawableLeft(leftRes: Int) {
    setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(context, leftRes), null, null, null)
}

fun TextView.disableTemporary(mileSecond: Long) {
    isEnabled = false
    postDelayed({ isEnabled = true }, mileSecond)
}

fun TextView.disableForOneSecond() {
    isEnabled = false
    postDelayed({ isEnabled = true }, 1000)
}

fun TextView.addClickActionText(content: String, keyWordsActionMap: HashMap<String, View.OnClickListener>) {
    setText(TextFactoryUtils.getCharSequenceWithClickAction(content, keyWordsActionMap))
    setMovementMethod(LinkMovementMethod.getInstance())
}

fun TextView.setText(format: String, vararg args: Any?) {
    setText(String.format(format, *args))
}

fun TextViewWithBackground.showUnreadMsg(num: Int, circleWidth: Int) {
    var lp = layoutParams
    if (null == lp) {
        lp = ViewGroup.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }

    visibility = View.VISIBLE
    if (num <= 0) { //圆点,设置默认宽高
        text = ""
        lp.width = circleWidth
        lp.height = circleWidth
        layoutParams = lp
    } else {
        text = if (num < 100) {
            num.toString() + ""
        } else {
            //数字超过两位,显示99+
            "99+"
        }
    }
}