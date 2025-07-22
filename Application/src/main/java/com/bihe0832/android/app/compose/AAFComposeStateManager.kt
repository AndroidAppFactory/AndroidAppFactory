package com.bihe0832.android.app.compose

import android.content.Context
import com.bihe0832.android.common.compose.state.AAFDarkColorScheme
import com.bihe0832.android.common.compose.state.AAFLightColorScheme
import com.bihe0832.android.common.compose.state.DensityState
import com.bihe0832.android.common.compose.state.LanguageItem
import com.bihe0832.android.common.compose.state.LayerToGrayState
import com.bihe0832.android.common.compose.state.MultiLanguageState
import com.bihe0832.android.common.compose.state.ThemeState
import com.bihe0832.android.common.permission.AAFPermissionManager
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
            add(LanguageItem("中文", Locale.CHINESE))
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
        MultiLanguageState.changeLanguage(context, code)
        AAFPermissionManager.initPermission(context)
    }
}