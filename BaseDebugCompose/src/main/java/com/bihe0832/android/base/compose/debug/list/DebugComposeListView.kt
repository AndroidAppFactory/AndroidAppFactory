package com.bihe0832.android.base.compose.debug.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.base.compose.debug.list.model.DebugPageListActivity
import com.bihe0832.android.common.compose.debug.DebugComposeRootActivity.Companion.startActivityWithException
import com.bihe0832.android.common.compose.debug.DebugContent
import com.bihe0832.android.common.compose.debug.item.DebugItem


@Preview
@Composable
fun DebugComposeListView() {
    val context = LocalContext.current
    DebugContent {
        DebugItem("简单的单次列表") {
            RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_LANGUAGE)
        }
        DebugItem("调试的单次列表") {
            startActivityWithException(context, DebugOnceListActivity::class.java, null)
        }
        DebugItem("调试的分页列表") {
            startActivityWithException(context, DebugPageListActivity::class.java, null)
        }
    }
}
