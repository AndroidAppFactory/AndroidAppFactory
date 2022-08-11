package com.bihe0832.android.test

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.view.Gravity
import com.bihe0832.android.app.leakcanary.LeakCanaryManager.addWatch
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.framework.ui.main.CommonActivity
import com.bihe0832.android.lib.adapter.CardInfoHelper
import com.bihe0832.android.lib.debug.icon.DebugLogTips
import com.bihe0832.android.lib.immersion.hideBottomUIMenu
import com.bihe0832.android.lib.lifecycle.ApplicationObserver
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.router.annotation.APPMain
import com.bihe0832.android.lib.router.annotation.Module
import com.bihe0832.android.lib.sqlite.impl.CommonDBManager
import com.bihe0832.android.lib.utils.os.BuildUtils

@APPMain
@Module(RouterConstants.MODULE_NAME_DEBUG)
open class TestMainActivity : CommonActivity() {
    val LOG_TAG = "DebugHttpActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initToolbar("TestMainActivity", false)
        if (BuildUtils.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        }
        CardInfoHelper.getInstance().setAutoAddItem(true)
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

//    override fun getStatusBarColor(): Int {
//        return Color.WHITE
//    }
//
//    override fun getNavigationBarColor(): Int {
//        return ContextCompat.getColor(this, R.color.result_point_color)
//    }

    override fun onResume() {
        super.onResume()
        if (findFragment(TestMainFragment::class.java) == null) {
            loadRootFragment(R.id.common_fragment_content, TestMainFragment())
        }
        hideBottomUIMenu()
//        mIconManager.showIcon()
//        hideBottomUIMenu()
    }


    override fun onBackPressedSupport() {
        super.onBackPressedSupport()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }
}
