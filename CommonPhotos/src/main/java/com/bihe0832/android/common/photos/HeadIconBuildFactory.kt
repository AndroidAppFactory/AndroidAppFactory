/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/5 下午10:37
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/5 下午10:37
 *
 */

package com.bihe0832.android.common.photos

import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.android.lib.media.image.HeadIconBuilder
import java.io.File

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2022/7/5.
 * Description: Description
 *
 */
object HeadIconBuildFactory {

    fun generateBitmap(headIconBuilder: HeadIconBuilder, filepath: String, call: HeadIconBuilder.GenerateBitmapCallback) {
        generateBitmap(headIconBuilder, filepath, 30 * 1000, call)
    }

    fun generateBitmap(headIconBuilder: HeadIconBuilder, filepath: String, cacheDuration: Long, call: HeadIconBuilder.GenerateBitmapCallback) {

        if (FileUtils.checkFileExist(filepath)) {
            (System.currentTimeMillis() - File(filepath).lastModified()).let {
                if (it < cacheDuration) {
                    ThreadManager.getInstance().runOnUIThread {
                        ZLog.d("HeadIconBuildFactory", "cache file:$it")
                        call.onResult(null, filepath)
                    }
                    return
                }
            }
        }

        headIconBuilder.generateBitmap { bitmap, source ->
            FileUtils.checkAndCreateFolder(File(filepath).parent)
            FileUtils.copyFile(File(source), File(filepath)).let {
                ZLog.d("HeadIconBuildFactory", "new copyFile:$it")
            }
            //更新到最新
            ThreadManager.getInstance().runOnUIThread {
                call.onResult(bitmap, filepath)
            }
        }
    }
}