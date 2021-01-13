package com.bihe0832.android.common.splash

import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.View
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.constant.Constants
import com.bihe0832.android.framework.router.RouterAction.openFinalURL
import com.bihe0832.android.framework.router.RouterAction.openPageByRouter
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.framework.router.RouterInterrupt.hasAgreedPrivacy
import com.bihe0832.android.framework.ui.main.CommonActivity
import com.bihe0832.android.lib.config.Config
import com.bihe0832.android.lib.immersion.hideBottomUIMenu
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.ui.dialog.CommonDialog
import com.bihe0832.android.lib.ui.dialog.OnDialogListener
import com.bihe0832.android.lib.utils.intent.IntentUtils
import java.util.*

abstract class SplashActivity : CommonActivity() {

    private var nextRouter = ""

    protected abstract fun getMainRouter(): String

    private val privacyAndAgreementMap by lazy {
        HashMap<String, View.OnClickListener>().apply {
            put(resources.getString(R.string.privacy_title), View.OnClickListener {
                IntentUtils.openWebPage(resources.getString(R.string.privacy_url), applicationContext)
            })
            put(resources.getString(R.string.agreement_title), View.OnClickListener {
                IntentUtils.openWebPage(resources.getString(R.string.agreement_url), applicationContext)
            })
        }
    }

    protected open fun getLayoutID(): Int {
        return R.layout.com_bihe0832_activity_splash
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutID())
        if (null != intent && intent.hasExtra(RouterConstants.INTENT_EXTRA_KEY_WEB_URL)) {
            nextRouter = intent.getStringExtra(RouterConstants.INTENT_EXTRA_KEY_WEB_URL)
        }
        initView()
    }

    private fun initView() {
        if (!hasAgreedPrivacy()) {
            CommonDialog(this).apply {
                title = resources.getString(R.string.dialog_title_privacy_and_agreement)
                setHtmlContent(
                        TextFactoryUtils.getCharSequenceWithClickAction(
                                resources.getString(R.string.privacy_agreement_content),
                                privacyAndAgreementMap), LinkMovementMethod.getInstance()
                )
                positive = "同意"
                negative = "暂不使用"
                setOnClickBottomListener(object : OnDialogListener {
                    override fun onPositiveClick() {
                        dismiss()
                        Config.writeConfig(Constants.CONFIG_KEY_PRIVACY_AGREEMENT_ENABLED, true)
                        doAgreement()
                    }

                    override fun onNegativeClick() {
                        dismiss()
                        ZixieContext.exitAPP()
                    }

                    override fun onCancel() {
                        onNegativeClick()
                    }
                })
            }.let {
                it.show()
            }
        } else {
            doNext()
        }
    }

    override fun onResume() {
        super.onResume()
        hideBottomUIMenu()
    }

    private fun enterMainActivity() {
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

    protected fun doNext() {
        Handler().postDelayed({ enterMainActivity() }, 1500)
    }
}