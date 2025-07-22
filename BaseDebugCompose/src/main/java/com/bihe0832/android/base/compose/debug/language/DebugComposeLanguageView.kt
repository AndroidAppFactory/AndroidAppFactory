package com.bihe0832.android.base.compose.debug.language

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.bihe0832.android.app.compose.AAFComposeStateManager
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.base.compose.debug.R
import com.bihe0832.android.base.compose.debug.DebugComposeViewPreviewParameterProvider
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.item.DebugTips
import com.bihe0832.android.common.compose.state.MultiLanguageState
import com.bihe0832.android.lib.permission.PermissionManager
import java.util.Locale


@Preview
@Composable
fun DebugComposeLanguageViewPreview(
    @PreviewParameter(DebugComposeViewPreviewParameterProvider::class) currentLanguage: Locale
) {
    DebugComposeLanguageView(currentLanguage = currentLanguage)
}

@Composable
fun DebugComposeLanguageView(currentLanguage: Locale) {
    val context = LocalContext.current
    Column {

        DebugTips(stringResource(R.string.app_name))
        DebugTips("当前语言：" + MultiLanguageState.getCurrentLanguageState().language)
        DebugItem("AAF 自带语言切换页面") {
            RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_LANGUAGE)
        }
        DebugItem("切换语言到中文") {
            AAFComposeStateManager.changeLanguage(context, Locale.CHINESE)
        }
        DebugItem("切换语言到英文") {
            AAFComposeStateManager.changeLanguage(context, Locale.US)
        }
        DebugItem("弹Dialog让UI切后台") {
            PermissionManager.checkPermission(context!!, Manifest.permission.CAMERA)
        }
        CustomCompose2(1, currentLanguage)
        CustomCompose1(currentLanguage)
    }
}


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
internal fun CustomCompose1(currentLanguage: Locale) {
    DebugItem(stringResource(R.string.dialog_title)) { }
    LazyColumn {
        items(1) { index ->
            DebugItem("${stringResource(R.string.dialog_title)}: $index - 0 " + currentLanguage.language) { }
            DebugItem(stringResource(R.string.dialog_title) + ": $index - 1") { }
        }
    }
    CustomCompose2(2, currentLanguage)
}


@Composable
internal fun CustomCompose2(num: Int, currentLanguage: Locale) {
    DebugItem(stringResource(R.string.install_success)) { }
    LazyColumn {
        items(1, key = { currentLanguage }) { index ->
            DebugItem("${stringResource(R.string.install_success)} 2 - $num: $index - 0") { }
            DebugItem(stringResource(R.string.install_success) + " 2 - $num: $index - 1") { }
        }
    }
}
