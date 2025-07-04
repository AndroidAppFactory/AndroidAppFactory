package com.bihe0832.android.base.debug.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.common.compose.state.MultiLanguageState
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.common.compose.state.ThemeState
import com.bihe0832.android.common.debug.DebugUtils
import com.bihe0832.android.common.debug.compose.DebugItem
import com.bihe0832.android.common.debug.compose.DebugTips
import com.bihe0832.android.common.list.compose.CommonComposeListActivity
import java.util.Locale

/**
 * @author zixie code@bihe0832.com Created on 2025/2/17. Description: Description
 */
class DebugComposeActivity : CommonComposeListActivity() {

    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content(currentLanguage: Locale) {
                DebugComposeView()
            }
        }
    }
}

@Preview
@Composable
fun DebugComposeView() {
    val context = LocalContext.current

    Column {
        DebugItem("切换语言到调试") {
            DebugUtils.startActivityWithException(context, DebugComposeLanguageActivity::class.java)
        }
        DebugTips(stringResource(R.string.app_name))
        DebugItem("切换语言到中文") {
            MultiLanguageState.changeLanguage(context, Locale.CHINESE)
        }
        DebugItem("切换语言到英文") {
            MultiLanguageState.changeLanguage(context, Locale.US)
        }

        DebugItem("切换主题到亮色") {
            ThemeState.changeTheme(ThemeState.AAFLightColorScheme)
        }
        DebugItem("切换主题到暗色") {
            ThemeState.changeTheme(ThemeState.AAFDarkColorScheme)
        }

    }
}
