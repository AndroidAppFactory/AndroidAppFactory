package com.bihe0832.android.app.compose

import android.content.Context
import com.bihe0832.android.common.compose.state.AAFLightColorScheme
import com.bihe0832.android.common.compose.state.DensityState
import com.bihe0832.android.common.compose.state.LanguageItem
import com.bihe0832.android.common.compose.state.LayerToGrayState
import com.bihe0832.android.common.compose.state.MultiLanguageState
import com.bihe0832.android.common.compose.state.ThemeState
import com.bihe0832.android.common.permission.AAFPermissionManager
import com.bihe0832.android.lib.language.MultiLanguageHelper
import com.bihe0832.android.lib.lifecycle.ActivityObserver
import java.util.Locale

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/2.
 * Description: Description
 *
 */
object AAFComposeStateManager {

    private val DEFAULT_LANGUAGE = Locale.US
    private val supportLanguage by lazy {
        mutableListOf<LanguageItem>().apply {
            add(LanguageItem("中文", Locale.SIMPLIFIED_CHINESE))
            add(LanguageItem("English", Locale.US))
        }
    }


    fun init(context: Context) {
        val supportList = supportLanguage.filter { it.locale != null }.toList()
        MultiLanguageState.init(context, supportList, DEFAULT_LANGUAGE)
        LayerToGrayState.update()
        DensityState.getCurrentDensity()
        ThemeState.init(AAFLightColorScheme)
    }


    fun changeLanguage(context: Context, code: Locale?) {
        code?.let {
            if (supportLanguage.find { code == it.locale } != null) {
                // 临时方案，等全切换为Compose即可废弃
                ActivityObserver.getActivityList().toList().forEach { activity ->
                    MultiLanguageHelper.modifyContextLanguageConfig(
                        activity, code
                    )
                    MultiLanguageHelper.modifyContextLanguageConfig(
                        activity.resources, code
                    )
                }
                MultiLanguageState.changeLanguage(context, code)
                AAFPermissionManager.initPermission(context)
            }
        }
    }
}