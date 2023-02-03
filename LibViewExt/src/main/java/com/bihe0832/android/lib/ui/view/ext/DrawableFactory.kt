package com.bihe0832.android.lib.ui.view.ext

import android.R
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.view.View
import com.bihe0832.android.lib.utils.os.DisplayUtil

/**
 *
 * @author hardyshi code@bihe0832.com
 * Created on 2023/2/3.
 * Description: 一些常见的背景颜色等的代码实现，核心就是 GradientDrawable
 *
 */


/**
 * 所有长度的单位都是px
 */
fun getDrawable(color: Int, pxOfCornerRadius: Float, pxOfStrokeWidth: Int, strokeColor: Int): Drawable {
    GradientDrawable().apply {
        if (color != -1) {
            setColor(color)
        }
        setCornerRadius(pxOfCornerRadius)
        setStroke(pxOfStrokeWidth, strokeColor)
    }.let {
        return StateListDrawable().apply {
            addState(intArrayOf(-R.attr.state_pressed), it)
        }
    }
}

/**
 * 所有长度的单位都是px
 */
fun View.generateDrawable(color: Int, pxOfCornerRadius: Float, pxOfStrokeWidth: Int, strokeColor: Int): Drawable {
    if (color == -1) {
        if (background is ColorDrawable) {
            (background as ColorDrawable).color
        } else {
            -1
        }
    } else {
        color
    }.let {
        return getDrawable(it, pxOfCornerRadius, pxOfStrokeWidth, strokeColor)
    }
}


fun View.setDrawableBackground(color: Int, pxOfCornerRadius: Float, pxOfStrokeWidth: Int, strokeColor: Int) {
    background = generateDrawable(color, pxOfCornerRadius, pxOfStrokeWidth, strokeColor)
}

/**
 * 所有长度的单位都是dp
 */
fun getDrawable(context: Context, color: Int, dpOfCornerRadius: Int, dpOfStrokeWidth: Int, strokeColor: Int): Drawable {
    return getDrawable(color, DisplayUtil.dip2px(context, dpOfCornerRadius.toFloat()).toFloat(), DisplayUtil.dip2px(context, dpOfStrokeWidth.toFloat()), strokeColor)
}


fun View.generateDrawable(color: Int, dpOfCornerRadius: Int, dpOfStrokeWidth: Int, strokeColor: Int): Drawable {
    return generateDrawable(color, DisplayUtil.dip2px(context, dpOfCornerRadius.toFloat()).toFloat(), DisplayUtil.dip2px(context, dpOfStrokeWidth.toFloat()), strokeColor)
}


fun View.setDrawableBackground(color: Int, cornerRadius: Int, strokeWidth: Int, strokeColor: Int) {
    background = generateDrawable(color, cornerRadius, strokeWidth, strokeColor)
}

fun View.generateCornerRadiusDrawable(color: Int, cornerRadius: Int): Drawable {
    return generateDrawable(color, cornerRadius, 0, Color.TRANSPARENT)
}

fun View.setCornerRadiusBackground(color: Int, cornerRadius: Int) {
    background = generateCornerRadiusDrawable(color, cornerRadius)
}

fun View.generateCornerRadiusDrawable(cornerRadius: Int): Drawable {
    return generateCornerRadiusDrawable(-1, cornerRadius)
}

fun View.setCornerRadiusBackground(cornerRadius: Int) {
    background = generateCornerRadiusDrawable(cornerRadius)
}

fun View.generateStrokeDrawable(cornerRadius: Int, strokeWidth: Int, strokeColor: Int): Drawable {
    return generateDrawable(-1, cornerRadius, strokeWidth, strokeColor)
}

fun View.setStrokeBackground(cornerRadius: Int, strokeWidth: Int, strokeColor: Int) {
    background = generateStrokeDrawable(cornerRadius, strokeWidth, strokeColor)
}

fun View.generateStrokeDrawable(strokeWidth: Int, strokeColor: Int): Drawable {
    return generateStrokeDrawable(0, strokeWidth, strokeColor)
}

fun View.setStrokeBackground(strokeWidth: Int, strokeColor: Int) {
    background = generateStrokeDrawable(strokeWidth, strokeColor)
}
