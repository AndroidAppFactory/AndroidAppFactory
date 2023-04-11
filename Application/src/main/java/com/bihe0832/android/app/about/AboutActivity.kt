package com.bihe0832.android.app.about

import android.os.Bundle
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.update.UpdateManager
import com.bihe0832.android.framework.debug.ShowDebugClick
import com.bihe0832.android.lib.router.annotation.Module


@Module(RouterConstants.MODULE_NAME_BASE_ABOUT)
open class AboutActivity : com.bihe0832.android.common.about.AboutActivity() {

    override fun getAboutItemClass(): Class<out AboutFragment> {
        return AboutFragment::class.java
    }

    override fun getItemFragment(): AboutFragment {
        return AboutFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UpdateManager.checkUpdateAndShowDialog(this, checkUpdateByUser = false, showIfNeedUpdate = true)
    }

    override fun initView() {
        super.initView()
        getVersionIcon().setOnClickListener(object : ShowDebugClick() {
            override fun onClickAction() {
                showVersionDetail()
            }

            override fun onDebugAction() {

            }
        })
    }
}