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
import com.bihe0832.android.common.debug.item.getDebugItem
import com.bihe0832.android.common.debug.module.DebugEnvFragment
import com.bihe0832.android.common.video.FFmpegTools
import com.bihe0832.android.lib.aaf.tools.AAFDataCallback
import com.bihe0832.android.lib.adapter.CardBaseModule
import com.bihe0832.android.lib.download.DownloadItem
import com.bihe0832.android.lib.download.wrapper.DownloadFile
import com.bihe0832.android.lib.download.wrapper.SimpleDownloadListener
import com.bihe0832.android.lib.file.FileUtils
import com.bihe0832.android.lib.log.ZLog
import com.bihe0832.android.lib.thread.ThreadManager
import com.bihe0832.lib.audio.player.AudioPlayListener
import com.bihe0832.lib.audio.player.block.AudioPLayerManager


class DebugAudioFragment : DebugEnvFragment() {
    val LOG_TAG = this.javaClass.simpleName

    private val blockAudioPlayerManager = AudioPLayerManager()


    override fun getDataList(): ArrayList<CardBaseModule> {
        return ArrayList<CardBaseModule>().apply {
            add(getDebugItem("播放远程音频", View.OnClickListener {
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

            add(getDebugItem("远程音频裁剪", View.OnClickListener {
                DownloadFile.download(
                    context!!,
                    "https://cdn.bihe0832.com/audio/03.wav",
                    "",false,
                    "FAEE24E98CD256E204FAF5168FA39D24",
                    false,
                    object : SimpleDownloadListener() {
                        override fun onComplete(filePath: String, item: DownloadItem): String {
                            ZLog.d(FileUtils.getFileMD5(filePath))
                            FFmpegTools.splitAudioWithDuration(filePath, 0.7f, 2f, object : AAFDataCallback<String>() {
                                override fun onSuccess(result: String?) {
                                    blockAudioPlayerManager.play(filePath)
                                    blockAudioPlayerManager.play(result ?: "")
                                }
                            })
                            return filePath
                        }

                        override fun onFail(errorCode: Int, msg: String, item: DownloadItem) {

                        }

                        override fun onProgress(item: DownloadItem) {

                        }

                    })

            }))

            add(getDebugItem("播放本地音频", View.OnClickListener {
                blockAudioPlayerManager.play(context!!, R.raw.one)
                blockAudioPlayerManager.play(context!!, R.raw.two, 0.1f, 1.0f, 1.0f, 1, null)
                blockAudioPlayerManager.play(context!!, R.raw.three)
                blockAudioPlayerManager.play(context!!, R.raw.icon)
                blockAudioPlayerManager.play(context!!, R.raw.test)
                blockAudioPlayerManager.play(context!!, R.raw.three)
            }))

            add(getDebugItem("本地音频极限测试", View.OnClickListener {
                ThreadManager.getInstance().start {
                    ZLog.d("本地音频极限测试开始")
                    for (i in 0..257) {
                        blockAudioPlayerManager.play(context!!, R.raw.one)
                    }
                    ZLog.d("本地音频极限测试结束")
                }
            }))

            add(getDebugItem("播放扫码音频", View.OnClickListener {
                blockAudioPlayerManager.play(context!!, R.raw.beep)
            }))


            add(getDebugItem("立即结束并清空队列", View.OnClickListener {
                blockAudioPlayerManager.stopAll(true)
            }))

            add(getDebugItem("清空队列", View.OnClickListener {
                blockAudioPlayerManager.stopAll(false)
            }))

            add(getDebugItem("清空队列并播放", View.OnClickListener {
                blockAudioPlayerManager.stopAll(false)
                blockAudioPlayerManager.play(context!!, R.raw.three)
            }))

            add(getDebugItem("强制中断当前并播放下一个", View.OnClickListener {
                blockAudioPlayerManager.play(context!!, R.raw.three, 0.1f, 1.0f, 1.0f, 1, object : AudioPlayListener {
                    override fun onLoad() {
                        ZLog.d(LOG_TAG, "onLoad")
                    }

                    override fun onLoadComplete(soundid: Int, status: Int) {
                        ZLog.d(LOG_TAG, "onLoadComplete")
                    }

                    override fun onPlayStart() {
                        ZLog.d(LOG_TAG, "onPlayStart")
                    }

                    override fun onPlayFinished(error: Int, msg: String) {
                        ZLog.d(LOG_TAG, "onPlayFinished")
                    }

                })
                ThreadManager.getInstance().start({
                    blockAudioPlayerManager.finishCurrent()
                    blockAudioPlayerManager.play(
                        context!!,
                        R.raw.three,
                        0.1f,
                        1.0f,
                        1.0f,
                        1,
                        object : AudioPlayListener {
                            override fun onLoad() {
                                ZLog.d(LOG_TAG, "onLoad")
                            }

                            override fun onLoadComplete(soundid: Int, status: Int) {
                                ZLog.d(LOG_TAG, "onLoadComplete")
                            }

                            override fun onPlayStart() {
                                ZLog.d(LOG_TAG, "onPlayStart")
                            }

                            override fun onPlayFinished(error: Int, msg: String) {
                                ZLog.d(LOG_TAG, "onPlayFinished")
                            }

                        })
                }, 800L)

            }))

        }
    }

}