package com.bihe0832.android.lib.ui.view.ext;

import android.app.Activity
import android.content.ContextWrapper
import android.view.View

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2019-09-17.
 * Description: Description
 *
 */

/**
 * 设置view高度
 */
fun View.setViewHeight(height: Int) {
    val params = this.layoutParams
    params.height = height
    this.layoutParams = params
}

fun View.setViewWidth(width: Int) {
    val params = this.layoutParams
    params.width = width
    this.layoutParams = params
}

/**
 * 旋转view，并设置高度和宽度
 */
fun View.rotateView(degree: Float, specifiedHeight: Int, specifiedWidth: Int) {
    val h = if (specifiedHeight > 0) specifiedHeight else this.height
    val w = if (specifiedWidth > 0) specifiedWidth else this.width
    this.rotation = degree
    this.translationX = (w - h) / 2.toFloat()
    this.translationY = (h - w) / 2.toFloat()
    val lp = this.layoutParams
    lp.height = w
    lp.width = h
    this.requestLayout()
}

fun View.getActivity(): Activity? {
    var context = context
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }
        context = context.baseContext
    }
    return null
}
