package com.bihe0832.android.test

import android.os.Bundle
import android.view.Gravity
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.ui.AAFCommonMainActivity
import com.bihe0832.android.lib.debug.icon.DebugLogTips
import com.bihe0832.android.lib.lifecycle.ApplicationObserver
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.router.annotation.APPMain
import com.bihe0832.android.lib.router.annotation.Module
import com.bihe0832.android.lib.sqlite.impl.CommonDBManager
import com.bihe0832.android.test.widget.DebugWidget

@APPMain
@Module(RouterConstants.MODULE_NAME_DEBUG)
open class TestMainActivity : AAFCommonMainActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DebugLogTips.initModule(this, true, Gravity.LEFT or Gravity.TOP)
        CommonDBManager.init(this)
        ApplicationObserver.addDestoryListener(object : ApplicationObserver.APPDestroyListener {

            override fun onAllActivityDestroyed() {
                ZLog.d("onAllActivityDestroyed")
                R.mipmap.default_head_icon
            }
        })
//        ThemeManager.getThemeInfo()?.let {
//            setTheme(if (it.isDark) R.style.DarkTheme else R.style.DefaultTheme)
//        }
    }

//    override fun getLayoutID(): Int {
//        return R.layout.activity_debug_main
//    }

    override fun onResume() {
        super.onResume()
        DebugWidget.showAddDebugWidgetTips(this)
    }
    override fun onDestroy() {
        super.onDestroy()
    }

    override fun getRootFragmentClassName(): String {
        return TestMainFragment::class.java.name
    }
}
