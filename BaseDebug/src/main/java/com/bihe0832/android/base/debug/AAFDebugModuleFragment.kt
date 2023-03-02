/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug


import android.view.View
import com.bihe0832.android.base.debug.audio.DebugAudioFragment
import com.bihe0832.android.base.debug.block.DebugEnqueueFragment
import com.bihe0832.android.base.debug.cache.DebugCacheFragment
import com.bihe0832.android.base.debug.card.DebugListFragment
import com.bihe0832.android.base.debug.card.TestListActivity
import com.bihe0832.android.base.debug.clipboard.DebugClipboardFragment
import com.bihe0832.android.base.debug.convert.DebugConvertFragment
import com.bihe0832.android.base.debug.dialog.DebugDialogFragment
import com.bihe0832.android.base.debug.download.DebugDownloadFragment
import com.bihe0832.android.base.debug.file.DebugFileFragment
import com.bihe0832.android.base.debug.floatview.DebugFloatViewFragment
import com.bihe0832.android.base.debug.image.DebugImageFragment
import com.bihe0832.android.base.debug.immersion.DebugImmersionActivity
import com.bihe0832.android.base.debug.ipc.AAFDebugIPCFragment
import com.bihe0832.android.base.debug.log.DebugLogFragment
import com.bihe0832.android.base.debug.message.DebugMessageFragment
import com.bihe0832.android.base.debug.network.DebugNetworkActivity
import com.bihe0832.android.base.debug.notify.DebugNotifyFragment
import com.bihe0832.android.base.debug.permission.DebugPermissionFragment
import com.bihe0832.android.base.debug.photos.DebugPhotosFragment
import com.bihe0832.android.base.debug.request.DebugHttpActivity
import com.bihe0832.android.base.debug.svga.DebugSvgaFragment
import com.bihe0832.android.base.debug.tab.DebugTabFragment
import com.bihe0832.android.base.debug.temp.DebugBasicFragment
import com.bihe0832.android.base.debug.temp.DebugTempFragment
import com.bihe0832.android.base.debug.thread.DebugThreadAndCoroutinesFragmeAndnt
import com.bihe0832.android.base.debug.toast.DebugToastFragment
import com.bihe0832.android.base.debug.tts.DebugTTSFragment
import com.bihe0832.android.base.debug.ui.DebugUIFragment
import com.bihe0832.android.base.debug.view.DebugTextViewFragment
import com.bihe0832.android.base.debug.webview.DebugWebviewActivity
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.lib.adapter.CardBaseModule


class AAFDebugModuleFragment : DebugEnvFragment() {

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getDebugFragmentItemData("临时测试(Temp)", DebugTempFragment::class.java))
            add(getDebugFragmentItemData("临时测试(Basic)", DebugBasicFragment::class.java))

            add(getDebugFragmentItemData("下载及安装 Download 调试", DebugDownloadFragment::class.java))
            add(getDebugFragmentItemData("Dialog、底部弹出Activity 调试", DebugDialogFragment::class.java))
            add(getDebugFragmentItemData("文件（Zip、assets 等）、配置操作调试", DebugFileFragment::class.java))
            add(getDebugFragmentItemData("TAB 调试", DebugTabFragment::class.java))
            add(getDebugFragmentItemData("权限 Permission 调试", DebugPermissionFragment::class.java))
            add(getDebugFragmentItemData("公告消息 调试", DebugMessageFragment::class.java))


            add(getDebugFragmentItemData("TextView 调试", DebugTextViewFragment::class.java))
            add(getDebugFragmentItemData("音频播放", DebugAudioFragment::class.java))
            add(getDebugFragmentItemData("协程及多线程调用测试", DebugThreadAndCoroutinesFragmeAndnt::class.java))



            add(getDebugFragmentItemData("数据转化", DebugConvertFragment::class.java))
            add(getDebugFragmentItemData("UI（点击区、Toast、前后台）测试", DebugUIFragment::class.java))
            add(getDebugFragmentItemData("Toast 调试", DebugToastFragment::class.java))


            add(getDebugFragmentItemData("悬浮窗测试(Basic)", DebugFloatViewFragment::class.java))
            add(getDebugFragmentItemData("定时任务、阻塞任务", DebugEnqueueFragment::class.java))
            add(getDebugFragmentItemData("图片操作调试", DebugImageFragment::class.java))
            add(getDebugFragmentItemData("SVGA 调试", DebugSvgaFragment::class.java))
            add(getDebugFragmentItemData("拍照及相册调试", DebugPhotosFragment::class.java))
            add(getDebugFragmentItemData("Cache、数据中心测试", DebugCacheFragment::class.java))
            add(DebugItemData("沉浸式状态栏及标题栏调试", View.OnClickListener {
                startActivityWithException(DebugImmersionActivity::class.java)
            }))
            add(getDebugFragmentItemData("多进程调试", AAFDebugIPCFragment::class.java))
            add(getDebugFragmentItemData("日志调试", DebugLogFragment::class.java))
            add(getDebugFragmentItemData("通知栏调试", DebugNotifyFragment::class.java))
            add(getDebugFragmentItemData("TTS 调试", DebugTTSFragment::class.java))
            add(DebugItemData("WebView 调试", View.OnClickListener {
                startActivityWithException(DebugWebviewActivity::class.java)
            }))

            add(DebugItemData("网络切换监控") { startActivityWithException(DebugNetworkActivity::class.java) })
            add(DebugItemData("HTTP Request", View.OnClickListener {
                startActivityWithException(DebugHttpActivity::class.java)
            }))


            add(getDebugFragmentItemData("剪切板调试", DebugClipboardFragment::class.java))

            add(getDebugFragmentItemData("列表 Fragment 调试", DebugListFragment::class.java))
            add(DebugItemData("列表 Activity 调试", View.OnClickListener {
                startActivityWithException(TestListActivity::class.java)
            }))

        }
    }
}