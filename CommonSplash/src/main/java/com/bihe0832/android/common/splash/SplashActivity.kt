package com.bihe0832.android.common.splash

import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import com.bihe0832.android.framework.router.RouterAction.openFinalURL
import com.bihe0832.android.framework.router.RouterAction.openPageByRouter
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.framework.router.RouterInterrupt.hasAgreedPrivacy
import com.bihe0832.android.framework.ui.main.CommonActivity
import com.bihe0832.android.lib.immersion.hideBottomUIMenu
import com.bihe0832.android.lib.log.ZLog

abstract class SplashActivity : CommonActivity() {

    private var nextRouter = ""

    protected abstract fun getMainRouter(): String

    protected open fun getLayoutID(): Int {
        return R.layout.com_bihe0832_activity_splash
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutID())
        if (null != intent && intent.hasExtra(RouterConstants.INTENT_EXTRA_KEY_WEB_URL)) {
            nextRouter = intent.getStringExtra(RouterConstants.INTENT_EXTRA_KEY_WEB_URL) ?: ""
        }
        initView()
    }

    private fun initView() {
        if (!hasAgreedPrivacy()) {
            AgreementPrivacy.showPrivacy(this) {
                doAgreement()
            }
        } else {
            doNext()
        }
    }

    override fun onResume() {
        super.onResume()
        hideBottomUIMenu()
    }

    protected fun enterMainActivity() {
        ZLog.d("enterMainActivity")
        if (TextUtils.isEmpty(nextRouter)) {
            openPageByRouter(getMainRouter())
        } else {
            openFinalURL(nextRouter)
        }
        finish()
    }

    open protected fun doAgreement() {
        doNext()
    }

    open protected fun doNext() {
        Handler().postDelayed({ enterMainActivity() }, 1500)
    }
}