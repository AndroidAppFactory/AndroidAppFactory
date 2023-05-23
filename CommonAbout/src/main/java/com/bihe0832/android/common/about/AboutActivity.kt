package com.bihe0832.android.common.about

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.debug.ShowDebugClick
import com.bihe0832.android.framework.ui.BaseActivity
import com.bihe0832.android.lib.theme.ThemeResourcesManager
import kotlinx.android.synthetic.main.about_layout.*
import java.util.*

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
        initToolbar(R.id.app_about_toolbar, ThemeResourcesManager.getString(R.string.about), true)
        initView()
        //仅检查更新，不做升级
        if (findFragment(getAboutItemClass()) == null) {
            loadRootFragment(R.id.about_fragment_content, getItemFragment())
        }
    }

    protected open fun initView() {
        getVersionTextView().text = "当前版本：" + ZixieContext.getVersionName()
        getVersionIcon().setOnClickListener(object : ShowDebugClick() {
            override fun onClickAction() {
                showVersionDetail()
            }

            override fun onDebugAction() {

            }
        })
        getCopyRightTextView().text = "Copyright 2019-" + Calendar.getInstance()[Calendar.YEAR] + " " + ThemeResourcesManager.getString(R.string.author) + " .All Rights Reserved"
    }

    protected fun showVersionDetail() {
        getVersionTextView().text = "当前版本：" + ZixieContext.getVersionNameAndCode()
    }

    protected fun getVersionIcon(): ImageView {
        return about_app_icon
    }

    protected fun getVersionTextView(): TextView {
        return about_version_info
    }

    protected fun getCopyRightTextView(): TextView {
        return about_copyright
    }
}