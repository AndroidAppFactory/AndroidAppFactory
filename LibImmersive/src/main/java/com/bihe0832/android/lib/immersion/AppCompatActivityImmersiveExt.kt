package com.bihe0832.android.lib.immersion

import android.graphics.Color
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager

/**
 * @param colorPrimaryDark 状态栏的颜色
 * @param navigationBarColor 导航栏的颜色
 */
fun AppCompatActivity.enableActivityImmersive(colorPrimaryDark: Int, navigationBarColor: Int) {
    try {
        val window = window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //4.4版本及以上 5.0版本及以下
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            LightStatusBarUtils.setLightStatusBar(this, navigationBarColor == Color.TRANSPARENT
                    , colorPrimaryDark == Color.TRANSPARENT)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = colorPrimaryDark
            window.navigationBarColor = navigationBarColor
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}