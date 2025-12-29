package com.bihe0832.android.base.compose.debug.language

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.app.compose.AAFComposeStateManager
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.item.DebugTips
import com.bihe0832.android.lib.aaf.res.R as ResR
import com.bihe0832.android.model.res.R as ModelResR
import com.bihe0832.android.common.compose.state.MultiLanguageState
import com.bihe0832.android.common.compose.state.aafStringResource
import com.bihe0832.android.lib.permission.PermissionManager
import java.util.Locale


@Preview
@Composable
fun DebugLanguageComposeView() {
    val context = LocalContext.current
    Column {
        DebugTips(stringResource(ResR.string.app_name))
        DebugTips("当前语言：" + MultiLanguageState.getCurrentLanguageState().language + "，当前Context语言：" + LocalContext.current.resources.configuration.locale.language)
        DebugTips("Locale.getDefault：" + Locale.getDefault().language + ",Locale.Current：" + androidx.compose.ui.text.intl.Locale.current)
        CustomCompose2(1)
        CustomCompose1()
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
    }
}


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
internal fun CustomCompose1() {

    Text(modifier = Modifier.background(Color.Red), text = aafStringResource(ModelResR.string.dialog_title))
    DebugItem(stringResource(ModelResR.string.dialog_title)) { }
    DebugItem(aafStringResource(ModelResR.string.dialog_title)) { }
    LazyColumn {
        items(1) { index ->
            DebugItem("${stringResource(ModelResR.string.dialog_title)}: $index - 0 ") { }
        }
    }
    CustomCompose2(2)
}


@Composable
internal fun CustomCompose2(num: Int) {
    DebugItem(aafStringResource(ModelResR.string.install_success)) { }
    LazyColumn {
        items(1, key = { item ->
            // 将外部变量加入key，变化时触发重建
            "$item"
        }) { index ->
            DebugItem("${aafStringResource(ModelResR.string.install_success)} 2 - $num: $index - 0") { }
        }
    }
}
