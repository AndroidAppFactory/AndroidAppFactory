package com.bihe0832.android.app.about

import android.os.Bundle
import com.bihe0832.android.app.R
import com.bihe0832.android.app.update.UpdateManager.checkUpdateAndShowDialog
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.debug.ShowDebugClick
import com.bihe0832.android.framework.ui.BaseActivity
import com.bihe0832.android.lib.router.annotation.Module
import kotlinx.android.synthetic.main.about_layout.*
import java.util.*

const val MODULE_NAME_BASE_ABOUT = "babout"

@Module(MODULE_NAME_BASE_ABOUT)
class AboutActivity : BaseActivity() {

    open fun getAboutItemClass(): Class<out AboutFragment> {
        return AboutFragment::class.java
    }

    open fun getItemFragment(): AboutFragment {
        return AboutFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_layout)
        initToolbar(R.id.app_about_toolbar, "关于我们", true)
        initView()
        //仅检查更新，不做升级
        if (findFragment(getAboutItemClass()) == null) {
            loadRootFragment(R.id.about_fragment_content, getItemFragment())
        }
        checkUpdateAndShowDialog(this, false)
    }

    private fun initView() {
        about_version_info.text = "当前版本：" + ZixieContext.getVersionName()
        about_app_icon.setOnClickListener(object : ShowDebugClick() {
            override fun onClickAction() {
                about_version_info.text = "当前版本：" + ZixieContext.getVersionNameAndCode()
            }
        })
        about_copyright.text = "Copyright 2019-" + Calendar.getInstance()[Calendar.YEAR] + " ZIXIE.All Rights Reserved"
    }

}