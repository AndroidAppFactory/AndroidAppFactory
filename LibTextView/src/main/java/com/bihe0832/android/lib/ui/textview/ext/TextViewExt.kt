package com.bihe0832.android.lib.ui.textview.ext;

import android.graphics.drawable.Drawable
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.theme.ThemeResourcesManager

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
        left = ThemeResourcesManager.getDrawable(leftRes)
    }

    if (topRes > 0) {
        top = ThemeResourcesManager.getDrawable(topRes)
    }

    if (rightRes > 0) {
        right = ThemeResourcesManager.getDrawable(rightRes)
    }

    if (bottomRes > 0) {
        bottom = ThemeResourcesManager.getDrawable(bottomRes)
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
        left = ThemeResourcesManager.getDrawable(leftRes)?.apply {
            setBounds(0, 0, width, height)
        }
    }

    if (topRes > 0) {
        top = ThemeResourcesManager.getDrawable(topRes)?.apply {
            setBounds(0, 0, width, height)
        }
    }

    if (rightRes > 0) {
        right = ThemeResourcesManager.getDrawable(rightRes)?.apply {
            setBounds(0, 0, width, height)
        }
    }

    if (bottomRes > 0) {
        bottom = ThemeResourcesManager.getDrawable(bottomRes)?.apply {
            setBounds(0, 0, width, height)
        }
    }
    setCompoundDrawables(left, top, right, bottom)
}

fun TextView.setDrawableLeft(leftRes: Int) {
    setCompoundDrawablesWithIntrinsicBounds(ThemeResourcesManager.getDrawable(leftRes), null, null, null)
}

fun TextView.setDrawableLeft(leftRes: Int, width: Int, height: Int) {
    ThemeResourcesManager.getDrawable(leftRes)?.apply {
        setBounds(0, 0, width, height)
    }.let {
        setCompoundDrawables(it, null, null, null)
    }
}

fun TextView.setDrawableTop(topRes: Int, width: Int, height: Int) {
    ThemeResourcesManager.getDrawable(topRes)?.apply {
        setBounds(0, 0, width, height)
    }.let {
        setCompoundDrawables(null, it, null, null)
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