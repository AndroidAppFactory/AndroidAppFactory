package com.bihe0832.android.test

import android.os.Bundle
import android.view.Gravity
import androidx.lifecycle.Observer
import com.bihe0832.android.app.leakcanary.LeakCanaryManager.addWatch
import com.bihe0832.android.app.message.AAFMessageManager
import com.bihe0832.android.base.debug.navigation.DebugNavigationDrawerFragment
import com.bihe0832.android.common.debug.DebugMainActivity
import com.bihe0832.android.common.message.data.MessageInfoItem
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

    private val mNavigationDrawerFragment by lazy {
        DebugNavigationDrawerFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateIcon("", R.mipmap.ic_menu_white) {
            mNavigationDrawerFragment.openDrawer()
        }

        AAFMessageManager.getMessageLiveData().observe(this, object : Observer<List<MessageInfoItem>> {
            override fun onChanged(t: List<MessageInfoItem>?) {
                t?.filter { it.canShow(true) }?.forEach {
                    AAFMessageManager.showNotice(this@TestMainActivity, it, true)
                }
            }
        })
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

        //菜单
        mNavigationDrawerFragment.setUp(findViewById(R.id.navigation_drawer_fl), findViewById(R.id.drawer_layout))

    }

    override fun loadFragment() {
        super.loadFragment()
        loadRootFragment(R.id.navigation_drawer_fl, mNavigationDrawerFragment)
    }

    override fun getLayoutID(): Int {
        return R.layout.debug_main_activity_framelayout
    }

    override fun onDestroy() {
        super.onDestroy()
        addWatch(this)
    }

    override fun getRootFragmentClassName(): String {
        return TestMainFragment::class.java.name
    }
}
