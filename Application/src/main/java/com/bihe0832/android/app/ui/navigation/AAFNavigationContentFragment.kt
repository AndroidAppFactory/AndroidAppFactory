package com.bihe0832.android.app.ui.navigation

import android.view.View
import com.bihe0832.android.app.about.getFeedbackItem
import com.bihe0832.android.app.message.AAFMessageManager
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.common.about.R
import com.bihe0832.android.common.main.CommonNavigationContentFragment
import com.bihe0832.android.common.permission.PermissionFragment
import com.bihe0832.android.common.settings.SettingsItem
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.framework.update.UpdateInfoLiveData
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.theme.ThemeResourcesManager

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2023/4/10.
 * Description: Description
 *
 */
class AAFNavigationContentFragment : CommonNavigationContentFragment() {

    override fun initView(view: View) {
        super.initView(view)
        AAFMessageManager.getMessageLiveData().observe(this) { t ->
            changeMessageRedDot(ThemeResourcesManager.getString(R.string.settings_message_title), AAFMessageManager.getUnreadNum())
        }
        UpdateInfoLiveData.observe(this) { t ->
            changeUpdateRedDot(SettingsItem.getAboutTitle(), t, false)
        }
    }

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(SettingsItem.getAboutAPP(UpdateInfoLiveData.value) {
                RouterAction.openPageByRouter(RouterConstants.MODULE_NAME_BASE_ABOUT)
            })
            if (AAFMessageManager.getUnreadNum() > 0) {
                add(SettingsItem.getMessage(AAFMessageManager.getUnreadNum()) {
                    RouterAction.openPageByRouter(RouterConstants.MODULE_NAME_MESSAGE)
                })
            } else {
                add(SettingsItem.getMessage(-1) {
                    RouterAction.openPageByRouter(RouterConstants.MODULE_NAME_MESSAGE)
                })
            }

            add(SettingsItem.getPermission(PermissionFragment::class.java))
            add(SettingsItem.getFeedbackURL())
            add(getFeedbackItem(activity))
            add(SettingsItem.getShareAPP(true))
            addAll(super.getDataList())
            add(SettingsItem.getZixie())
        }.apply {
            processLastItemDriver()
        }
    }


}