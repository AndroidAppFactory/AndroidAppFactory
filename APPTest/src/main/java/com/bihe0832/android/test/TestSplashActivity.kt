package com.bihe0832.android.test

import android.os.Bundle
import com.bihe0832.android.app.AppFactoryInit
import com.bihe0832.android.common.splash.SplashActivity
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.lib.app.icon.APPIconManager
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.router.annotation.APPMain
import com.bihe0832.android.lib.router.annotation.Module
import com.bihe0832.android.lib.utils.intent.IntentUtils

@APPMain
@Module(RouterConstants.MODULE_NAME_SPLASH)
class TestSplashActivity : SplashActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun doNext() {
        AppFactoryInit.initAll(application)
        super.doNext()
    }

    override fun getMainRouter(): String {
        return RouterConstants.MODULE_NAME_DEBUG
    }
}
