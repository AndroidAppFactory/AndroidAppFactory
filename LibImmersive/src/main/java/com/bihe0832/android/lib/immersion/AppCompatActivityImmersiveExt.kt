package com.bihe0832.android.lib.immersion

import android.graphics.Color
import android.os.Build
import android.support.annotation.ColorInt
import android.support.v4.graphics.ColorUtils
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import com.bihe0832.android.lib.utils.os.BuildUtils


/**
 * @param colorPrimaryDark 状态栏的颜色
 * @param navigationBarColor 导航栏的颜色
 * @param isDark 文字是否深色
 */
fun AppCompatActivity.enableActivityImmersive(colorPrimaryDark: Int, navigationBarColor: Int) {
    try {
        val window = window
        if (BuildUtils.SDK_INT >= Build.VERSION_CODES.KITKAT
                && BuildUtils.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //4.4版本及以上 5.0版本及以下
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        } else if (BuildUtils.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = colorPrimaryDark
            if (Color.TRANSPARENT != navigationBarColor) {
                window.navigationBarColor = navigationBarColor
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            }

            LightStatusBarUtils.setLightStatusBar(this, isLightColor(colorPrimaryDark))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun isLightColor(@ColorInt color: Int): Boolean {
    return ColorUtils.calculateLuminance(color) >= 0.5
}

fun AppCompatActivity.hideBottomUIMenu() {
    //隐藏虚拟按键，并且全屏
    if (BuildUtils.SDK_INT > Build.VERSION_CODES.HONEYCOMB && BuildUtils.SDK_INT < Build.VERSION_CODES.KITKAT) { // lower api
        this.window.decorView?.systemUiVisibility = View.GONE
    } else if (BuildUtils.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        val decorView = window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                or View.SYSTEM_UI_FLAG_IMMERSIVE)
        decorView.systemUiVisibility = uiOptions

    }
}
