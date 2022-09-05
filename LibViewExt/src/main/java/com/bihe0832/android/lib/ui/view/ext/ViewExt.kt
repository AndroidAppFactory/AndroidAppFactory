package com.bihe0832.android.lib.ui.view.ext;

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.WindowManager

/**
 *
 * @author zixie code@bihe0832.com
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
    return context.getActivity()
}

fun Context.getActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }
        context = context.baseContext
    }
    return null
}

/**
 * 此方法用于改变背景的透明度，从而达到“变暗”的效果，数值越大透明度越高，最大为1
 */
fun Context.changeBackgroundAlpha(bgAlpha: Float) {

    getActivity()?.let {
        val lp: WindowManager.LayoutParams = it.getWindow().getAttributes()
        // 0.0-1.0
        lp.alpha = bgAlpha
        it.getWindow().setAttributes(lp)
        // everything behind this window will be dimmed.
        // 此方法用来设置浮动层，防止部分手机变暗无效
        it.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

}