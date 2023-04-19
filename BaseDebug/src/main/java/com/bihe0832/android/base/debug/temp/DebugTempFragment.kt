/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.temp


import android.view.View
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.framework.ZixieContext
import com.bihe0832.android.framework.request.ZixieRequestHttp
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.wrapper.DownloadFile
import com.bihe0832.android.lib.download.wrapper.SimpleDownloadListener
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.lib.audio.player.block.AudioPLayerManager
import java.io.File


class DebugTempFragment : DebugEnvFragment() {
    val LOG_TAG = this.javaClass.simpleName
    private val blockAudioPlayerManager = AudioPLayerManager()

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugItemData("简单测试函数", View.OnClickListener { testFunc() }))
            add(DebugItemData("通用测试预处理", View.OnClickListener { preTest(it) }))
        }
    }


    private fun preTest(itemView: View) {

    }

    private fun testFunc() {
        val url = "https://cdn.bihe0832.com/audio/02.wav"
        ThreadManager.getInstance().start {
            ZixieRequestHttp.getOriginByteArray(url).let {
                val file = File(ZixieContext.getZixieFolder() + "02.wav")
                file.writeBytes(it)
                blockAudioPlayerManager.play(file.absolutePath)
            }
        }
    }
}