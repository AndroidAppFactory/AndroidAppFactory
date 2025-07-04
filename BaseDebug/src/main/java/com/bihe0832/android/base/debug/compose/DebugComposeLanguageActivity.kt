package com.bihe0832.android.base.debug.compose

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.common.compose.common.activity.CommonComposeActivity
import com.bihe0832.android.common.compose.state.MultiLanguageState
import com.bihe0832.android.common.compose.state.RenderState
import com.bihe0832.android.common.debug.compose.DebugItem
import com.bihe0832.android.common.debug.compose.DebugTips
import java.util.Locale

/**
 * @author zixie code@bihe0832.com Created on 2025/2/17. Description: Description
 */
class DebugComposeLanguageActivity : CommonComposeActivity() {

    override fun getContentRender(): RenderState {
        return object : RenderState {
            @Composable
            override fun Content(currentLanguage: Locale) {
                DebugLanguageView(currentLanguage)
            }
        }
    }
}

@Composable
fun DebugLanguageView(currentLanguage: Locale) {
    val context = LocalContext.current
    Column {
        DebugTips(stringResource(R.string.app_name))
        DebugTips("当前语言5：" + MultiLanguageState.getCurrentLanguageState().language)
        DebugItem("切换语言到中文") {
            MultiLanguageState.changeLanguage(context, Locale.CHINESE)
        }
        DebugItem("切换语言到英文") {
            MultiLanguageState.changeLanguage(context, Locale.US)
        }
        CustomCompose2(currentLanguage)
        CustomCompose1(currentLanguage)
    }
}


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun CustomCompose1(currentLanguage: Locale) {
    DebugItem(stringResource(R.string.dialog_title)) { }
    LazyColumn {
        items(1) { index ->
            DebugItem("${stringResource(R.string.dialog_title)}: $index - 0 " + currentLanguage.language) { }
            DebugItem(stringResource(R.string.dialog_title) + ": $index - 1") { }
        }
    }
    CustomCompose2(currentLanguage)
}


@Composable
fun CustomCompose2(currentLanguage: Locale) {
    DebugItem(stringResource(R.string.install_success)) { }
    LazyColumn {
        items(1, key = { currentLanguage }) { index ->
            DebugItem("${stringResource(R.string.install_success)} 2: $index - 0") { }
            DebugItem(stringResource(R.string.install_success) + "2: $index - 1") { }
        }
    }
}
