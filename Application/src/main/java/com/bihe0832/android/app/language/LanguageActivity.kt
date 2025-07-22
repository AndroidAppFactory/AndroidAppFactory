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
 * @author zixie code@bihe0832.com Created on 2025/3/27. Description: Description
 */
@Module(RouterConstants.MODULE_NAME_LANGUAGE)
open class LanguageActivity : BaseLanguageActivity() {

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

