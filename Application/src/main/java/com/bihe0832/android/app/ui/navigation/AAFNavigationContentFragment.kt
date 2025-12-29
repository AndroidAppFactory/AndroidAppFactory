package com.bihe0832.android.app.ui.navigation

import android.view.View
import com.bihe0832.android.app.about.getFeedbackItem
import com.bihe0832.android.model.res.R as ModelResR
import com.bihe0832.android.framework.R as FrameworkR
import com.bihe0832.android.app.message.AAFMessageManager
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.common.main.CommonNavigationContentFragment
import com.bihe0832.android.common.permission.settings.PermissionFragment
import com.bihe0832.android.common.permission.settings.PermissionItem
import com.bihe0832.android.common.settings.SettingsItem
import com.bihe0832.android.common.settings.card.SettingsDataGo
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
            changeMessageRedDot(
                ThemeResourcesManager.getString(ModelResR.string.settings_message_title),
                AAFMessageManager.getUnreadNum(),
            )
        }
        UpdateInfoLiveData.observe(this) { t ->
            changeUpdateRedDot(SettingsItem.getAboutTitle(), t, false)
        }
    }

    fun getChangeLanguage(): SettingsDataGo {
        return SettingsDataGo(resources.getString(ModelResR.string.settings_language_title)).apply {
            mItemIconRes = FrameworkR.drawable.icon_language
            mShowDriver = true
            mShowGo = true
            mHeaderListener = object : View.OnClickListener {
                override fun onClick(p0: View?) {
                    RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_LANGUAGE)
                }
            }
        }
    }

    override fun getDataList(processLast: Boolean): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(
                SettingsItem.getAboutAPP(UpdateInfoLiveData.value) {
                    RouterAction.openPageByRouter(RouterConstants.MODULE_NAME_BASE_ABOUT)
                },
            )
            if (AAFMessageManager.getUnreadNum() > 0) {
                add(
                    SettingsItem.getMessage(AAFMessageManager.getUnreadNum()) {
                        RouterAction.openPageByRouter(RouterConstants.MODULE_NAME_MESSAGE)
                    },
                )
            } else {
                add(
                    SettingsItem.getMessage(-1) {
                        RouterAction.openPageByRouter(RouterConstants.MODULE_NAME_MESSAGE)
                    },
                )
            }

            add(PermissionItem.getPermission(context!!, PermissionFragment::class.java))
            add(getFeedbackItem(activity))
            add(SettingsItem.getShareAPP(true,resources.getString(ModelResR.string.com_bihe0832_share_title)))
            add(getChangeLanguage())
            addAll(super.getDataList(false))
            add(SettingsItem.getClearCache(activity!!))
            add(SettingsItem.getZixie())
        }.apply {
            processLastItemDriver(processLast)
        }
    }
}
