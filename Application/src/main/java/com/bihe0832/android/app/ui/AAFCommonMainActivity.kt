package com.bihe0832.android.app.ui

import android.os.Bundle
import com.bihe0832.android.app.message.checkMsgAndShowFace
import com.bihe0832.android.common.navigation.drawer.R as NavigationDrawerR
import com.bihe0832.android.app.ui.navigation.AAFNavigationDrawerFragment
import com.bihe0832.android.app.ui.navigation.addRedDotAction
import com.bihe0832.android.app.update.UpdateManager
import com.bihe0832.android.common.main.CommonActivityWithNavigationDrawer
import com.bihe0832.android.common.navigation.drawer.NavigationDrawerFragment
import com.bihe0832.android.framework.ZixieContext
import java.util.Locale


open class AAFCommonMainActivity : CommonActivityWithNavigationDrawer() {

    private var mAAFNavigationDrawerFragment = createAAFNavigationDrawerFragment()

    open fun createAAFNavigationDrawerFragment(): AAFNavigationDrawerFragment {
        return AAFNavigationDrawerFragment()
    }

    override fun getNavigationDrawerFragment(): NavigationDrawerFragment? {
        return mAAFNavigationDrawerFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addRedDotAction(findViewById(NavigationDrawerR.id.title_icon_unread))
        checkMsgAndShowFace()
        UpdateManager.checkUpdateAndShowDialog(this, false, ZixieContext.isOfficial())
        updateTitle(titleName)
        showQrcodeScan(needSound = true, needVibrate = true, onlyQRCode = false)
    }

    override fun onLocaleChanged(lastLocale: Locale, toLanguageTag: Locale) {
        mAAFNavigationDrawerFragment = createAAFNavigationDrawerFragment()
        super.onLocaleChanged(lastLocale, toLanguageTag)
    }


}
