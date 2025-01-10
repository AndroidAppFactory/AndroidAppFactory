/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.audio

import com.bihe0832.android.common.debug.audio.DebugAudioDataFactory
import com.bihe0832.android.common.debug.audio.DebugWAVListWithProcessFragment
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.item.getTipsItem
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.log.LoggerFile
import com.bihe0832.android.lib.file.FileUtils.getFileLength
import java.io.File

/**
 *
 * @author zixie code@bihe0832.com
 * Created on 2025/1/10.
 * Description: 本地音频批处理自定义
 *
 */
class DebugLocalWAVListFragment : DebugWAVListWithProcessFragment() {

    override fun filterFile(filePath: String): Boolean {
        return File(filePath).length() > 44
    }

    override fun getTips(): DebugItemData {
        val tips =
            "1. <b><font color='#3AC8EF'>点击</font>图标</b>，播放音频<BR>" +
                    "2. <b><font color='#3AC8EF'>长按</font>图标</b>，可以删除音频，<b><font color='#3AC8EF'>长按</font>标题和内容</b>，可以发送音频"
        return getTipsItem(tips)
    }

    override fun getLogFile(): String {
        return LoggerFile.getZixieFileLogPathByModule(
            "audio1", ZixieContext.getLogFolder(), LoggerFile.TYPE_HTML
        )
    }

    override fun getAudioDataProcessInterface(): DebugAudioDataFactory {
        return object : DebugAudioDataFactory(getAudioDataFactoryCallback()) {
            override fun processAudioData(
                logFile: String,
                index: Int,
                folder: String,
                filePath: String
            ) {
                commonProcessAudioData(logFile, index, folder, filePath)
                LoggerFile.logH5(getLogFile(), "", "文件大小：" + getFileLength(File(filePath).length()))
                Thread.sleep(1000L)
            }
        }
    }
}
