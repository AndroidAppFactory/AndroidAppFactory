package com.bihe0832.android.base.compose.debug.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.bihe0832.android.app.compose.AAFComposeStateManager
import com.bihe0832.android.base.compose.debug.R
import com.bihe0832.android.base.compose.debug.language.DebugComposeLanguageView
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.common.compose.debug.item.DebugComposeItem
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.item.DebugTips
import com.bihe0832.android.common.compose.state.AAFDarkColorScheme
import com.bihe0832.android.common.compose.state.AAFLightColorScheme
import com.bihe0832.android.common.compose.state.ThemeState
import com.bihe0832.android.common.compose.state.aafStringResource
import java.util.Locale

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/5.
 * Description: Description
 *
 */
@Composable
fun DebugComposeConfigView() {
    val context = LocalContext.current

    DebugContent {
        DebugTips(stringResource(R.string.app_name))
        DebugTips(aafStringResource(R.string.app_name))
//        DebugComposeItem("语言调试", "DebugComposeLanguageView") {
//            DebugComposeLanguageView()
//        }
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
