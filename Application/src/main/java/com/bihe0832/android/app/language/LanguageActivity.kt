package com.bihe0832.android.app.language

import com.bihe0832.android.app.R
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.common.language.BaseLanguageActivity
import com.bihe0832.android.common.language.card.SettingsDataLanguage
import com.bihe0832.android.common.permission.AAFPermissionManager
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.lib.router.annotation.Module

/**
 * @author zixie code@bihe0832.com Created on 2025/3/27. Description: Description
 */
@Module(RouterConstants.MODULE_NAME_LANGUAGE)
class LanguageActivity : BaseLanguageActivity() {
    override fun getLanguageList(): MutableList<SettingsDataLanguage> {
        return AAFLanguageManager.getSupportLanguageList()
    }

    override fun setLocale(settingData: SettingsDataLanguage?) {
        if (null != settingData?.locale) {
            updateApplicationLocale(settingData.locale!!)
            AAFPermissionManager.initPermission(applicationContext)
            onLocaleChanged(getLastLocale(), settingData.locale!!)
        } else {
            ZixieContext.showToast(resources.getString(R.string.toast_settings_language_tips))
        }
    }
}
