package com.bihe0832.android.base.compose.debug.common.item

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.bihe0832.android.app.compose.AAFComposeStateManager
import com.bihe0832.android.base.compose.debug.R
import com.bihe0832.android.base.compose.debug.common.LocaleParameterProvider
import com.bihe0832.android.common.compose.debug.item.DebugComposeItem
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.item.DebugTips
import com.bihe0832.android.common.compose.state.MultiLanguageState
import com.bihe0832.android.common.compose.state.ThemeState
import java.util.Locale

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/5.
 * Description: Description
 *
 */

@Preview
@Composable
fun DebugCommonConfigViewPreview(
    @PreviewParameter(LocaleParameterProvider::class) currentLanguage: Locale
) {
    DebugCommonConfigView(currentLanguage = currentLanguage)
}

@Composable
fun DebugCommonConfigView(currentLanguage: Locale) {
    val context = LocalContext.current

    Column {
        DebugTips(stringResource(R.string.app_name))
        DebugComposeItem("切换语言到调试", "CommonLanguageView")
        DebugItem("切换语言到中文") {
            AAFComposeStateManager.changeLanguage(context, Locale.CHINESE)
        }
        DebugItem("切换语言到英文") {
            AAFComposeStateManager.changeLanguage(context, Locale.US)
        }

        DebugItem("切换主题到亮色") {
            ThemeState.changeTheme(ThemeState.AAFLightColorScheme)
        }
        DebugItem("切换主题到暗色") {
            ThemeState.changeTheme(ThemeState.AAFDarkColorScheme)
        }

    }
}
