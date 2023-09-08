package com.bihe0832.android.base.debug.immersion

import android.graphics.Color
import android.os.Bundle
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.framework.ui.main.CommonActivity
import com.bihe0832.android.lib.color.utils.ColorUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import com.bihe0832.android.lib.text.TextFactoryUtils


class DebugImmersionActivity : CommonActivity() {

    override fun getStatusBarColor(): Int {
        return Color.parseColor("#00ffffff")
//        return Color.TRANSPARENT
    }

    override fun getNavigationBarColor(): Int {
        return Color.RED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initToolbar("TestMain" + TextFactoryUtils.getSpecialText("Activity", Color.GREEN), true)
//        updateIcon(false, "https://cdn.bihe0832.com/images/cv_512.jpg", -1)

        ZLog.d("DebugImmersionActivity", "DebugImmersionActivity color: ${Color.WHITE}")
        ZLog.d("DebugImmersionActivity", "DebugImmersionActivity color 2: ${ThemeResourcesManager.getColor( R.color.colorPrimary)}")
        ZLog.d("DebugImmersionActivity", "DebugImmersionActivity color: ${ThemeResourcesManager.getColor(R.color.white)}")
        ZLog.d("DebugImmersionActivity", "DebugImmersionActivity color: ${Color.parseColor("#FFFFFF")}")
        ZLog.d("DebugImmersionActivity", "DebugImmersionActivity color: ${Color.parseColor("#00FFFFFF")}")
        ZLog.d("DebugImmersionActivity", "DebugImmersionActivity color: ${ColorUtils.addAlpha(0f, Color.WHITE)}")
        ZLog.d("DebugImmersionActivity", "DebugImmersionActivity color: ${ColorUtils.addAlpha(1f, Color.WHITE)}")
    }
}
