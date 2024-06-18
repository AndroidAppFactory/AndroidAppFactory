package com.bihe0832.android.lib.immersion

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.bihe0832.android.lib.color.utils.ColorUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.media.image.bitmap.BitmapUtil
import com.bihe0832.android.lib.utils.os.BuildUtils

fun AppCompatActivity.getStatusBarColorBySpecialPostion(): String {
    BitmapUtil.getViewBitmapData(window.decorView)?.let { bitmap ->
        val pixel = bitmap.getPixel(100, 5)
        ZLog.d("Immersion", "x:100 y:5 【颜色值】:$pixel")
        ("#" + Integer.toHexString(pixel).toUpperCase()).let {
            ZLog.d("immersion", "【颜色值】:$it")
            ZLog.d("immersion", "【颜色值】:$pixel")
            ZLog.d("immersion", "【颜色值】:${Color.parseColor(it)}")
            return it
        }
    }

    return ""
}

fun AppCompatActivity.enableActivityImmersive(statusBarColor: Int, navigationBarColor: Int) {
    enableActivityImmersive(statusBarColor, navigationBarColor, ColorUtils.isLightColor(statusBarColor))
}

/**
 * @param colorPrimaryDark 状态栏的颜色
 * @param navigationBarColor 导航栏的颜色
 * @param isDark 文字是否深色
 */
fun AppCompatActivity.enableActivityImmersive(statusBarColor: Int, navigationBarColor: Int, isDark: Boolean) {
    ZLog.d(
        "Immersion",
        "Activity: $this statusBarColor: ${
            "#" + Integer.toHexString(statusBarColor).toUpperCase()
        }, navigationBarColor:${"#" + Integer.toHexString(navigationBarColor).toUpperCase()} isDark: $isDark",
    )
    try {
        val window = window
        if (BuildUtils.SDK_INT >= Build.VERSION_CODES.KITKAT &&
            BuildUtils.SDK_INT < Build.VERSION_CODES.LOLLIPOP
        ) {
            // 4.4版本及以上 5.0版本及以下
            window.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
            )
        } else if (BuildUtils.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
            )
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = statusBarColor
            if (Color.TRANSPARENT != navigationBarColor) {
                window.navigationBarColor = navigationBarColor
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            }

            LightStatusBarUtils.setLightStatusBar(this, ColorUtils.isLightColor(statusBarColor))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun AppCompatActivity.hideBottomUIMenu() {
    // 隐藏虚拟按键，并且全屏
    if (BuildUtils.SDK_INT > Build.VERSION_CODES.HONEYCOMB && BuildUtils.SDK_INT < Build.VERSION_CODES.KITKAT) { // lower api
        this.window.decorView?.systemUiVisibility = View.GONE
    } else if (BuildUtils.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        val decorView = window.decorView
        val uiOptions = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                or View.SYSTEM_UI_FLAG_IMMERSIVE
            )
        decorView.systemUiVisibility = uiOptions or decorView.systemUiVisibility
    }
}
