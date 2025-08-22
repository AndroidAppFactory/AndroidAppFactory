package com.bihe0832.android.base.compose.debug.dialog

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.common.compose.debug.item.CHANGE_ENV_EXIST_TYPE_EXIST
import com.bihe0832.android.common.compose.debug.item.CHANGE_ENV_EXIST_TYPE_NOTHING
import com.bihe0832.android.common.compose.debug.item.CHANGE_ENV_EXIST_TYPE_RESTART
import com.bihe0832.android.common.compose.debug.item.DebugComposeActivityItem
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.item.DebugTips
import com.bihe0832.android.common.compose.debug.item.getChangeEnvSelectDialog
import com.bihe0832.android.common.compose.debug.item.showChangeEnvDialog
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.common.share.ShareAPPActivity
import com.bihe0832.android.lib.ui.dialog.impl.BottomDialog
import com.bihe0832.android.lib.ui.view.ext.getActivity


@Preview
@Composable
fun DebugComposeDialogViewPreview() {
    DebugDialogComposeView()
}

@Composable
fun showChangeEnv(activity: Activity, title: String, action: Int) {
    DebugItem(title) {
        mutableListOf<String>().apply {
            add("测试环境1")
            add("测试环境2")
        }.let { data ->
            getChangeEnvSelectDialog(
                activity,
                "查看应用版本及环境",
                data,
                0,
            ) { index ->
                showChangeEnvDialog(
                    activity,
                    "应用环境重启",
                    data.get(index),
                    action
                )
            }.show()
        }
    }
}

@Composable
fun DebugDialogComposeView() {
    val context = LocalContext.current
    val activity = context.getActivity()!!

    DebugContent {
        DebugTips("一些特殊场景的弹框")
        DebugItem("唯一弹框(无论调用多少次，只弹一次)") { testUnique(activity) }
        DebugItem("逐次弹框，前一个关闭后一个弹出(从0到5)") { testBlock(activity) }
        DebugTips("强制按顺序弹框，前一个全弹完才能有下一个，建议测试时先多次手动，然后触发自动")
        DebugItem("强制按顺序弹框添加弹框、不自动弹") { testSequence1(activity) }
        DebugItem("强制按顺序弹框自动弹") { testSequence2(activity) }

        DebugItem("强制按顺序弹框手动触发启动") {
            mDependenceBlockDialogManager.start()
            resume()
        }
        DebugItem("强制按顺序弹框手动触发暂停") { pause() }
        DebugItem("强制按顺序弹框重置清空") { reset() }
        DebugTips("通用弹框")
        DebugItem("底部Dialog") { showAlert(activity, BottomDialog(activity)) }
        DebugItem("底部列表弹框") { showBottomDialog(activity) }
        
        DebugItem("通用弹框") { testAlert(activity) }
        DebugItem("单选列表弹框") { testRadio(activity) }
        DebugItem("自定义弹框") { testCustom(activity) }
        DebugItem("URL图片竖弹框") { testVURLImage(activity) }
        DebugItem("URL图片横弹框") { testHURLImage(activity) }
        DebugItem("本地资源竖弹框") { testImage(activity) }
        DebugItem("通用确认弹框") { testAlertTools(activity) }
        DebugItem("通用带输入弹框") { testInput(activity) }
        DebugItem("进度条弹框") { testUpdate(activity) }
        DebugItem("加载弹框") { testLoading(activity) }
        showChangeEnv(activity, "模拟环境切换并自动重启", CHANGE_ENV_EXIST_TYPE_RESTART)
        showChangeEnv(activity, "模拟环境切换并自动退出", CHANGE_ENV_EXIST_TYPE_EXIST)
        showChangeEnv(activity, "模拟环境切换并立即生效", CHANGE_ENV_EXIST_TYPE_NOTHING)
    }
}

