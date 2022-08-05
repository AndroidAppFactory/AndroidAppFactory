package com.bihe0832.android.base.debug.immersion

import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.framework.ui.main.CommonActivity
import com.bihe0832.android.lib.immersion.enableActivityImmersive
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.ui.common.ColorTools


class DebugImmersionActivity : CommonActivity() {

    override fun getStatusBarColor(): Int {
//        return Color.parseColor("#00ffffff")
        return Color.TRANSPARENT
    }

    override fun getNavigationBarColor(): Int {
        return Color.RED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ZLog.d("DebugImmersionActivity","DebugImmersionActivity color: ${Color.WHITE}")
        ZLog.d("DebugImmersionActivity","DebugImmersionActivity color 2: ${ContextCompat.getColor(this, R.color.colorPrimary)}")
        ZLog.d("DebugImmersionActivity","DebugImmersionActivity color: ${ContextCompat.getColor(this, R.color.white)}")
        ZLog.d("DebugImmersionActivity","DebugImmersionActivity color: ${Color.parseColor("#FFFFFF")}")
        ZLog.d("DebugImmersionActivity","DebugImmersionActivity color: ${Color.parseColor("#00FFFFFF")}")
        ZLog.d("DebugImmersionActivity","DebugImmersionActivity color: ${ColorTools.getColorWithAlpha(0f,Color.WHITE)}")
        ZLog.d("DebugImmersionActivity","DebugImmersionActivity color: ${ColorTools.getColorWithAlpha(1f,Color.WHITE)}")
    }
}
