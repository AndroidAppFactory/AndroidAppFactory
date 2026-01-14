package com.bihe0832.android.app.language

import androidx.compose.runtime.Composable
import com.bihe0832.android.app.compose.AAFComposeStateManager
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.common.compose.state.LanguageItem
import com.bihe0832.android.common.language.BaseLanguageActivity
import com.bihe0832.android.common.language.item.LanguageItemCompose
import com.bihe0832.android.lib.router.annotation.Module
import java.util.Locale

/**
 * AAF 多语言设置页面
 *
 * 提供应用语言切换功能，支持中文和英文
 * 通过路由 {@link RouterConstants#MODULE_NAME_LANGUAGE} 访问
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/3/27.
 */
@Module(RouterConstants.MODULE_NAME_LANGUAGE)
open class LanguageActivity : BaseLanguageActivity() {

    /**
     * 获取语言选项的 Compose 组件
     *
     * @param languageItem 语言项数据
     * @param currentLanguage 当前选中的语言
     * @return 语言选项 Compose 组件
     */
    @Composable
    override fun getLanguageItemCompose(languageItem: LanguageItem, currentLanguage: Locale) {
        return LanguageItemCompose(
            title = languageItem.title,
            event = {
                AAFComposeStateManager.changeLanguage(this, languageItem.locale!!)
            },
            isSelected = currentLanguage == languageItem.locale
        )
    }
}

