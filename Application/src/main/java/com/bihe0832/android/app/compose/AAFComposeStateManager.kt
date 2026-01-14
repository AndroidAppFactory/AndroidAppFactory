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
 * AAF Compose 状态管理器
 *
 * 统一管理 Compose UI 相关的全局状态，包括：
 * - 多语言状态管理
 * - 灰度模式状态
 * - 屏幕密度状态
 * - 主题状态
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/2.
 */
object AAFComposeStateManager {

    /** 默认语言：英语 */
    private val DEFAULT_LANGUAGE = Locale.US

    /** 支持的语言列表 */
    private val supportLanguage by lazy {
        mutableListOf<LanguageItem>().apply {
            add(LanguageItem("中文", Locale.SIMPLIFIED_CHINESE))
            add(LanguageItem("English", Locale.US))
        }
    }

    /**
     * 初始化 Compose 状态
     *
     * 初始化多语言、灰度模式、屏幕密度和主题状态
     *
     * @param context 上下文
     */
    fun init(context: Context) {
        val supportList = supportLanguage.filter { it.locale != null }.toList()
        MultiLanguageState.init(context, supportList, DEFAULT_LANGUAGE)
        LayerToGrayState.update()
        DensityState.getCurrentDensity()
        ThemeState.init(AAFLightColorScheme)
    }

    /**
     * 切换应用语言
     *
     * 更新所有已存在 Activity 的语言配置，并刷新 Compose 多语言状态
     *
     * @param context 上下文
     * @param code 目标语言的 Locale
     */
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