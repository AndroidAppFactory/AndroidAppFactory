/*
 * *
 *  * Created by zixie <code@bihe0832.com> on 2022/7/8 下午10:09
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 2022/7/8 下午10:05
 *
 */

package com.bihe0832.android.base.debug.audio


import android.view.View
import com.bihe0832.android.base.debug.R
import com.bihe0832.android.common.debug.item.DebugItemData
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.wrapper.DownloadFile
import com.bihe0832.android.lib.download.wrapper.SimpleDownloadListener
import com.bihe0832.lib.audio.player.block.AudioPLayerManager


class DebugAudioFragment : DebugEnvFragment() {
    val LOG_TAG = this.javaClass.simpleName

    private val blockAudioPlayerManager = AudioPLayerManager()


    override fun initView(view: View) {
        super.initView(view)
    }

    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(DebugItemData("播放远程音频", View.OnClickListener {
                DownloadFile.download(
                    context!!,
                    "https://cdn.bihe0832.com/audio/03.wav",
                    false,
                    object : SimpleDownloadListener() {
                        override fun onComplete(filePath: String, item: DownloadItem): String {
                            blockAudioPlayerManager.play(filePath)
                            blockAudioPlayerManager.play(context!!, R.raw.one)
                            blockAudioPlayerManager.play(context!!, R.raw.four)
                            return filePath
                        }

                        override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {

                        }

                        override fun onProgress(item: DownloadItem) {

                        }

                    })

            }))

            add(DebugItemData("播放本地音频", View.OnClickListener {
                blockAudioPlayerManager.play(context!!, R.raw.two)
                blockAudioPlayerManager.play(context!!,  R.raw.three, 1)
            }))

            add(DebugItemData("播放扫码音频", View.OnClickListener {
                blockAudioPlayerManager.play(context!!, R.raw.beep)
            }))


            add(DebugItemData("立即结束并清空队列", View.OnClickListener {
                blockAudioPlayerManager.stopAll(true)
            }))

            add(DebugItemData("清空队列", View.OnClickListener {
                blockAudioPlayerManager.stopAll(false)
            }))

            add(DebugItemData("清空队列并播放", View.OnClickListener {
                blockAudioPlayerManager.stopAll(false)
                blockAudioPlayerManager.play(context!!, R.raw.three)
            }))


        }
    }

}