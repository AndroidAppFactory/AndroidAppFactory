package com.bihe0832.android.base.compose.debug

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bihe0832.android.base.compose.debug.cache.DebugCacheComposeView
import com.bihe0832.android.base.compose.debug.clipboard.DebugClipboardComposeView
import com.bihe0832.android.base.compose.debug.color.DebugColorFragment
import com.bihe0832.android.base.compose.debug.convert.DebugConvertComposeView
import com.bihe0832.android.base.compose.debug.dialog.DebugDialogComposeView
import com.bihe0832.android.base.compose.debug.download.DebugDownloadView
import com.bihe0832.android.base.compose.debug.encrypt.DebugEncryptView
import com.bihe0832.android.base.compose.debug.file.DebugFileComposeView
import com.bihe0832.android.base.compose.debug.google.DebugGoogleFragment
import com.bihe0832.android.base.compose.debug.intent.DebugIntentComposeView
import com.bihe0832.android.base.compose.debug.ipc.DebugIPCView
import com.bihe0832.android.base.compose.debug.list.DebugListComposeView
import com.bihe0832.android.base.compose.debug.log.DebugLogComposeView
import com.bihe0832.android.base.compose.debug.media.DebugMediaComposeView
import com.bihe0832.android.base.compose.debug.message.DebugMessageComposeView
import com.bihe0832.android.base.compose.debug.notify.DebugNotifyComposeView
import com.bihe0832.android.base.compose.debug.qrcode.DebugQRCodeFragment
import com.bihe0832.android.base.compose.debug.request.DebugHttpActivity
import com.bihe0832.android.base.compose.debug.shake.DebugShakeView
import com.bihe0832.android.base.compose.debug.share.DebugShareComposeView
import com.bihe0832.android.base.compose.debug.task.DebugEnqueueView
import com.bihe0832.android.base.compose.debug.task.DebugThreadAndCoroutinesView
import com.bihe0832.android.base.compose.debug.toast.DebugToastComposeView
import com.bihe0832.android.base.compose.debug.ui.DebugApplicaionUIView
import com.bihe0832.android.common.compose.debug.item.DebugComposeActivityItem
import com.bihe0832.android.common.compose.debug.item.DebugComposeFragmentItem
import com.bihe0832.android.common.compose.debug.item.DebugComposeItem
import com.bihe0832.android.common.compose.debug.ui.DebugContent

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/7/5.
 * Description: Description
 *
 */

@Preview
@Composable
fun AAFDebugModuleView() {

    DebugContent {
        DebugComposeItem("临时测试(Temp)", "DebugTempView") { DebugTempView() }
        DebugComposeItem("下载及安装 Download 调试", "DebugDownloadView") { DebugDownloadView() }
        DebugComposeItem("文件管理相关", "DebugFileComposeView") { DebugFileComposeView() }
        DebugComposeItem(
            "Dialog (不同类型、顺序、底部，分享等)调试", "DebugDialogComposeView"
        ) { DebugDialogComposeView() }
        DebugComposeItem("拍照、相册、图片、视频、音频操作调试", "DebugMediaComposeView") {
            DebugMediaComposeView()
        }
        DebugComposeItem("AES、RSA、MD5、SHA256等", "DebugEncryptView") { DebugEncryptView() }

        DebugComposeItem(
            "Cache、配置、数据中心测试", "DebugCacheComposeView"
        ) { DebugCacheComposeView() }
        DebugComposeItem("剪切板调试", "DebugClipboardComposeView") { DebugClipboardComposeView() }
        DebugComposeItem("数据处理（格式转化、Json、拼音简繁体等）", "DebugConvertComposeView") {
            DebugConvertComposeView()
        }
        DebugComposeItem(
            "UI（换肤、APPIcon切换、哀悼日、多语言、前后台等）测试", "DebugApplicaionUIView"
        ) {
            DebugApplicaionUIView()
        }
        DebugComposeItem(
            "UI（Intent跳转、反馈、评分、锁屏、隐私弹框、设置等）测试", "DebugIntentComposeView"
        ) {
            DebugIntentComposeView()
        }
        DebugComposeFragmentItem("UI（颜色取色器）测试", DebugColorFragment::class.java)
        DebugComposeItem("公告消息 调试", "DebugMessageComposeView") { DebugMessageComposeView() }

        DebugComposeItem("定时任务、阻塞任务、延迟任务", "DebugEnqueueView") { DebugEnqueueView() }
        DebugComposeItem(
            "协程、多线程、前台服务", "DebugThreadAndCoroutinesView"
        ) { DebugThreadAndCoroutinesView() }
        DebugComposeItem("摇一摇、震动等测试", "DebugShakeView") { DebugShakeView() }
        DebugComposeItem("Compose List 调试", "DebugListComposeView") { DebugListComposeView() }
        DebugComposeItem("Toast 调试", "DebugToastComposeView") { DebugToastComposeView() }
        DebugComposeItem("分享、底部Activity 调试", "DebugShareComposeView") {
            DebugShareComposeView()
        }

        DebugComposeFragmentItem("二维码调试", DebugQRCodeFragment::class.java)

        DebugComposeItem("日志调试", "DebugLogComposeView") { DebugLogComposeView() }
        DebugComposeFragmentItem("Google相关 调试", DebugGoogleFragment::class.java)
        DebugComposeItem("通知栏调试", "DebugNotifyComposeView") { DebugNotifyComposeView() }
        DebugComposeItem("多进程调试", "DebugIPCView") { DebugIPCView() }
        DebugComposeActivityItem("HTTP Request", DebugHttpActivity::class.java)
    }
}


