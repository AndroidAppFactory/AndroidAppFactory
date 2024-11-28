/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.common.debug.device

import android.text.TextUtils
import com.bihe0832.android.common.debug.item.getLittleDebugItem
import com.bihe0832.android.common.debug.item.getTipsItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.common.list.CommonListLiveData
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.utils.apk.AppStorageUtil

open class DebugCurrentStorageFragment : DebugEnvFragment() {
    private var needSort = true
    private var showFile = false


    fun getFolderInfo(entry: Map.Entry<String, Long>): CardBaseModule {
        val itemContent = entry.key.replace(
            context!!.packageName, "包名"
        ) + " ：<b>${FileUtils.getFileLength(entry.value)}</b>"
        return getLittleDebugItem(
            itemContent,
            { showInfoWithHTML("应用调试信息", itemContent) }, false, TextUtils.TruncateAt.MIDDLE
        )
    }

    override fun getDataLiveData(): CommonListLiveData {
        return mListLiveData
    }

    private val mListLiveData = object : CommonListLiveData() {
        override fun initData() {
            ThreadManager.getInstance().start {
                val fileList = getDataList()
                val appSize = "当前应用共使用：<b>${
                    FileUtils.getFileLength(
                        AppStorageUtil.getCurrentAppSize(
                            context!!
                        )
                    )
                }</b>，其中APK占用：<b>${
                    FileUtils.getFileLength(AppStorageUtil.getCurrentApplicationSize(context!!))
                }</b>，内部存储占用：<b>${
                    FileUtils.getFileLength(AppStorageUtil.getCurrentAppDataSize(context!!))
                }</b>，外部存储占用：<b>${
                    FileUtils.getFileLength(
                        AppStorageUtil.getCurrentAppExternalDirSize(
                            context!!
                        )
                    )
                }</b>"

                ThreadManager.getInstance().runOnUIThread {
                    showResult(appSize)
                    postValue(fileList)
                }
            }
        }

        override fun refresh() {
            initData()
        }

        override fun loadMore() {

        }

        override fun hasMore(): Boolean {
            return false
        }

        override fun canRefresh(): Boolean {
            return true
        }
    }

    override fun getDataList(): ArrayList<CardBaseModule> {
        val data = ArrayList<CardBaseModule>()
        data.add(getTipsItem("<b>点击切换文件夹是否按大小排列，当前：$needSort</b>") {
            needSort = !needSort
            mListLiveData.refresh()
        })
        data.add(getTipsItem("<b>点击切换是否展示文件大小，当前：$showFile</b>") {
            showFile = !showFile
            mListLiveData.refresh()
        })
        AppStorageUtil.getCurrentAppFolderSizeList(
            context!!, FileUtils.SPACE_MB.toInt(), showFile, needSort
        ).forEach {
            data.add(getFolderInfo(it))
        }
        return data
    }
}