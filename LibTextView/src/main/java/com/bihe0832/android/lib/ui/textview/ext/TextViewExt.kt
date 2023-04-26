package com.bihe0832.android.lib.ui.textview.ext;

import android.graphics.drawable.Drawable
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bihe0832.android.lib.text.TextFactoryUtils

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
        top = ContextCompat.getDrawable(context, topRes)
    }

    if (rightRes > 0) {
        right = ContextCompat.getDrawable(context, rightRes)
    }

    if (bottomRes > 0) {
        bottom = ContextCompat.getDrawable(context, bottomRes)
    }
    setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom)
}

fun TextView.setDrawable(
    leftRes: Int,
    topRes: Int,
    rightRes: Int,
    bottomRes: Int,
    width: Int,
    height: Int
) {
    var left: Drawable? = null
    var top: Drawable? = null
    var right: Drawable? = null
    var bottom: Drawable? = null

    if (leftRes > 0) {
        left = ContextCompat.getDrawable(context, leftRes)?.apply {
            setBounds(0, 0, width, height)
        }
    }

    if (topRes > 0) {
        top = ContextCompat.getDrawable(context, topRes)?.apply {
            setBounds(0, 0, width, height)
        }
    }

    if (rightRes > 0) {
        right = ContextCompat.getDrawable(context, rightRes)?.apply {
            setBounds(0, 0, width, height)
        }
    }

    if (bottomRes > 0) {
        bottom = ContextCompat.getDrawable(context, bottomRes)?.apply {
            setBounds(0, 0, width, height)
        }
    }
    setCompoundDrawables(left, top, right, bottom)
}

fun TextView.setDrawableLeft(leftRes: Int) {
    setCompoundDrawablesWithIntrinsicBounds(
        ContextCompat.getDrawable(context, leftRes),
        null,
        null,
        null
    )
}

fun TextView.setDrawableLeft(leftRes: Int, width: Int, height: Int) {
    ContextCompat.getDrawable(context, leftRes)?.apply {
        setBounds(0, 0, width, height)
    }.let {
        setCompoundDrawables(it, null, null, null)
    }
}

fun TextView.disableTemporary(mileSecond: Long) {
    isEnabled = false
    postDelayed({ isEnabled = true }, mileSecond)
}

fun TextView.disableForOneSecond() {
    disableTemporary(1000)
}

fun TextView.addClickActionText(
    content: String,
    keyWordsActionMap: HashMap<String, View.OnClickListener>
) {
    setText(TextFactoryUtils.getCharSequenceWithClickAction(content, keyWordsActionMap))
    setMovementMethod(LinkMovementMethod.getInstance())
}

fun TextView.setText(format: String, vararg args: Any?) {
    setText(String.format(format, *args))
}