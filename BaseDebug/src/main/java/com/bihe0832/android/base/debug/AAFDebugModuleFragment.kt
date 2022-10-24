/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug


import android.view.View
import com.bihe0832.android.base.debug.cache.DebugCacheFragment
import com.bihe0832.android.base.debug.card.DebugListFragment
import com.bihe0832.android.base.debug.card.TestListActivity
import com.bihe0832.android.base.debug.clipboard.DebugClipboardFragment
import com.bihe0832.android.base.debug.dialog.DebugDialogFragment
import com.bihe0832.android.base.debug.download.DebugDownloadFragment
import com.bihe0832.android.base.debug.block.DebugEnqueueFragment
import com.bihe0832.android.base.debug.file.DebugFileFragment
import com.bihe0832.android.base.debug.floatview.DebugFloatViewFragment
import com.bihe0832.android.base.debug.image.DebugImageFragment
import com.bihe0832.android.base.debug.immersion.DebugImmersionActivity
import com.bihe0832.android.base.debug.ipc.AAFDebugIPCFragment
import com.bihe0832.android.base.debug.log.DebugLogFragment
import com.bihe0832.android.base.debug.notify.DebugNotifyFragment
import com.bihe0832.android.base.debug.permission.DebugPermissionFragment
import com.bihe0832.android.base.debug.photos.DebugPhotosFragment
import com.bihe0832.android.base.debug.request.DebugHttpActivity
import com.bihe0832.android.base.debug.svga.DebugSvgaFragment
import com.bihe0832.android.base.debug.tab.DebugTabFragment
import com.bihe0832.android.base.debug.temp.DebugBasicFragment
import com.bihe0832.android.base.debug.temp.DebugTempFragment
import com.bihe0832.android.base.debug.tts.DebugTTSFragment
import com.bihe0832.android.base.debug.view.DebugTextViewFragment
import com.bihe0832.android.base.debug.webview.DebugWebviewActivity
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.common.praise.UserPraiseManager
import com.bihe0832.android.framework.router.RouterAction
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.request.URLUtils


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

            add(getDebugFragmentItemData("悬浮窗测试(Basic)", DebugFloatViewFragment::class.java))
            add(getDebugFragmentItemData("任务队列 调试", DebugEnqueueFragment::class.java))
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
            add(getDebugFragmentItemData("TextView 调试", DebugTextViewFragment::class.java))
            add(DebugItemData("WebView 调试", View.OnClickListener {
                startActivityWithException(DebugWebviewActivity::class.java)
            }))
            add(DebugItemData("HTTP Request", View.OnClickListener {
                startActivityWithException(DebugHttpActivity::class.java)
            }))

            add(DebugItemData("弹出评分页面", View.OnClickListener {
                UserPraiseManager.showUserPraiseDialog(activity!!, RouterAction.getFinalURL(RouterConstants.MODULE_NAME_FEEDBACK))
            }))
            add(getDebugFragmentItemData("剪切板调试", DebugClipboardFragment::class.java))

            add(getDebugFragmentItemData("列表 Fragment 调试", DebugListFragment::class.java))
            add(DebugItemData("列表 Activity 调试", View.OnClickListener {
                startActivityWithException(TestListActivity::class.java)
            }))
            add(DebugItemData("打开反馈页面", View.OnClickListener {
                val map = HashMap<String, String>()
                map[RouterConstants.INTENT_EXTRA_KEY_WEB_URL] = URLUtils.encode("https://support.qq.com/embed/phone/290858/large/")
                RouterAction.openPageByRouter(RouterConstants.MODULE_NAME_FEEDBACK, map)
            }))
        }
    }


}