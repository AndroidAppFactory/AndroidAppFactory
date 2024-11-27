/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.common.debug.device

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.bihe0832.android.common.debug.item.getTipsItem
import com.bihe0832.android.lib.utils.apk.AppStorageUtil
import com.bihe0832.android.framework.file.AAFFileWrapper
import com.bihe0832.android.framework.router.RouterConstants
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.file.select.FileSelectTools
import java.io.File

class DebugStorageFragment : DebugCurrentStorageFragment() {

    private var folder = AAFFileWrapper.getFolder()

    override fun initView(view: View) {
        super.initView(view)
        showResult("当前目录：$folder")
    }

    override fun parseBundle(bundle: Bundle, isOnCreate: Boolean) {
        super.parseBundle(bundle, isOnCreate)
        folder =
            bundle.getString(RouterConstants.INTENT_EXTRA_KEY_WEB_URL) ?: AAFFileWrapper.getFolder()
    }

    override fun getDataList(): ArrayList<CardBaseModule> {

        val data = ArrayList<CardBaseModule>().apply {
            add(
                getDebugFragmentItemData(
                    "<b>查看当前应用的目录</b>",
                    DebugCurrentStorageFragment::class.java,true
                )
            )
            add(
                getTipsItem(
                    "<b>点击切换要查看的文件目录</b>"
                ) {
                    FileSelectTools.openFileSelect(
                        this@DebugStorageFragment,
                        AAFFileWrapper.getFolder()
                    )
                }
            )
            AppStorageUtil.getFolderSizeList(File(folder), 0, showFile = false, needSort = true).forEach {
                add(getFolderInfo(it))
            }
        }
        return data
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FileSelectTools.FILE_CHOOSER && resultCode == RESULT_OK) {
            data?.extras?.getString(FileSelectTools.INTENT_EXTRA_KEY_WEB_URL, "")?.let { filePath ->
                folder = filePath
                showResult("当前目录：$folder")
                getDataLiveData().refresh()
            }
        }
    }
}