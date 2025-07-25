package com.bihe0832.android.common.language

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.res.stringResource
import com.bihe0832.android.common.compose.common.activity.CommonComposeActivity
import com.bihe0832.android.common.compose.state.LanguageItem
import com.bihe0832.android.common.compose.state.MultiLanguageState
import com.bihe0832.android.common.compose.state.RenderState
import java.util.Locale

/**
 * @author zixie code@bihe0832.com Created on 2025/2/17. Description: Description
 */

abstract class BaseLanguageActivity : CommonComposeActivity() {

    @Composable
    abstract fun getLanguageItemCompose(languageItem: LanguageItem, currentLanguage: Locale)

    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content() {
                val languageList: List<LanguageItem> by rememberUpdatedState(
                    MultiLanguageState.getSupportLanguageList()
                )
                val currentLanguage by rememberUpdatedState(MultiLanguageState.getCurrentLanguageState())
                LazyColumn {
                    items(languageList) {
                        getLanguageItemCompose(it, currentLanguage)
                    }
                }
            }
        }
    }

    @Composable
    override fun getTitleName(): String {
        return stringResource(R.string.settings_language_title)
    }

}


