package com.bihe0832.android.common.compose.state

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.bihe0832.android.framework.ZixieCoreInit
import com.bihe0832.android.lib.language.MultiLanguageHelper
import java.util.Locale

object MultiLanguageState {

    private var supportLanguage: List<LanguageItem> by mutableStateOf(emptyList())

    private var _currentLanguage: Locale by mutableStateOf(Locale.getDefault())

    fun init(context: Context, locales: List<LanguageItem>, default: Locale) {
        supportLanguage = locales
        _currentLanguage = MultiLanguageHelper.getLanguageConfig(context)
        if (supportLanguage.find { _currentLanguage.language.equals(it.locale?.language) } == null) {
            _currentLanguage = default
            ZixieCoreInit.updateApplicationLocale(context, default)
        }
    }

    fun getCurrentLanguageState(): Locale {
        return _currentLanguage
    }

    fun getSupportLanguageList(): List<LanguageItem> {
        return supportLanguage
    }

    fun changeLanguage(context: Context, code: Locale?) {
        code?.let {
            _currentLanguage = code
            MultiLanguageHelper.setLanguageConfig(context, code)
        }
    }
}

data class LanguageItem(
    var title: String = "", var locale: Locale? = null
)

@Composable
fun MultiLanguageContent(content: @Composable (currentLanguage: Locale) -> Unit) {
    val currentLanguage by rememberUpdatedState(MultiLanguageState.getCurrentLanguageState())
    content(currentLanguage)
}