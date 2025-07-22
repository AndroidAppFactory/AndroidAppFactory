package com.bihe0832.android.base.compose.debug.common

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.bihe0832.android.app.compose.AAFComposeStateManager
import com.bihe0832.android.base.compose.debug.DebugComposeViewPreviewParameterProvider
import com.bihe0832.android.common.compose.debug.item.DebugComposeItem
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.state.AAFDarkColorScheme
import com.bihe0832.android.common.compose.state.AAFLightColorScheme
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
fun DebugComposeConfigViewPreview(@PreviewParameter(DebugComposeViewPreviewParameterProvider::class) currentLanguage: Locale) {
    DebugComposeConfigView(currentLanguage = currentLanguage)
}

@Composable
fun DebugComposeConfigView(currentLanguage: Locale) {
    val context = LocalContext.current

    Column {
        DebugComposeItem("切换语言到调试", "CommonLanguageView")
        DebugItem("切换语言到中文") {
            AAFComposeStateManager.changeLanguage(context, Locale.CHINESE)
        }
        DebugItem("切换语言到英文") {
            AAFComposeStateManager.changeLanguage(context, Locale.US)
        }

        DebugItem("切换主题到亮色") {
            ThemeState.changeTheme(AAFLightColorScheme)
        }
        DebugItem("切换主题到暗色") {
            ThemeState.changeTheme(AAFDarkColorScheme)
        }

    }
}
