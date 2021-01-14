package com.bihe0832.android.lib.ui.view.ext;

import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import com.bihe0832.android.lib.text.TextFactoryUtils

/**
 *
 * @author hardyshi code@bihe0832.com
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