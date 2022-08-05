package com.bihe0832.android.base.debug.immersion

import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.framework.ui.main.CommonActivity
import com.bihe0832.android.lib.immersion.enableActivityImmersive
import com.bihe0832.android.lib.thread.ThreadManager


class DebugImmersionActivity : CommonActivity() {

    override fun getStatusBarColor(): Int {
        return Color.parseColor("#00ffffff")
    }

    override fun getNavigationBarColor(): Int {
        return Color.RED
    }
}
