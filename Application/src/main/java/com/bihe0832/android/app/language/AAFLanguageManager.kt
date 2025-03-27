package com.bihe0832.android.app.language

import android.content.Context
import com.bihe0832.android.common.language.card.SettingsDataLanguage
import com.bihe0832.android.framework.ZixieCoreInit
import com.bihe0832.android.lib.language.MultiLanguageHelper
import java.util.Locale

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/3/27.
 * Description: Description
 *
 */
object AAFLanguageManager {
    private val DEFAULT_LANGUAGE = Locale.US
    private val supportLanguage by lazy {
        mutableListOf<SettingsDataLanguage>().apply {
            add(getLanguageItem("中文", Locale.CHINESE))
            add(getLanguageItem("English", Locale.US))
        }
    }

    fun getLanguageItem(titleName: String, localeInfo: Locale): SettingsDataLanguage {
        return SettingsDataLanguage().apply {
            this.title = titleName
            this.locale = localeInfo
            this.useLanguage = true
        }
    }

    fun getSupportLanguageList(): MutableList<SettingsDataLanguage> {
        return supportLanguage
    }

    fun init(context: Context) {
        val supportList = supportLanguage.mapNotNull { it.locale }.toList()
        MultiLanguageHelper.init(supportList)
        val lastLocale = MultiLanguageHelper.getLanguageConfig(context)
        if (supportList.find { it.language.equals(lastLocale.language) } == null) {
            ZixieCoreInit.updateApplicationLocale(context, DEFAULT_LANGUAGE)
        }
    }
}