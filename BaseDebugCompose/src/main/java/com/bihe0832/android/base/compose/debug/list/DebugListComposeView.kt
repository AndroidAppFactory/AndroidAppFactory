package com.bihe0832.android.base.compose.debug.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.app.router.RouterConstants
import com.bihe0832.android.app.router.RouterHelper
import com.bihe0832.android.base.compose.debug.DebugTempView
import com.bihe0832.android.base.compose.debug.list.model.DebugPageListActivity
import com.bihe0832.android.common.compose.debug.DebugUtilsV2
import com.bihe0832.android.common.compose.debug.item.DebugComposeItem
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.ui.DebugContent


@Preview
@Composable
fun DebugListComposeView() {
    val context = LocalContext.current
    DebugContent {
        DebugItem("简单的单次列表") {
            RouterHelper.openPageByRouter(RouterConstants.MODULE_NAME_LANGUAGE)
        }
        DebugItem("调试的单次列表") {
            DebugUtilsV2.startActivityWithException(
                context,
                DebugOnceListActivity::class.java,
                null
            )
        }
        DebugItem("调试的分页列表") {
            DebugUtilsV2.startActivityWithException(
                context,
                DebugPageListActivity::class.java,
                null
            )
        }

        DebugComposeItem("Flex 布局联动", "DebugFlexWithScrollAndExamplePre") { DebugFlexWithScrollAndExamplePre() }

    }
}
