/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug

import androidx.compose.runtime.Composable
import com.bihe0832.android.base.debug.cache.DebugCacheFragment
import com.bihe0832.android.base.debug.card.DebugListFragment
import com.bihe0832.android.base.debug.card.TestListActivity
import com.bihe0832.android.base.debug.clipboard.DebugClipboardFragment
import com.bihe0832.android.base.debug.color.DebugColorFragment
import com.bihe0832.android.base.debug.convert.DebugConvertFragment
import com.bihe0832.android.base.debug.dialog.DebugDialogFragment
import com.bihe0832.android.base.debug.download.DebugDownloadFragment
import com.bihe0832.android.base.debug.encrypt.DebugEncryptFragment
import com.bihe0832.android.base.debug.file.DebugFileFragment
import com.bihe0832.android.base.debug.floatview.DebugFloatViewFragment
import com.bihe0832.android.base.debug.google.DebugGoogleFragment
import com.bihe0832.android.base.debug.immersion.DebugImmersionActivity
import com.bihe0832.android.base.debug.intent.DebugIntentFragment
import com.bihe0832.android.base.debug.ipc.AAFDebugIPCFragment
import com.bihe0832.android.base.debug.log.DebugLogFragment
import com.bihe0832.android.base.debug.media.DebugMediaFragment
import com.bihe0832.android.base.debug.message.DebugMessageFragment
import com.bihe0832.android.base.debug.network.DebugNetworkActivity
import com.bihe0832.android.base.debug.network.DebugWiFiFragment
import com.bihe0832.android.base.debug.notify.DebugNotifyFragment
import com.bihe0832.android.base.debug.panel.DebugPanelFragment
import com.bihe0832.android.base.debug.permission.DebugPermissionFragment
import com.bihe0832.android.base.debug.qrcode.DebugQRCodeFragment
import com.bihe0832.android.base.debug.request.DebugHttpActivity
import com.bihe0832.android.base.debug.shake.DebugShakeAndVibratorFragment
import com.bihe0832.android.base.debug.share.DebugShareFragment
import com.bihe0832.android.base.debug.svga.DebugSvgaFragment
import com.bihe0832.android.base.debug.tab.DebugTabFragment
import com.bihe0832.android.base.debug.task.DebugEnqueueFragment
import com.bihe0832.android.base.debug.task.DebugThreadAndCoroutinesFragment
import com.bihe0832.android.base.debug.temp.DebugBasicFragment
import com.bihe0832.android.base.debug.temp.DebugTempFragment
import com.bihe0832.android.base.debug.toast.DebugToastFragment
import com.bihe0832.android.base.debug.tts.DebugTTSFragment
import com.bihe0832.android.base.debug.ui.DebugUIFragment
import com.bihe0832.android.base.debug.view.DebugBaseViewFragment
import com.bihe0832.android.base.debug.webview.DebugWebViewFragment
import com.bihe0832.android.base.debug.widget.DebugWidgetFragment
import com.bihe0832.android.common.compose.debug.DebugUtilsV2
import com.bihe0832.android.common.compose.debug.item.DebugComposeFragmentItem
import com.bihe0832.android.common.compose.debug.item.DebugItem
import com.bihe0832.android.common.compose.debug.ui.DebugContent
import com.bihe0832.android.common.debug.module.DebugRootActivity
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.ui.BaseFragment

@Composable
fun AAFDebugModule() {
    DebugContent {
        DebugComposeFragmentItem("临时测试(Temp)", DebugTempFragment::class.java)
        DebugFragmentItem("临时测试(Basic)", DebugBasicFragment::class.java)
        DebugFragmentItem("下载及安装 Download 调试", DebugDownloadFragment::class.java)
        DebugFragmentItem("Dialog、底部弹出Activity 调试", DebugDialogFragment::class.java)

        DebugFragmentItem("文件（Zip、assets 等）、配置、DB操作调试", DebugFileFragment::class.java)
        DebugFragmentItem("TAB 调试", DebugTabFragment::class.java)
        DebugFragmentItem("权限 Permission 调试", DebugPermissionFragment::class.java)
        DebugFragmentItem("公告消息 调试", DebugMessageFragment::class.java)
        DebugFragmentItem("拍照、相册、图片、视频、音频操作调试", DebugMediaFragment::class.java)
        DebugFragmentItem("Google相关 调试", DebugGoogleFragment::class.java)
        DebugFragmentItem("TTS 调试", DebugTTSFragment::class.java)
        DebugFragmentItem("定时任务、阻塞任务、延迟任务", DebugEnqueueFragment::class.java)
        DebugFragmentItem("协程、多线程、前台服务", DebugThreadAndCoroutinesFragment::class.java)
        DebugFragmentItem("摇一摇、震动等测试", DebugShakeAndVibratorFragment::class.java)

        DebugFragmentItem("数据转化", DebugConvertFragment::class.java)
        DebugFragmentItem("AES、RSA、MD5、SHA256等", DebugEncryptFragment::class.java)
        DebugFragmentItem("二维码调试", DebugQRCodeFragment::class.java)
        DebugFragmentItem(
            "UI（Intent跳转、反馈、评分、锁屏、隐私弹框、设置等）测试",
            DebugIntentFragment::class.java,
        )
        DebugFragmentItem("UI（Widget）测试", DebugWidgetFragment::class.java)
        DebugFragmentItem("UI（点击区、TextView、自定义View）测试", DebugBaseViewFragment::class.java)

        DebugFragmentItem("UI（颜色取色器）测试", DebugColorFragment::class.java)
        DebugFragmentItem("UI（绘图板）测试", DebugPanelFragment::class.java)
        DebugFragmentItem("UI（换肤、APPIcon切换、哀悼日、多语言、前后台等）测试", DebugUIFragment::class.java)


        DebugFragmentItem("Toast 调试", DebugToastFragment::class.java)
        DebugFragmentItem("分享调试", DebugShareFragment::class.java)

        DebugFragmentItem("悬浮窗测试(Basic)", DebugFloatViewFragment::class.java)
        DebugFragmentItem("SVGA 调试", DebugSvgaFragment::class.java)
        DebugFragmentItem("Cache、数据中心测试", DebugCacheFragment::class.java)
        DebugItem("沉浸式状态栏及标题栏调试") { context ->
            DebugUtilsV2.startActivityWithException(context, DebugImmersionActivity::class.java)
        }
        DebugFragmentItem("多进程调试", AAFDebugIPCFragment::class.java)
        DebugFragmentItem("日志调试", DebugLogFragment::class.java)
        DebugFragmentItem("通知栏调试", DebugNotifyFragment::class.java)
        DebugFragmentItem("WebView 调试", DebugWebViewFragment::class.java)


        DebugItem("网络切换监控") { context ->
            DebugUtilsV2.startActivityWithException(context, DebugNetworkActivity::class.java)
        }
        DebugFragmentItem("Wi-Fi 相关信息", DebugWiFiFragment::class.java)
        DebugItem("HTTP Request") { context ->
            DebugUtilsV2.startActivityWithException(context, DebugHttpActivity::class.java)
        }

        DebugFragmentItem("剪切板调试", DebugClipboardFragment::class.java)
        DebugFragmentItem("列表 Fragment 调试", DebugListFragment::class.java)
        DebugFragmentItem("Intent及跳转调试", DebugListFragment::class.java)

        DebugItem("列表 Activity 调试") { context ->
            DebugUtilsV2.startActivityWithException(context, TestListActivity::class.java)
        }
    }
}

@Composable
fun DebugFragmentItem(text: String, fragmentName: Class<out BaseFragment>) {
    DebugItem(text) {
        DebugRootActivity.startDebugRootActivity(
            ZixieContext.applicationContext!!,
            fragmentName,
            text,
        )
    }
}

