package com.bihe0832.android.common.compose.state

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ZixieCoreInit
import com.bihe0832.android.lib.language.MultiLanguageHelper
import java.util.Locale

object MultiLanguageState {

    private var supportLanguage: List<LanguageItem> by mutableStateOf(emptyList())

    private var _currentLanguage: Locale by mutableStateOf(Locale.getDefault())

    fun init(context: Context, locales: List<LanguageItem>, default: Locale) {
        supportLanguage = locales.filter { it.locale != null }
        val lastLocale = MultiLanguageHelper.getLanguageConfig(context)
        if (supportLanguage.find { lastLocale.language == it.locale?.language && lastLocale.country == it.locale?.country } == null) {
            _currentLanguage = default
            ZixieCoreInit.updateApplicationLocale(context, default)
        } else {
            _currentLanguage = lastLocale
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
            if (_currentLanguage != code && supportLanguage.find { code == it.locale } != null) {
                MultiLanguageHelper.setLanguageConfig(context, code)
                ZixieContext.updateApplicationContext(context, true)
                MultiLanguageHelper.modifyContextLanguageConfig(context, code)
                _currentLanguage = code
            }
        }
    }
}

data class LanguageItem(
    var title: String = "", var locale: Locale? = null
)

@Composable
fun aafStringResource(@StringRes id: Int): String {
    val currentLanguage by rememberUpdatedState(MultiLanguageState.getCurrentLanguageState())
    return stringResource(id, currentLanguage)
}