package com.bihe0832.android.test

import android.os.Bundle
import android.view.Gravity
import com.bihe0832.android.app.leakcanary.LeakCanaryManager.addWatch
import com.bihe0832.android.common.debug.DebugMainActivity
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.lib.debug.icon.DebugLogTips
import com.bihe0832.android.lib.lifecycle.ApplicationObserver
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.router.annotation.APPMain
import com.bihe0832.android.lib.router.annotation.Module
import com.bihe0832.android.lib.sqlite.impl.CommonDBManager

@APPMain
@Module(RouterConstants.MODULE_NAME_DEBUG)
open class TestMainActivity : DebugMainActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initToolbar("TestMainActivity", false)

//        UpdateManager.checkUpdateAndShowDialog(this, fa¬lse)
        DebugLogTips.initModule(this, true, Gravity.RIGHT or Gravity.BOTTOM)

        CommonDBManager.init(this)

        ApplicationObserver.addDestoryListener(object : ApplicationObserver.APPDestroyListener {

            override fun onAllActivityDestroyed() {
                ZLog.d("onAllActivityDestroyed")
            }
        })
//        ThemeManager.getThemeInfo()?.let {
//            setTheme(if (it.isDark) R.style.DarkTheme else R.style.DefaultTheme)
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        addWatch(this)
    }

    override fun onResume() {
        super.onResume()
//        mIconManager.showIcon()
//        hideBottomUIMenu()
    }

    override fun loadFragment() {
        if (findFragment(TestMainFragment::class.java) == null) {
            loadRootFragment(TestMainFragment())
        }
    }
}
