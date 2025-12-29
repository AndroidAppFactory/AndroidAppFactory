package com.bihe0832.android.common.about

import android.os.Bundle
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.debug.ShowDebugClick
import com.bihe0832.android.framework.privacy.AgreementPrivacy
import com.bihe0832.android.framework.ui.BaseActivity
import com.bihe0832.android.lib.text.TextFactoryUtils
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import java.util.Calendar
import com.bihe0832.android.model.res.R as ModelResR
import com.bihe0832.android.framework.R as FrameworkR

open class AboutActivity : BaseActivity() {

    open fun getAboutItemClass(): Class<out AboutFragment> {
        return AboutFragment::class.java
    }

    open fun getItemFragment(): AboutFragment {
        return AboutFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_layout)
        initToolbar(R.id.app_about_toolbar, ThemeResourcesManager.getString(ModelResR.string.about), true)
        initView()
        //仅检查更新，不做升级
        if (findFragment(getAboutItemClass()) == null) {
            loadRootFragment(R.id.about_fragment_content, getItemFragment())
        }
    }

    protected open fun initView() {
        getVersionTextView()?.text =
            resources.getString(ModelResR.string.settings_update_current) + ZixieContext.getVersionName()
        getVersionIcon()?.setOnClickListener(object : ShowDebugClick() {
            override fun onClickAction() {
                showVersionDetail()
            }

            override fun onDebugAction() {

            }
        })
        ThemeResourcesManager.getString(FrameworkR.string.privacy_url).let { privacy_url ->
            if (TextUtils.isEmpty(privacy_url)) {
                getPrivacyTextView()?.visibility = View.GONE
            } else {
                getPrivacyTextView()?.apply {
                    visibility = View.VISIBLE
                    text = TextFactoryUtils.getCharSequenceWithClickAction(
                        resources.getString(ModelResR.string.privacy_agreement_entrance),
                        AgreementPrivacy.getAgreementAndPrivacyClickActionMap(this@AboutActivity)
                    )
                    movementMethod = LinkMovementMethod.getInstance()
                }
            }
        }

        getCopyRightTextView()?.text = getCommonRightText()
    }

    protected open fun getCommonRightText(): String {
        return "Copyright 2019-" + Calendar.getInstance()[Calendar.YEAR] + " " + ThemeResourcesManager.getString(
            ModelResR.string.author
        ) + " .All Rights Reserved"

    }

    protected fun showVersionDetail() {
        getVersionTextView()?.text =
            resources.getString(ModelResR.string.settings_update_current) + ZixieContext.getVersionNameAndCode()
    }

    protected fun getVersionIcon(): ImageView? {
        return findViewById(R.id.about_app_icon) as? ImageView
    }

    protected fun getVersionTextView(): TextView? {
        return findViewById(R.id.about_version_info) as? TextView
    }

    protected fun getCopyRightTextView(): TextView? {
        return findViewById(R.id.about_copyright) as? TextView
    }

    protected fun getPrivacyTextView(): TextView? {
        return findViewById(R.id.about_privacy) as? TextView
    }
}